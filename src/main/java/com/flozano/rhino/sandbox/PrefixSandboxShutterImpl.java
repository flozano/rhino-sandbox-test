package com.flozano.rhino.sandbox;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PrefixSandboxShutterImpl implements SandboxShutter {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(PrefixSandboxShutterImpl.class);
	private static final Set<String> GLOBALLY_FORBIDDEN_FIELD_NAMES = new HashSet<String>(
			Arrays.asList("class"));
	private static final Set<String> GLOBALLY_FORBIDDEN_STATIC_FIELD_NAMES = new HashSet<String>(
			Arrays.asList("class"));
	private static final Set<String> GLOBALLY_FORBIDDEN_METHOD_NAMES = new HashSet<String>(
			Arrays.asList("getClass"));
	private static final Set<String> GLOBALLY_FORBIDDEN_STATIC_METHOD_NAMES = new HashSet<String>(
			Arrays.asList("getClass"));

	private final Set<String> allowedPrefixes = new HashSet<String>();

	public PrefixSandboxShutterImpl(String... allowedPrefixes) {
		if (allowedPrefixes == null || allowedPrefixes.length == 0)
			throw new IllegalArgumentException();
		this.allowedPrefixes.addAll(Arrays.asList(allowedPrefixes));
	}

	public boolean allowClassAccess(Class<?> type) {
		boolean allowed = false;
		if (type != null) {
			for (String s : allowedPrefixes) {
				if (type.getCanonicalName().startsWith(s)) {
					allowed = true;
					break;
				}
			}
		}

		if (allowed) {
			LOGGER.info("Allow class access: type = {}",
					type.getCanonicalName());
			return true;
		} else {
			LOGGER.warn("Deny class access: type = {}", type.getCanonicalName());
			return false;
		}
	}

	public boolean allowFieldAccess(Class<?> type, Object instance,
			String fieldName) {
		if (!GLOBALLY_FORBIDDEN_FIELD_NAMES.contains(fieldName)) {
			LOGGER.info(
					"Allow field access: type = {}, fieldName={}, instance={}",
					new Object[] { type.getCanonicalName(), fieldName, instance });
			return true;
		} else {
			LOGGER.warn(
					"Deny field access: type = {}, fieldName={}, instance={}",
					new Object[] { type.getCanonicalName(), fieldName, instance });
			return false;
		}
	}

	public boolean allowMethodAccess(Class<?> type, Object instance,
			String methodName) {
		if (!GLOBALLY_FORBIDDEN_METHOD_NAMES.contains(methodName)) {
			LOGGER.info(
					"Allow method access: type = {}, methodName={}, instance={}",
					new Object[] { type.getCanonicalName(), methodName,
							instance });
			return true;
		} else {
			LOGGER.warn(
					"Deny method access: type={}, methodName={}, instance={}",
					new Object[] { type.getCanonicalName(), methodName,
							instance });
			return false;
		}
	}

	public boolean allowStaticFieldAccess(Class<?> type, String fieldName) {
		if (!GLOBALLY_FORBIDDEN_STATIC_FIELD_NAMES.contains(fieldName)) {
			LOGGER.info("Allow static field access: type = {}, fieldName={}",
					new Object[] { type.getCanonicalName(), fieldName });
			return true;
		} else {
			LOGGER.warn("Deny static field access: type = {}, fieldName={}",
					new Object[] { type.getCanonicalName(), fieldName });
			return false;
		}
	}

	public boolean allowStaticMethodAccess(Class<?> type, String methodName) {
		if (!GLOBALLY_FORBIDDEN_STATIC_METHOD_NAMES.contains(methodName)) {
			LOGGER.info("Allow static method access: type = {}, methodName={}",
					new Object[] { type.getCanonicalName(), methodName });
			return true;
		} else {
			LOGGER.warn("Deny static method access: type = {}, fieldName={}",
					new Object[] { type.getCanonicalName(), methodName });
			return false;
		}
	}

}
