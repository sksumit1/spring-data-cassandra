package org.springframework.cassandra.test.integration.core.template.async;

import java.util.List;

import org.springframework.cassandra.core.QueryForListListener;
import org.springframework.cassandra.test.unit.support.TestListener;

public class ListListener<T> extends TestListener implements QueryForListListener<T> {

	Exception exception;

	@Override
	public void onQueryComplete(List<T> results) {
		countDown();
	}

	@Override
	public void onException(Exception x) {
		countDown();
		this.exception = x;
	}
}
