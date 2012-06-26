package com.flozano.rhino.sandbox;

import java.util.HashMap;

public class Foo {

	public static final int VALUE = 1234;

	public static void doSomething() {
		// Do some operations with a class which cannot be used directly
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		map.put("1234", "1234");
		System.out.println("Hello world " + map.get("1234"));
	}

	public static void printSomething(int val) {
		System.out.println("Value is " + val);
	}
}
