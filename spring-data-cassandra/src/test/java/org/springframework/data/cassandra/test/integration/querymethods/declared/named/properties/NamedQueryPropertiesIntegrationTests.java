package org.springframework.data.cassandra.test.integration.querymethods.declared.named.properties;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.test.integration.querymethods.declared.QueryIntegrationJavaConfigTests;
import org.springframework.data.cassandra.test.integration.querymethods.declared.named.PersonRepositoryWithNamedQueries;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class NamedQueryPropertiesIntegrationTests extends QueryIntegrationJavaConfigTests {

	@Configuration
	@EnableCassandraRepositories(basePackageClasses = PersonRepositoryWithNamedQueries.class,
			namedQueriesLocation = "classpath:META-INF/PersonRepositoryWithNamedQueries.properties")
	public static class Config extends QueryIntegrationJavaConfigTests.Config {}
}
