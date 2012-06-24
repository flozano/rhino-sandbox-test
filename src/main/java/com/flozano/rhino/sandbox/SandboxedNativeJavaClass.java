package com.flozano.rhino.sandbox;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.Scriptable;

class SandboxedNativeJavaClass extends NativeJavaClass {

	private static final long serialVersionUID = 1L;

	private final SandboxShutter shutter;
	private final Class<?> type;
	
	SandboxedNativeJavaClass(Scriptable obj, Class<?> clazz,
			SandboxShutter shutter) {
		super(obj, clazz);
		this.shutter = shutter;
		this.type = clazz;
	}

	@Override
	public Object get(String name, Scriptable start) {
		Object wrapped = super.get(name, start);

		if (wrapped instanceof BaseFunction) {
			if (!shutter.allowStaticMethodAccess(type, name)) {
				return NOT_FOUND;
			}
		} else {
			// NativeJavaObject + only boxed primitive types?
			if (!shutter.allowStaticFieldAccess(type, name)) {
				return NOT_FOUND;
			}
		}

		return wrapped;
	}

}
