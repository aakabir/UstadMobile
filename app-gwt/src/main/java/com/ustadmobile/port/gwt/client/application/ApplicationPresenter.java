package com.ustadmobile.port.gwt.client.application;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

/**
 * This is the top level presenter of the hierarchy ApplicationPresenter. 
 * Other presenters reveal themselves within this Presenter. 
 * 
 * This is triggered from ClientModule where GWTP "Injects" something to start this.
 * 
 * Each GWTP Presenter has to extend: 
 * 	Presenter<..that takes in.. View and Proxy interfaces for some reason> 
 * 
 * @author varuna
 *
 */
public class ApplicationPresenter
        extends Presenter<ApplicationPresenter.MyView, ApplicationPresenter.MyProxy> {
	
	/**
	 * A PlaceManager is used to go between Places. 
	 * A place manager "reveals" a place via a PlaceRequest. 
	 * 	A place request is created from the NameTokens as such:
	 * eg:
	 * Building a place request:
	 	PlaceRequest placeRequest = new PlaceRequest.Builder()
	                .nameToken(NameTokens.LOGIN)
	                .build();
	 * Using the PlaceManager to go to the place mentioned in the place request. 
		 placeManager.revealPlace(placeRequest); 
		 
	 * How is the NameToken mapped to a particular Module/Presenter/View? 
	 * 	In The Module's Presenter's Proxy Interface with the Annotation: 
	 * 	@NameToken(NameTokens.ABOUT). 
	 */
	private PlaceManager placeManager;
	
	//This Presenter's View Interface
    interface MyView extends View {
    }
    
    //This is the Presenter's Proxy Interface. 
    @ProxyStandard
    interface MyProxy extends Proxy<ApplicationPresenter> {
    }
    
    //Main Content slot
    public static final NestedSlot SLOT_CONTENT = new NestedSlot();
    
    //TODO: Add logger functionality
    //public static Logger logger = Logger.getLogger("NameOfYourLogger");

    /**
     * The Main Application's Presenter's Constructor is the first
     *  thing initialised in GWTP. 
     * @param eventBus
     * @param view
     * @param proxy
     * @param placeManager
     */
    @Inject
    ApplicationPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealType.Root);
        
        GWT.log("ApplicationPresenter()");
        //Setting the placeManager so it can be used throughout 
        // the lifecycle of the application.
        this.placeManager = placeManager;
        
        //Start based on startUI: This starts the remaining UstadMobile bits. 
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        impl.startUI(placeManager);
        
        //If you have, set view's UI Handler like: view.setUiHanders(this);
    }

}
