package com.ustadmobile.port.gwt.client.application.catalog;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.port.gwt.client.application.ApplicationPresenter;
import com.ustadmobile.port.gwt.client.place.NameTokens;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.UiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
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
public class CatalogPresenterGWT 
  extends Presenter<CatalogPresenterGWT.MyView, CatalogPresenterGWT.MyProxy> {
	
	/**
     * This class extends the core controller/presenter and implements UiHandler (if present)
     * This class is extended by CatalogPresenterGWT's view interface so it indirectly 
     * also implements UiHandler (if present)
     * 
     * @author varuna
     *
     */
    public class CoreCatalogPresenterHandler 	extends CatalogPresenter {

		public CoreCatalogPresenterHandler(Object context, CatalogView aView) {
			super(context, aView);
			GWT.log("CoreCatalogPresenterHandler constructor. TODO.");
		}
    }
    
    //Core CatalogView's Presenter's View Interface
    interface MyView extends 	View, 
								CatalogView {
    }
    
    //A Presenter having a NameToken is a Place.
    //If we are not injecting, ProxyStandard will cause an error. 
    //@ProxyStandard
    @NameToken(NameTokens.CATALOG)
    @NoGatekeeper 
    interface MyProxy extends ProxyPlace<CatalogPresenterGWT> {
    }
    
    private PlaceManager placeManager;
    private CatalogPresenter mController;

    //Constructor with GWTP Inject 
    //This needs to be injected via a Module (eg: in ApplicationModule here)
    //  else GWTP will never call it. 
    @Inject
    CatalogPresenterGWT(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager) {
        super(eventBus, view, proxy, 
        		ApplicationPresenter.SLOT_CONTENT
        		);
        
        this.placeManager = placeManager;

        mController = new CoreCatalogPresenterHandler(placeManager, view);
        
        //If we are using UiHandlers:
        //getView().setUiHandlers((CoreCatalogPresenterHandler) mController);
        
    }

}
