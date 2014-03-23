package org.springframework.cassandra.core.cql.naming;

/**
 * 
 * @author John McPeek
 * 
 */
public class CamelCaseToUnderscoreCqlNamingStrategy implements CqlNamingStrategy {
	private static final String CAMEL_CASE_SPLITER = "(?<=[a-z])(?=[A-Z])";

	@Override
	public String transform(String srcName) {
		String[] parts = srcName.split(CAMEL_CASE_SPLITER);

		String cqlName = "";
		for (String part : parts) {
			if (part != null && part.length() > 0) {
				cqlName += part.toLowerCase() + "_";
			}
		}

		if (cqlName.length() > 0) {
			cqlName = cqlName.substring(0, cqlName.length() - 1);
		}

		return cqlName;
	}

}
