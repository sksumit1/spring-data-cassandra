package org.springframework.data.cassandra.test.integration.querymethods.declared.anno;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.test.integration.querymethods.declared.QueryIntegrationJavaConfigTests;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class QueryAnnotationIntegrationTests extends QueryIntegrationJavaConfigTests {

	@Configuration
	@EnableCassandraRepositories(basePackageClasses = PersonRepositoryWithQueryAnnotations.class)
	public static class Config extends QueryIntegrationJavaConfigTests.Config {}
}
