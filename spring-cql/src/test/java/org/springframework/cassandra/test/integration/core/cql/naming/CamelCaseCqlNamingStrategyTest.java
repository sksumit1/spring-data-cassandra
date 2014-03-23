package org.springframework.cassandra.test.integration.core.cql.naming;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cassandra.core.cql.naming.CamelCaseToUnderscoreCqlNamingStrategy;
import org.springframework.cassandra.core.cql.naming.CqlNamingStrategy;

public class CamelCaseCqlNamingStrategyTest {
	private CqlNamingStrategy strategy;

	@Before
	public void before() {
		strategy = new CamelCaseToUnderscoreCqlNamingStrategy();
	}

	@Test
	public void simplestTest() {
		String result = strategy.transform("a");
		assertEquals("a", result);
	}

	@Test
	public void test() {
		String result = strategy.transform("aB");
		assertEquals("a_b", result);
	}

	@Test
	public void test1() {
		String result = strategy.transform("aBC");
		assertEquals("a_bc", result);
	}

	@Test
	public void test2() {
		String result = strategy.transform("aBCa");
		assertEquals("a_bca", result);
	}

	@Test
	public void test3() {
		String result = strategy.transform("aBcD");
		assertEquals("a_bc_d", result);
	}

	@Test
	public void test4() {
		String result = strategy.transform("ABCD");
		assertEquals("abcd", result);
	}

	@Test
	public void test5() {
		String result = strategy.transform("AbCd");
		assertEquals("ab_cd", result);
	}

}