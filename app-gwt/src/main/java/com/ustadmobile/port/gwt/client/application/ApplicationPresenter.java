package com.ustadmobile.port.gwt.client.application;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
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

    @Inject
    ApplicationPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealType.Root);
        
        this.placeManager = placeManager;
        
        //If you have, 
        //view.setUiHanders(this);
    }
    
    //Seperate out reveal
    
}
