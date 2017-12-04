package com.ustadmobile.port.gwt.client.application;

///*
import org.json.JSONException;
import org.json.JSONObject;
//*/

import java.util.Hashtable;

//testing deferred binding
import com.ustadmobile.port.gwt.client.util.ReplaceWithThis;
import com.google.gwt.core.client.GWT;
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
    //private static final PopupImpl impl = GWT.create(PopupImpl.class);
    //private static ReplaceWithThis bindingTest = GWT.create(ReplaceWithThis.class);
    

    ///*
    @Inject
    ApplicationPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealType.Root);
        
        this.placeManager = placeManager;
        
        GWT.log("ApplicationPresenter");
        //Testing this
        ///*
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
		//*/
        
        //Testing replace
        ReplaceWithThis bindingTest = new ReplaceWithThis();
        GWT.log(bindingTest.value);
        
        //If you have, 
        //view.setUiHanders(this);
    }
    //*/
    
    
    /*
    //Just trying:
    @Inject
    ApplicationPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            PlaceManager placeManager,
            ContactsWidgetPresenter contactsWidgetPresenter) {
        super(eventBus, view, proxy, RevealType.Root);
        
        this.placeManager = placeManager;
        
        //If you have, 
        //view.setUiHanders(this);
    }
    */
    
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
    
}
