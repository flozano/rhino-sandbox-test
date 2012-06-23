package com.flozano.rhino.sandbox.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flozano.rhino.sandbox.SandboxShutter;

public class DummySandboxShutterImpl implements SandboxShutter {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DummySandboxShutterImpl.class);

	public boolean allowClassAccess(Class<?> type) {
		LOGGER.info("Allow class access: type = {}", type.getCanonicalName());
		return true;
	}

	public boolean allowFieldAccess(Class<?> type, Object instance,
			String fieldName) {
		LOGGER.info("Allow field access: type = {}, fieldName={}, instance={}",
				new Object[] { type.getCanonicalName(), fieldName, instance });
		return true;
	}

	public boolean allowMethodAccess(Class<?> type, Object instance,
			String methodName) {
		LOGGER.info(
				"Allow method access: type = {}, methodName={}, instance={}",
				new Object[] { type.getCanonicalName(), methodName, instance });
		return true;
	}

	public boolean allowStaticFieldAccess(Class<?> type, String fieldName) {
		LOGGER.info("Allow static field access: type={}, fieldName={}",
				new Object[] { type.getCanonicalName(), fieldName });
		return true;
	}

	public boolean allowStaticMethodAccess(Class<?> type, String methodName) {
		LOGGER.info("Allow static method access: type={}, methodName={}",
				new Object[] { type.getCanonicalName(), methodName });
		return true;
	}

}
