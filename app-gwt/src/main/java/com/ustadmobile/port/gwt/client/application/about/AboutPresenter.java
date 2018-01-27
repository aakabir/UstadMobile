package com.ustadmobile.port.gwt.client.application.about;


import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.port.gwt.client.application.ApplicationPresenter;
import com.ustadmobile.port.gwt.client.place.NameTokens;

import java.util.Hashtable;

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
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;


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
public class AboutPresenter 
  extends Presenter<AboutPresenter.MyView, AboutPresenter.MyProxy> 
	implements AboutUiHandlers{
	
	/**
     * This class extends the core controller/presenter and implements UiHandler
     * This class is extended by AboutPresenters' view interface so it indirectly also implements UiHandler
     * 
     * @author varuna
     *
     */
    public class CoreAboutPresenterHandler 	extends com.ustadmobile.core.controller.AboutController 
    										implements UiHandlers {

		public CoreAboutPresenterHandler(Object context, AboutView view) {
			super(context, view);
			// TODO Auto-generated constructor stub
			GWT.log("CoreAboutPresenter constructor. TODO.");
		}
    }
    
    //About Presenter's View Interface
    interface MyView extends 	View, 
								HasUiHandlers<CoreAboutPresenterHandler>, 
								com.ustadmobile.core.view.AboutView {
    }
    
    //A Presenter having a NameToken is a Place.
    @ProxyStandard
    @NameToken(NameTokens.ABOUT)
    @NoGatekeeper
    interface MyProxy extends ProxyPlace<AboutPresenter> {
    }
    
    private String currentUser;
    private PlaceManager placeManager;
    private com.ustadmobile.core.controller.AboutController mController;

    //Constructor with GWTP Inject 
    @Inject
    AboutPresenter(EventBus eventBus, 
    		MyView view, 
    		MyProxy proxy, 
    		PlaceManager placeManager) {
        super(eventBus, view, proxy, 
        		//ApplicationPresenter.SLOT_ABOUT
        		ApplicationPresenter.SLOT_CONTENT
        		);
        
        this.placeManager = placeManager;
        //getView().setUiHandlers(this);
        
        mController = new CoreAboutPresenterHandler(placeManager, (AboutView) view);
        
        //This will create the AboutView object within the AboutController's from its onCreate()
        //Args are never used anyway..
        mController.onCreate(null, null);
        getView().setUiHandlers((CoreAboutPresenterHandler) mController);
        
    }
    
	@Override
	public void dummyHandler() {
		GWT.log("Alora!");
	}
}
