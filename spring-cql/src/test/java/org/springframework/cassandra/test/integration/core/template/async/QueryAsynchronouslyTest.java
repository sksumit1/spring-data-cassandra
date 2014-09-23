package org.springframework.cassandra.test.integration.core.template.async;

import static org.junit.Assert.assertEquals;
import static org.springframework.cassandra.core.keyspace.CreateTableSpecification.createTable;

import java.util.concurrent.CancellationException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cassandra.core.ConsistencyLevel;
import org.springframework.cassandra.core.QueryCancellor;
import org.springframework.cassandra.core.QueryOptions;
import org.springframework.cassandra.core.RetryPolicy;
import org.springframework.cassandra.support.exception.CassandraInsufficientReplicasAvailableException;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

public class QueryAsynchronouslyTest extends AbstractAsynchronousTest {

	public static String cql(String isbn) {
		return String.format("select * from %s where isbn = '%s'", Book.TABLE, isbn);
	}

	public static Select select(String isbn) {
		Select select = QueryBuilder.select("isbn", "title").from(Book.TABLE);
		select.where(QueryBuilder.eq("isbn", isbn));
		return select;
	}

	void createTableIf() {
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
		createTableIf();
		t.truncate(Book.TABLE);
	}

	void assertBook(Book expected, Book actual) {
		assertEquals(expected.isbn, actual.isbn);
		assertEquals(expected.title, actual.title);
	}

	abstract class Template {

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

	@Test(expected = CancellationException.class)
	public void testString_AsynchronousQueryListener_Cancelled() throws InterruptedException {
		new Template() {
			@Override
			void doAsyncQuery(Book b, BasicListener listener) {
				QueryCancellor qc = t.queryAsynchronously(cql(b.isbn), listener);
				qc.cancelQuery();
			}
		}.test();
	}

	@Test
	public void testString_AsynchronousQueryListener() throws InterruptedException {
		new Template() {
			@Override
			void doAsyncQuery(Book b, BasicListener listener) {
				t.queryAsynchronously(cql(b.isbn), listener);
			}
		}.test();
	}

	@Test
	public void testString_AsynchronousQueryListener_QueryOptions() throws InterruptedException {
		new Template() {
			@Override
			void doAsyncQuery(Book b, BasicListener listener) {
				QueryOptions opts = new QueryOptions();
				opts.setConsistencyLevel(ConsistencyLevel.ONE);
				opts.setRetryPolicy(RetryPolicy.LOGGING);
				t.queryAsynchronously(cql(b.isbn), listener, opts);
			}
		}.test();
	}

	@Test(expected = CassandraInsufficientReplicasAvailableException.class)
	public void testString_AsynchronousQueryListener_QueryOptionsWithConsistencyLevel2() throws InterruptedException {
		new Template() {
			@Override
			void doAsyncQuery(Book b, BasicListener listener) {
				QueryOptions opts = new QueryOptions();
				// next line should cause failure on query because we integration test with only ONE node
				opts.setConsistencyLevel(ConsistencyLevel.TWO);
				opts.setRetryPolicy(RetryPolicy.LOGGING);
				t.queryAsynchronously(cql(b.isbn), listener, opts);
			}
		}.test();
	}

	@Test
	public void testSelect_AsynchronousQueryListener() throws InterruptedException {
		new Template() {
			@Override
			void doAsyncQuery(Book b, BasicListener listener) {
				t.queryAsynchronously(cql(b.isbn), listener);
			}
		}.test();
	}

}
