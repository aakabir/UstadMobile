package com.ustadmobile.port.gwt.client.application;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Hashtable;

//testing deferred binding
import com.ustadmobile.port.gwt.client.util.ReplaceWithThis;
import com.ustadmobile.port.gwt.xmlpull.XmlPullParserGWT;
import com.google.gwt.core.client.GWT;
import com.google.gwt.xml.client.DOMException;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.ustadmobile.port.gwt.client.place.NameTokens;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;

//import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.test.TestThis;

/**
 * This is the top level presenter of the hierarchy ApplicationPresenter. 
 * Other presenters reveal themselves within this Presenter. 
 * 
 * @author varuna
 *
 */
public class ApplicationPresenter
        extends Presenter<ApplicationPresenter.MyView, ApplicationPresenter.MyProxy> {
	
	//The Presenter's View
    interface MyView extends View {
    }
    
    private PlaceManager placeManager;

    @ProxyStandard
    interface MyProxy extends Proxy<ApplicationPresenter> {
    }

    //The Presenter main slot
    public static final NestedSlot SLOT_MAIN = new NestedSlot();
    
    //The Presenter login slot
    public static final NestedSlot SLOT_LOGIN = new NestedSlot();
    
    //public static Logger logger = Logger.getLogger("NameOfYourLogger");
    
    //like so:
    //private static ReplaceWithThis bindingTest = GWT.create(ReplaceWithThis.class);

    @Inject
    ApplicationPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealType.Root);
        
        this.placeManager = placeManager;
        
        GWT.log("ApplicationPresenter");

        JSONObject jsonObject = new JSONObject();
        try {
			jsonObject.put("key", "value");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        String key = "key";
        String value = "value";
        
        Hashtable hashtableObj = new Hashtable();
        hashtableObj.put(key, value);
        String hashTableValue = (String) hashtableObj.get(key);
        GWT.log("GWT Hashtable key's value: " + hashTableValue);
        GWT.log("Aloha");
        
        //Testing replace
        ReplaceWithThis bindingTest = new ReplaceWithThis();
        GWT.log(bindingTest.value);
        
        //GWT.log("Testing some core stuff..");
        
        //UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        //TestThis testThis = new TestThis();
        //GWT.log("This value is from core:" + testThis.value);
        //GWT.log("Implemented.");
        
        //GWT.log("Testing GWT's XML");
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
        //GWT.log("XML Result: " + attr);
        
        
        GWT.log("");
        GWT.log("GWT XmlPullParsing .. ");
        XmlPullParserGWT xmlObj = new XmlPullParserGWT(testXmlString);
        
        try {
        	int evt;
			while((evt = xmlObj.next()) != XmlPullParser.END_DOCUMENT){
				GWT.log("Parsing: going next..");
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
				
			}
		} catch (XmlPullParserException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			GWT.log("Unable to loop thorugh XML.");
		}
        
        
        //If you have, 
        //view.setUiHanders(this);
    }

  
    
    //Seperate out reveal
    
    
    public void goToHome(){
    	// Navigate to the HomePresenter
    	System.out.println("Going home..");
    	PlaceRequest placeRequest = new PlaceRequest.Builder()
	            .nameToken(NameTokens.HOME)
	            .build();
    	placeManager.revealPlace(placeRequest);			 
    }
    
    public void goToLogin(){
		// Navigate to the HomePresenter
		 System.out.println("Going to login..");
		 PlaceRequest placeRequest = new PlaceRequest.Builder()
	                .nameToken(NameTokens.LOGIN)
	                .build();
		 placeManager.revealPlace(placeRequest);
    			 
    }
    
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
    
}
