package org.springframework.cassandra.test.integration.core.template.async;

import java.util.UUID;

public class Book {

	public static final String uuid() {
		return UUID.randomUUID().toString();
	}

	public static final String TABLE = "book";

	public static Book random() {
		return new Book(uuid(), uuid());
	}

	public Book() {}

	public Book(String isbn, String title) {
		this.isbn = isbn;
		this.title = title;
	}

	public String isbn;
	public String title;
}
