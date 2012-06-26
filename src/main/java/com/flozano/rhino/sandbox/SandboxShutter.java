package com.flozano.rhino.sandbox;

public interface SandboxShutter {
	/**
	 * Control access to a given class
	 * 
	 * @param type
	 * @return
	 */
	public boolean allowClassAccess(Class<?> type);

	/**
	 * Control access to a given field in an instance
	 * 
	 * @param type
	 * @param instance
	 * @param fieldName
	 * @return
	 */
	public boolean allowFieldAccess(Class<?> type, Object instance,
			String fieldName);

	/**
	 * Control access to a given method in an instance
	 * 
	 * @param type
	 * @param instance
	 * @param methodName
	 * @return
	 */
	public boolean allowMethodAccess(Class<?> type, Object instance,
			String methodName);

	/**
	 * Control access to an static field of a given class
	 * 
	 * @param type
	 * @param fieldName
	 * @return
	 */
	public boolean allowStaticFieldAccess(Class<?> type, String fieldName);

	/**
	 * Control access to an static method of a given class
	 * 
	 * @param type
	 * @param methodName
	 * @return
	 */
	public boolean allowStaticMethodAccess(Class<?> type, String methodName);

}
