package org.springframework.data.cassandra.test.integration.querymethods.declared;

import org.junit.runner.RunWith;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.test.integration.support.IntegrationTestConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class QueryIntegrationJavaConfigTests extends QueryIntegrationTests {

	public static class Config extends IntegrationTestConfig {

		@Override
		public String[] getEntityBasePackages() {
			return new String[] { Person.class.getPackage().getName() };
		}

		@Override
		public SchemaAction getSchemaAction() {
			return SchemaAction.RECREATE_DROP_UNUSED;
		}
	}
}
