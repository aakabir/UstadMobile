package com.ustadmobile.port.gwt.client.application.home;

import com.ustadmobile.port.gwt.client.application.ApplicationPresenter;
import com.ustadmobile.port.gwt.client.place.NameTokens;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * This is the child presenter of Application Presenter.
 * It uses its parent's presenter's (ApplicationPresenter) Slot to 
 *  reveal itself.
 *  
 *  extends Presenter<HomePresenter.MyView, ...  <--This defines the
 *  ApplicationPresenter superclass. Those interfaces need to be defined
 *  into the class.
 * @author varuna
 *
 */
public class HomePresenter 
extends Presenter<HomePresenter.MyView, HomePresenter.MyProxy> {
	
	//Home Presenter's View 	
    interface MyView extends View {
    }

    @NameToken(NameTokens.HOME)
    @ProxyStandard
    interface MyProxy extends ProxyPlace<HomePresenter> {
    }
    //A Presenter having a NameToken is a Place.

    PlaceManager placeManager; 
    
    @Inject
    HomePresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy
            //,PlaceManager placeManager) {
    		){
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_MAIN);
        //this.placeManager = placeManager;
        
    }
}
