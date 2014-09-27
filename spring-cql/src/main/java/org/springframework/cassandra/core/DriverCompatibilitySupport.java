package org.springframework.cassandra.core;

import java.nio.ByteBuffer;

import org.springframework.util.ClassUtils;

import com.datastax.driver.core.DataType;

public class DriverCompatibilitySupport {

	public static final boolean DRIVER_2_1_1_PRESENT = ClassUtils.hasMethod(DataType.class, "deserialize",
			ByteBuffer.class, int.class);

	/**
	 * Uses the appropriate version of {@link DataType}'s <code>deserialize</code> method, depending on which version of
	 * the Datastax driver is on the classpath.
	 * 
	 * @param dataType The {@link DataType} to use to deserialize.
	 * @param buffer The bytes to deserialize.
	 * @param protocolVersion ignored if the Datastax driver is earlier than version 2.1.0.
	 * @return The deserialized object.
	 */
	@SuppressWarnings("deprecation")
	public static Object deserialize(DataType dataType, ByteBuffer buffer, int protocolVersion) {
		if (DRIVER_2_1_1_PRESENT) {
			return dataType.deserialize(buffer, protocolVersion);
		}
		return dataType.deserialize(buffer);
	}
}
