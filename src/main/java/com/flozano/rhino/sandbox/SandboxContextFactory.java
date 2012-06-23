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
		final Context cx = super.makeContext();
		cx.setWrapFactory(new SandboxWrapFactory());
		final ClassShutter s = new ClassShutter() {
			private final Map<String, Boolean> nameToAccepted = new HashMap<String, Boolean>();

			public boolean visibleToScripts(String fullClassName) {
				Boolean granted = this.nameToAccepted.get(fullClassName);

				if (granted != null) {
					return granted.booleanValue();
				}

				Class<?> staticType;
				try {
					staticType = Class.forName(fullClassName);
				} catch (Exception exc) {
					this.nameToAccepted.put(fullClassName, Boolean.FALSE);
					return false;
				}

				boolean grant = shutter.allowClassAccess(staticType);
				this.nameToAccepted.put(fullClassName, Boolean.valueOf(grant));
				return grant;
			}
		};
		cx.setClassShutter(s);
		return cx;
	}

	class SandboxWrapFactory extends WrapFactory {

		@Override
		public Scriptable wrapJavaClass(Context cx, Scriptable scope,
				@SuppressWarnings("rawtypes") Class javaClass) {
			this.ensureReplacedClass(scope, null, javaClass);
			return super.wrapJavaClass(cx, scope, javaClass);
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
			for (String part : type.getName().split(type.getName(), '.')) {
				holder = clazz;
				clazz = ScriptableObject.getProperty((Scriptable) clazz, part);
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