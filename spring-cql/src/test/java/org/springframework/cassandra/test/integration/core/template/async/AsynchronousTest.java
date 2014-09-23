package org.springframework.cassandra.test.integration.core.template.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.cassandra.core.keyspace.CreateTableSpecification.createTable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cassandra.core.AsynchronousQueryListener;
import org.springframework.cassandra.core.ConsistencyLevel;
import org.springframework.cassandra.core.QueryCancellor;
import org.springframework.cassandra.core.QueryForMapListener;
import org.springframework.cassandra.core.QueryForObjectListener;
import org.springframework.cassandra.core.QueryOptions;
import org.springframework.cassandra.core.RetryPolicy;
import org.springframework.cassandra.support.exception.CassandraInsufficientReplicasAvailableException;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

public class AsynchronousTest extends AbstractAsynchronousTest {

	public static String cql(String isbn) {
		return String.format("select * from %s where isbn = '%s'", Book.TABLE, isbn);
	}

	public static Select select(String isbn) {
		Select select = QueryBuilder.select("isbn", "title").from(Book.TABLE);
		select.where(QueryBuilder.eq("isbn", isbn));
		return select;
	}

	public static void assertMapEquals(Map<?, ?> expected, Map<?, ?> actual) {
		for (Object key : expected.keySet()) {
			assertTrue(actual.containsKey(key));
			assertEquals(expected.get(key), actual.get(key));
		}
	}

	void ensureTableExists() {
		t.execute(createTable(Book.TABLE).ifNotExists().partitionKeyColumn("isbn", DataType.ascii())
				.column("title", DataType.ascii()));
	}

	Book[] insert(int n) {
		Book[] books = new Book[n];
		for (int i = 0; i < n; i++) {
			Book b = books[i] = Book.random();
			t.execute(String.format("insert into %s (isbn, title) values ('%s', '%s')", Book.TABLE, b.isbn, b.title));
		}
		return books;
	}

	@Before
	public void beforeEach() {
		ensureTableExists();
		t.truncate(Book.TABLE);
	}

	void assertBook(Book expected, Book actual) {
		assertEquals(expected.isbn, actual.isbn);
		assertEquals(expected.title, actual.title);
	}

	/**
	 * Tests that test {@link AsynchronousQueryListener} should create an anonymous subclass of this class then call
	 * either {@link #test()} or {@link #test(int)}
	 */
	abstract class AsynchronousQueryListenerTestTemplate {

		/**
		 * Subclass must perform the asynchronous query using the given data & listener and set <code>this.expected</code>
		 * to the appropriate value before returning.
		 */
		abstract void doAsyncQuery(Book b, BasicListener listener);

		void test() throws InterruptedException {
			test(1);
		}

		void test(int n) throws InterruptedException {
			Book[] expected = insert(n);
			Book[] actual = new Book[n];
			for (int i = 0; i < expected.length; i++) {
				BasicListener listener = new BasicListener();
				doAsyncQuery(expected[i], listener);
				listener.await();
				Row r = t.getResultSetUninterruptibly(listener.rsf).one();
				actual[i] = new Book(r.getString(0), r.getString(1));
				assertBook(expected[i], actual[i]);
			}
		}
	}

	/**
	 * Tests that test {@link QueryForObjectListener} should create an anonymous subclass of this class then call either
	 * {@link #test()} or {@link #test(int)}
	 */
	abstract class QueryForObjectListenerTestTemplate<T> {

		/**
		 * Subclass must perform the asynchronous query using the given data & listener and set <code>this.expected</code>
		 * to the appropriate value before returning.
		 */
		abstract void doAsyncQuery(Book b, QueryForObjectListener<T> listener);

		T expected; // subclass should set this value in doAsyncQuery

		void test() throws Exception {
			test(1);
		}

		void test(int n) throws Exception {
			Book[] expecteds = insert(n);
			for (int i = 0; i < expecteds.length; i++) {
				ObjectListener<T> listener = new ObjectListener<T>();
				doAsyncQuery(expecteds[i], listener);
				listener.await();
				if (listener.exception != null) {
					throw listener.exception;
				}
				assertEquals(expected, listener.result);
			}
		}
	}

	/**
	 * Tests that test {@link QueryForMapListener} should create an anonymous subclass of this class then call either
	 * {@link #test()} or {@link #test(int)}
	 */
	abstract class QueryForMapListenerTestTemplate {

		/**
		 * Subclass must perform the asynchronous query using the given data & listener and set <code>this.expected</code>
		 * to the appropriate value before returning.
		 */
		abstract void doAsyncQuery(Book b, QueryForMapListener listener);

		Map<String, Object> expected; // subclass should set this value in doAsyncQuery

		void test() throws Exception {
			test(1);
		}

		void test(int n) throws Exception {
			Book[] expecteds = insert(n);
			for (int i = 0; i < expecteds.length; i++) {
				MapListener listener = new MapListener();
				doAsyncQuery(expecteds[i], listener);
				listener.await();
				if (listener.exception != null) {
					throw listener.exception;
				}
				assertMapEquals(expected, listener.result);
			}
		}
	}

	@Test(expected = CancellationException.class)
	public void testString_AsynchronousQueryListener_Cancelled() throws InterruptedException {
		new AsynchronousQueryListenerTestTemplate() {
			@Override
			void doAsyncQuery(Book b, BasicListener listener) {
				QueryCancellor qc = t.queryAsynchronously(cql(b.isbn), listener);
				qc.cancelQuery();
			}
		}.test();
	}

	@Test
	public void testString_AsynchronousQueryListener() throws InterruptedException {
		new AsynchronousQueryListenerTestTemplate() {
			@Override
			void doAsyncQuery(Book b, BasicListener listener) {
				t.queryAsynchronously(cql(b.isbn), listener);
			}
		}.test();
	}

	@Test
	public void testString_AsynchronousQueryListener_QueryOptions() throws InterruptedException {
		new AsynchronousQueryListenerTestTemplate() {
			@Override
			void doAsyncQuery(Book b, BasicListener listener) {
				t.queryAsynchronously(cql(b.isbn), listener, new QueryOptions(ConsistencyLevel.ONE, RetryPolicy.LOGGING));
			}
		}.test();
	}

	@Test(expected = CassandraInsufficientReplicasAvailableException.class)
	public void testString_AsynchronousQueryListener_QueryOptionsWithConsistencyLevel2() throws InterruptedException {
		new AsynchronousQueryListenerTestTemplate() {
			@Override
			void doAsyncQuery(Book b, BasicListener listener) {
				// next line should cause failure on query because we integration test with only ONE node
				t.queryAsynchronously(cql(b.isbn), listener, new QueryOptions(ConsistencyLevel.TWO, RetryPolicy.LOGGING));
			}
		}.test();
	}

	@Test
	public void testSelect_AsynchronousQueryListener() throws InterruptedException {
		new AsynchronousQueryListenerTestTemplate() {
			@Override
			void doAsyncQuery(Book b, BasicListener listener) {
				t.queryAsynchronously(cql(b.isbn), listener);
			}
		}.test();
	}

	@Test
	public void testString_QueryForObjectListener() throws Exception {
		new QueryForObjectListenerTestTemplate<String>() {

			@Override
			void doAsyncQuery(Book b, QueryForObjectListener<String> listener) {
				t.queryForObjectAsynchronously(cql(b.isbn), String.class, listener);
				expected = b.isbn;
			}

		}.test();
	}

	@Test
	public void testString_QueryForObjectListener_QueryOptions() throws Exception {
		new QueryForObjectListenerTestTemplate<String>() {

			@Override
			void doAsyncQuery(Book b, QueryForObjectListener<String> listener) {
				QueryOptions opts = new QueryOptions(ConsistencyLevel.ONE, RetryPolicy.LOGGING);
				t.queryForObjectAsynchronously(cql(b.isbn), String.class, listener, opts);
				expected = b.isbn;
			}

		}.test();
	}

	@Test(expected = CassandraInsufficientReplicasAvailableException.class)
	public void testString_QueryForObjectListener_QueryOptionsWithConsistencyLevel2() throws Exception {
		new QueryForObjectListenerTestTemplate<String>() {

			@Override
			void doAsyncQuery(Book b, QueryForObjectListener<String> listener) {
				// next line should cause failure on query because we integration test with only ONE node
				t.queryForObjectAsynchronously(cql(b.isbn), String.class, listener, new QueryOptions(ConsistencyLevel.TWO,
						RetryPolicy.LOGGING));
				expected = b.isbn;
			}

		}.test();
	}

	@Test
	public void testString_QueryForMapListener() throws Exception {
		new QueryForMapListenerTestTemplate() {

			@Override
			void doAsyncQuery(Book b, QueryForMapListener listener) {
				t.queryForMapAsynchronously(cql(b.isbn), listener);
				expected = new HashMap<String, Object>();
				expected.put("isbn", b.isbn);
				expected.put("title", b.title);
			}

		}.test();
	}

	@Test
	public void testString_QueryForMapListener_QueryOptions() throws Exception {
		new QueryForMapListenerTestTemplate() {

			@Override
			void doAsyncQuery(Book b, QueryForMapListener listener) {
				QueryOptions opts = new QueryOptions(ConsistencyLevel.ONE, RetryPolicy.LOGGING);
				t.queryForMapAsynchronously(cql(b.isbn), listener, opts);
				expected = new HashMap<String, Object>();
				expected.put("isbn", b.isbn);
				expected.put("title", b.title);
			}

		}.test();
	}

	@Test(expected = CassandraInsufficientReplicasAvailableException.class)
	public void testString_QueryForMapListener_QueryOptionsWithConsistencyLevel2() throws Exception {
		new QueryForMapListenerTestTemplate() {

			@Override
			void doAsyncQuery(Book b, QueryForMapListener listener) {
				// next line should cause failure on query because we integration test with only ONE node
				t.queryForMapAsynchronously(cql(b.isbn), listener, new QueryOptions(ConsistencyLevel.TWO, RetryPolicy.LOGGING));
				expected = new HashMap<String, Object>();
				expected.put("isbn", b.isbn);
				expected.put("title", b.title);
			}

		}.test();
	}
}
