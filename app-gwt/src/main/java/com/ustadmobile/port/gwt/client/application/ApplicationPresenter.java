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
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.port.gwt.client.test.TestInterface;

import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
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
    
    //The About page slot
    public static final NestedSlot SLOT_ABOUT = new NestedSlot();
    
    //The Presenter login slot
    public static final NestedSlot SLOT_LOGIN = new NestedSlot();
    
    //The Presenter core login slot
    public static final NestedSlot SLOT_CORELOGIN = new NestedSlot();
    
    //The Base page slot
    public static final NestedSlot SLOT_BASE = new NestedSlot();
    
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

        //Start BasePoint:
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.go(CoreBuildConfig.FIRST_DESTINATION, placeManager);
        
        //If you have, 
        //view.setUiHanders(this);
    }

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
    
    public void goToAbout(){
		// Navigate to the AboutPresenter
		 System.out.println("Going to about..");
		 PlaceRequest placeRequest = new PlaceRequest.Builder()
	                .nameToken(NameTokens.ABOUT)
	                .build();
		 placeManager.revealPlace(placeRequest);
    			 
    }
    
    public void goToCoreLogin(){
		// Navigate to the HomePresenter
		 System.out.println("Going to login..");
		 PlaceRequest placeRequest = new PlaceRequest.Builder()
	                .nameToken(NameTokens.CORELOGIN)
	                .build();
		 placeManager.revealPlace(placeRequest);
    			 
    }
    
    
    
}
