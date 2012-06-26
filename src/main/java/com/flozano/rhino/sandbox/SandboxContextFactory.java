package com.flozano.rhino.sandbox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;

public class SandboxContextFactory extends ContextFactory {
	private final SandboxShutter shutter;

	public SandboxContextFactory(SandboxShutter shutter) {
		this.shutter = shutter;
	}

	@Override
	protected Context makeContext() {
		Context cx = super.makeContext();
		cx.setWrapFactory(new SandboxWrapFactory());
		cx.setClassShutter(new ClassShutter() {
			private final Map<String, Boolean> nameToAccepted = new HashMap<String, Boolean>();

			// @Override
			public boolean visibleToScripts(String name) {
				Boolean granted = this.nameToAccepted.get(name);

				if (granted != null) {
					return granted.booleanValue();
				}

				Class<?> staticType;
				try {
					staticType = Class.forName(name);
				} catch (Exception exc) {
					this.nameToAccepted.put(name, Boolean.FALSE);
					return false;
				}

				boolean grant = shutter.allowClassAccess(staticType);
				this.nameToAccepted.put(name, Boolean.valueOf(grant));
				return grant;
			}
		});
		return cx;
	}

	class SandboxWrapFactory extends WrapFactory {

		@Override
		public Scriptable wrapJavaClass(Context cx, Scriptable scope,
				@SuppressWarnings("rawtypes") final Class javaClass) {
			return new NativeJavaClass(scope, javaClass) {
				private static final long serialVersionUID = 1L;

				@Override
				public Object get(String name, Scriptable start) {
					Object wrapped = super.get(name, start);

					if (wrapped instanceof BaseFunction) {
						if (!shutter.allowStaticMethodAccess(javaClass, name)) {
							return NOT_FOUND;
						}
					} else {
						// NativeJavaObject + only boxed primitive types?
						if (!shutter.allowStaticFieldAccess(javaClass, name)) {
							return NOT_FOUND;
						}
					}

					return wrapped;
				}
			};
			// return super.wrapJavaClass(cx, scope, replaced);
		}

		@Override
		public Scriptable wrapNewObject(Context cx, Scriptable scope, Object obj) {
			this.ensureReplacedClass(scope, obj, null);

			return super.wrapNewObject(cx, scope, obj);
		}

		@Override
		public Object wrap(Context cx, Scriptable scope, Object obj,
				Class<?> staticType) {
			this.ensureReplacedClass(scope, obj, staticType);

			return super.wrap(cx, scope, obj, staticType);
		}

		@Override
		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
				Object javaObject, Class<?> staticType) {
			final Class<?> type = this.ensureReplacedClass(scope, javaObject,
					staticType);

			return new NativeJavaObject(scope, javaObject, staticType) {
				private static final long serialVersionUID = 1L;

				private final Map<String, Boolean> instanceMethodToAllowed = new HashMap<String, Boolean>();

				@Override
				public Object get(String name, Scriptable scope) {
					Object wrapped = super.get(name, scope);

					if (wrapped instanceof BaseFunction) {
						String id = type.getName() + "." + name;
						Boolean allowed = this.instanceMethodToAllowed.get(id);

						if (allowed == null) {
							boolean allow = shutter.allowMethodAccess(type,
									javaObject, name);
							this.instanceMethodToAllowed.put(id,
									allowed = Boolean.valueOf(allow));
						}

						if (!allowed.booleanValue()) {
							return NOT_FOUND;
						}
					} else {
						// NativeJavaObject + only boxed primitive types?
						if (!shutter.allowFieldAccess(type, javaObject, name)) {
							return NOT_FOUND;
						}
					}

					return wrapped;
				}
			};
		}

		//

		private final Set<Class<?>> replacedClasses = new HashSet<Class<?>>();

		private Class<?> ensureReplacedClass(Scriptable scope, Object obj,
				Class<?> staticType) {
			final Class<?> type = (staticType == null && obj != null) ? obj
					.getClass() : staticType;

			if (!type.isPrimitive() && !type.getName().startsWith("java.")
					&& this.replacedClasses.add(type)) {
				this.replaceJavaNativeClass(type, scope);
			}

			return type;
		}

		private void replaceJavaNativeClass(final Class<?> type,
				Scriptable scope) {
			Object clazz = Context.jsToJava(
					ScriptableObject.getProperty(scope, "Packages"),
					Object.class);
			Object holder = null;
			String[] parts = type.getName().split("\\.");
			for (String part : parts/* Text.split(type.getName(), '.') */) {
				holder = clazz;
				clazz = ScriptableObject.getProperty((Scriptable) clazz, part);
			}
			if (!(clazz instanceof NativeJavaClass)) {
				return;
			}

			NativeJavaClass nativeClass = (NativeJavaClass) clazz;

			nativeClass = new NativeJavaClass(scope, type) {
				private static final long serialVersionUID = 1L;

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
			};

			ScriptableObject.putProperty((Scriptable) holder,
					type.getSimpleName(), nativeClass);
			ScriptableObject.putProperty(scope, type.getSimpleName(),
					nativeClass);
		}
	}
}
