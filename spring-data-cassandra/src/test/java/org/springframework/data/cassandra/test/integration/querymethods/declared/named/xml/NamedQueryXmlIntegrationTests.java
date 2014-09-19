package org.springframework.data.cassandra.test.integration.querymethods.declared.named.xml;

import org.springframework.data.cassandra.test.integration.querymethods.declared.QueryIntegrationTests;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:/META-INF/person-repository-with-named-queries.xml")
public class NamedQueryXmlIntegrationTests extends QueryIntegrationTests {}
