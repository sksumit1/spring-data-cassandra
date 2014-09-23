package org.springframework.cassandra.test.integration.core.template.async;

import java.util.Map;

import org.springframework.cassandra.core.QueryForMapListener;
import org.springframework.cassandra.test.unit.support.TestListener;

public class MapListener extends TestListener implements QueryForMapListener {

	@Override
	public void onQueryComplete(Map<String, Object> results) {
		countDown();
	}

	@Override
	public void onException(Exception x) {
		countDown();
	}
}
