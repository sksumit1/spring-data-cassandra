package org.springframework.cassandra.core.cql.naming;

public class CaseLoweringCqlNamingStrategy implements CqlNamingStrategy {

	@Override
	public String transform(String srcName) {
		return srcName.toLowerCase();
	}

}
