
package com.ustadmobile.port.gwt.client;

import com.google.gwt.junit.client.GWTTestCase;

public class SimpleGwtTest extends GWTTestCase {


	  @Override
	  public String getModuleName() {
	    return "com.ustadmobile.port.gwt.gwtapp";
	  }
	 
	  // Tests must start with 'test'
	   
	  public void testUpperCasingLabel() {
		  assertEquals("/", "/");
	  }

}
