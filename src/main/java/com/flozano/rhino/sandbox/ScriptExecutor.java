package com.flozano.rhino.sandbox;

import java.io.IOException;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

import com.flozano.rhino.sandbox.impl.DummySandboxShutterImpl;

public class ScriptExecutor {
	public static void main(String[] argz) throws IOException {
		ContextFactory.initGlobal(new SandboxContextFactory(
				new DummySandboxShutterImpl()));

		// create and initialize Rhino Context
		Context cx = Context.enter();
		Scriptable prototype = cx.initStandardObjects();
		Scriptable topLevel = new ImporterTopLevel(cx);
		prototype.setParentScope(topLevel);
		Scriptable scope = cx.newObject(prototype);
		scope.setPrototype(prototype);
		Reader s = Util.loadJS("/sample1.js");
		cx.evaluateReader(scope, s, "sample1", 1, null);
	}
}
