package com.flozano.rhino.sandbox;

import java.io.InputStreamReader;
import java.io.Reader;

public class Util {
	public static Reader loadJS(String name) {
		return new InputStreamReader(
				Util.class.getResourceAsStream(name));
	}
}
