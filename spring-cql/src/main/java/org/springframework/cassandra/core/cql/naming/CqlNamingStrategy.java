package org.springframework.cassandra.core.cql.naming;

public interface CqlNamingStrategy {
	String transform(String srcName);
}
