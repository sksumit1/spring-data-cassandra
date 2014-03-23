package org.springframework.cassandra.core.cql.naming;

public class CasePreservingCqlNamingStrategy implements CqlNamingStrategy {

	@Override
	public String transform(String srcName) {
		return srcName;
	}

}
