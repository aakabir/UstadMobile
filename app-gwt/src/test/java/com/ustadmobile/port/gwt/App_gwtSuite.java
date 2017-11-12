package com.ustadmobile.port.gwt;

import com.ustadmobile.port.gwt.client.App_gwtTest;
import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

public class App_gwtSuite extends GWTTestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for App_gwt");
		suite.addTestSuite(App_gwtTest.class);
		return suite;
	}
}
