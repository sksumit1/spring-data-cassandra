package org.springframework.cassandra.test.integration;

import org.springframework.cassandra.core.CqlTemplate;

public class AbstractCqlTemplateIntegrationTest extends AbstractKeyspaceCreatingIntegrationTest {

	protected CqlTemplate t;

	{
		t = new CqlTemplate(SESSION);
	}

	public AbstractCqlTemplateIntegrationTest() {}

	public AbstractCqlTemplateIntegrationTest(String keyspace) {
		super(keyspace);
	}

}
