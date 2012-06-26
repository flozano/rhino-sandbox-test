package com.flozano.rhino.sandbox;

import java.io.IOException;
import java.io.Reader;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

public class ScriptExecutorExample {

	@Test
	public void testItOK() throws IOException {
		ContextFactory.initGlobal(new SandboxContextFactory(
				new PrefixSandboxShutterImpl("java", "com.flozano")));
		// create and initialize Rhino Context
		Context cx = ContextFactory.getGlobal().enterContext();
		Scriptable prototype = cx.initStandardObjects();
		Scriptable topLevel = new ImporterTopLevel(cx);
		prototype.setParentScope(topLevel);
		Scriptable scope = cx.newObject(prototype);
		scope.setPrototype(prototype);
		Reader s = Util.loadJS("/sample1.js");
		cx.evaluateReader(scope, s, "sample1", 1, null);
		Assert.assertTrue(true);
	}
	
}
