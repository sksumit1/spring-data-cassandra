package org.springframework.data.cassandra.mapping;

import org.springframework.util.Assert;

/**
 * A named query for an entity.
 * 
 * @author Matthew T. Adams
 */
public class NamedQuery {

	protected String name;
	protected String value;

	public NamedQuery(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		Assert.hasText(name);
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		Assert.hasText(value);
		this.value = value;
	}
}
