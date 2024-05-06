package com.bgfang;

import java.lang.instrument.Instrumentation;

/**
 * 
 * @author hengyunabc 2020-07-28
 *
 */
public class DemoAgent {

	public static void premain(String args, Instrumentation inst) {
		init(true, args, inst);
	}

	public static void agentmain(String args, Instrumentation inst) {
		init(false, args, inst);
	}

	public static synchronized void init(boolean premain, String args, Instrumentation inst) {
		System.out.println("DemoAgent started.");
	}

}
