package com.ustadmobile.port.gwt.client.application;

import java.io.IOException;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.xml.client.DOMException;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.port.gwt.client.test.TestInterface;
import com.ustadmobile.port.gwt.client.util.ReplaceWithThis;
import com.ustadmobile.port.gwt.xmlpull.XmlPullParserGWT;

public class PreviousTests {
	
	/**
     * Testing XML Parsing
     * @param messageXml
     * @param tag
     * @param attribute
     * @return
     */
    private String parseMessage(String messageXml, String tag, String attribute) {
    	  try {
    	    // parse the XML document into a DOM
    	    Document messageDom = XMLParser.parse(messageXml);

    	    // find the sender's display name in an attribute of the <from> tag
    	    Node tagNode = messageDom.getElementsByTagName(tag).item(0);
    	    String attributeValue = ((Element)tagNode).getAttribute(attribute);
    	    
    	    return attributeValue;
    	    
    	  } catch (DOMException e) {
    	    GWT.log("Could not parse XML document.");
    	  }
    	  
    	  return null;
    	}
    
	public void testMe(){
		JSONObject jsonObject = new JSONObject();
	    try {
			jsonObject.put("key", "value");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    GWT.log("Testing Hashtable");
	    String key = "key";
	    String value = "value";
	    
	    Hashtable<String, String> hashtableObj = new Hashtable<String, String>();
	    hashtableObj.put(key, value);
	    String hashTableValue = (String) hashtableObj.get(key);
	    GWT.log("GWT Hashtable key's value: " + hashTableValue);
	    GWT.log("Aloha");
	    
	    //Testing replace
	    GWT.log("Testing Deffered binding..");
	    ReplaceWithThis bindingTest = new ReplaceWithThis();
	    GWT.log(bindingTest.value);
	    
	    GWT.log("Testing GWT's XML");
	    String testXmlString = "<?xml version=\"1.0\" ?>"
	    		+ "<message>"
	    		+ 	"<header>"
	    		+ 		"<to displayName=\"Varuna\" address=\"varuna@ustadmobile.com\" />"
	    		+ 		"<from displayName=\"Mike\" address=\"mike@ustadmobile.com\" />"
	    		+ 		"<sent>2017-12-07T12:03:55Z</sent>"
	    		+ 		"<subject>Re: Hello</subject>"
	    		+ 	"</header>"
	    		+ 	"<body>Hello!</body>"
	    		+ "</message>";
	    
	    String attr = parseMessage(testXmlString, "from", "displayName");
	    GWT.log("XML Result: " + attr);
	    
	    GWT.log("");
	    GWT.log("GWT XmlPullParsing .. ");
	    testXmlString = "<feed><id>http://umcloud1.ustadmobile.com/opds/courseid/6CM</id>"
	    		+ "<link rel=\"start\" href=\"/opds/\" type=\"application/atom+xml;profile=opds-catalog;kind=navigation\"/>"
	    		+ "<link rel=\"self\" href=\"test-acquisition-task-feed.opds\" "
	    		+ "type=\"application/atom+xml;profile=opds-catalog;kind=acquisition\"/>"
	    		+ "<title>Skills for Success</title><updated>2015-02-09T21:00:57Z</updated><author>"
	    		+ "<name>Mike Test4</name><uri>/user/6</uri></author><!-- Module 1 : CV Writing -->"
	    		+ "<entry><title>CV Writing</title><dc:language>en</dc:language>"
	    		+ "<link rel=\"alternate\" type=\"application/atom+xml;type=entry;profile=opds-catalog\" "
	    		+ "hreflang=\"en\" href=\"/opds/202b10fe-b028-4b84-9b84-852aa123456a.xml\"/><link rel=\"alternate\" "
	    		+ "type=\"application/atom+xml;type=entry;profile=opds-catalog\" hreflang=\"fa\" "
	    		+ "href=\"/opds/202b10fe-b028-4b84-9b84-852aa123456b.xml\"/><link rel=\"alternate\" "
	    		+ "type=\"application/atom+xml;type=entry;profile=opds-catalog\" hreflang=\"ps\" "
	    		+ "href=\"/opds/202b10fe-b028-4b84-9b84-852aa123456c.xml\"/><link rel=\"related\" "
	    		+ "type=\"application/atom+xml;type=entry;profile=opds-catalog\" hreflang=\"en\" "
	    		+ "href=\"/opds/eb0476a2-b8b1-43e3-bb85-f0e51e143afe.xml\" title=\"Cover Letter Writing\"/>"
	    		+ "<link rel=\"related\" type=\"application/atom+xml;type=entry;profile=opds-catalog\" "
	    		+ "hreflang=\"en\" href=\"/opds/114f6e63-80f2-4d10-9a70-efa113eb9f65.xml\" title=\"Job Search Skills\"/>"
	    		+ "<link rel=\"related\" type=\"application/atom+xml;type=entry;profile=opds-catalog\" "
	    		+ "hreflang=\"en\" href=\"/opds/3ce0e992-050c-4fbf-90c9-4dcb2b82bc64.xml\" title=\"Job Interview Skills\"/>"
	    		+ "<link rel=\"http://opds-spec.org/acquisition\" href=\"1-cvwriting-en.epub\" "
	    		+ "type=\"application/epub+zip\"/><link rel=\"http://opds-spec.org/image/thumbnail\" "
	    		+ "type=\"image/png\" href=\"cvwriting-thumb.png\"/>"
	    		+ "<link rel=\"http://www.ustadmobile.com/ns/opds/cover-image\" type=\"image/jpg\" "
	    		+ "href=\"1-cvwriting-cover.jpg\"/><id>202b10fe-b028-4b84-9b84-852aa123456a</id>"
	    		+ "<updated>2015-02-09T18:58:12Z</updated><author><name>Varuna Singh</name><uri>/user/2</uri>"
	    		+ "</author><content type=\"text\">Learn how to write a professional CV </content></entry></feed>";
	    XmlPullParserGWT xmlObj = new XmlPullParserGWT(testXmlString);
	    
	    try {
	    	int evt;
	    	GWT.log("Outter depth: " + xmlObj.getDepth());
			while((evt = xmlObj.next()) != XmlPullParser.END_DOCUMENT){
				
				GWT.log("Parsing: going next..");
				GWT.log("In depth: " + xmlObj.getDepth());
				String tagName = "";
				if(evt == XmlPullParser.START_TAG){
					tagName = xmlObj.getName();
					GWT.log("START_TAG : " + tagName);
				}else if(evt == XmlPullParser.TEXT) {
					GWT.log("TEXT: \"" + xmlObj.getText() + "\"");
				}else if(evt == XmlPullParser.END_TAG) {
					tagName = xmlObj.getName();
					GWT.log("END_TAG: " + tagName);
				}else {
					GWT.log("Other event: " + evt);
				}
				if(tagName.equals("link")){
					String linkHref = xmlObj.getAttributeValue("", "href");
					GWT.log("Link's link: \"" + linkHref + "\"");
				}
				
			}
		} catch (XmlPullParserException | IOException e1) {
			e1.printStackTrace();
			GWT.log("Unable to loop thorugh XML.");
		}
	    
	    GWT.log("Testing lonely interfaces..");
	    TestInterface testInterface;
	    GWT.log("Works.");
	    
	    GWT.log("Testing core..");
	    
	    String serverURL = "https://umcloud1.ustadmobile.com:8086/syncendpoint/";
	    String username = "ram.narayan";
	    String password = "varuna";
	    
	    LoginController.authenticate(username, password, serverURL, new UmCallback() {
	        @Override
	        public void onSuccess(Object result) {
	            GWT.log("SUCCESS");
	        }

	        @Override
	        public void onFailure(Throwable exception) {
	            GWT.log("FAIL");
	        }
	    });
	}
	
	

}
