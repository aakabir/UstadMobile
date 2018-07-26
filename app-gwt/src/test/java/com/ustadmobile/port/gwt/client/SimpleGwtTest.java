package com.ustadmobile.port.gwt.client;

import com.google.gwt.junit.client.GWTTestCase;

public class SimpleGwtTest extends GWTTestCase {

	  /**
	   * Specifies a module to use when running this test case. The returned
	   * module must include the source for this class.
	   * 
	   * @see com.google.gwt.junit.client.GWTTestCase#getModuleName()
	   */
	  @Override
	  public String getModuleName() {
	    return "com.ustadmobile.port.gwt.gwtapp";
	  }
	 
	  /**
	   * Tests must start with 'test'
	   */
	  
	  
	  public void testUpperCasingLabel() {
		  assertEquals("/", "/");
	  }

}
