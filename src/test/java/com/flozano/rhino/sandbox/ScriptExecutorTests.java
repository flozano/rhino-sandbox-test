package com.flozano.rhino.sandbox;

import java.io.IOException;
import java.io.Reader;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

public class ScriptExecutorTests {

	@BeforeClass
	public static void setUp() {
		ContextFactory.initGlobal(new SandboxContextFactory(
				new PrefixSandboxShutterImpl("java.lang", "java.io",
						"com.flozano")));
	}

	@Test
	public void testOK() throws IOException {
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

	@Test(expected=Throwable.class)
	public void testBadClass() throws IOException {
		// create and initialize Rhino Context
		Context cx = ContextFactory.getGlobal().enterContext();
		Scriptable prototype = cx.initStandardObjects();
		Scriptable topLevel = new ImporterTopLevel(cx);
		prototype.setParentScope(topLevel);
		Scriptable scope = cx.newObject(prototype);
		scope.setPrototype(prototype);
		Reader s = Util.loadJS("/sample2.js");
		cx.evaluateReader(scope, s, "sample2", 1, null);
		Assert.assertTrue(false);
	}
	

	
	@Test(expected=Throwable.class)
	public void testAccessToClass() throws IOException {
		// create and initialize Rhino Context
		Context cx = ContextFactory.getGlobal().enterContext();
		Scriptable prototype = cx.initStandardObjects();
		Scriptable topLevel = new ImporterTopLevel(cx);
		prototype.setParentScope(topLevel);
		Scriptable scope = cx.newObject(prototype);
		scope.setPrototype(prototype);
		Reader s = Util.loadJS("/sample3.js");
		cx.evaluateReader(scope, s, "sample3", 1, null);
		Assert.assertTrue(false);
	}
	
	// @Test(expected=Throwable.class)
	@Test
	public void testGoodClassExtendsBadClass() throws IOException {
		// create and initialize Rhino Context
		Context cx = ContextFactory.getGlobal().enterContext();
		Scriptable prototype = cx.initStandardObjects();
		Scriptable topLevel = new ImporterTopLevel(cx);
		prototype.setParentScope(topLevel);
		Scriptable scope = cx.newObject(prototype);
		scope.setPrototype(prototype);
		Reader s = Util.loadJS("/sample4.js");
		cx.evaluateReader(scope, s, "sample4", 1, null);
		Assert.assertTrue(true);	}
}
