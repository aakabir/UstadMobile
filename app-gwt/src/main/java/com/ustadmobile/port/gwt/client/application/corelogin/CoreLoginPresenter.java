package com.ustadmobile.port.gwt.client.application.corelogin;

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
public class CoreLoginPresenter 
  extends Presenter<CoreLoginPresenter.MyView, CoreLoginPresenter.MyProxy> 
	implements CoreLoginUiHandlers{
	
	/**
     * This class extends the core controller/presenter and implements UiHandler
     * This class is extended by CoreLoginPresenters' view interface so it indirectly also implements UiHandler
     * 
     * @author varuna
     *
     */
    public class CoreLoginPresenterHandler 	extends com.ustadmobile.core.controller.LoginController 
    										implements UiHandlers {

		public CoreLoginPresenterHandler(Object context) {
			super(context);
			// TODO Auto-generated constructor stub
			GWT.log("CoreLoginPresenterHandler constructor. TODO.");
		}
    }
    
    //Core Login's Presenter's View Interface
    interface MyView extends 	View, 
								HasUiHandlers<CoreLoginPresenterHandler>, 
								com.ustadmobile.core.view.LoginView {
    }
    
    //A Presenter having a NameToken is a Place.
    @ProxyStandard
    @NameToken(NameTokens.CORELOGIN)
    @NoGatekeeper
    interface MyProxy extends ProxyPlace<CoreLoginPresenter> {
    }
    
    private String currentUser;
    private PlaceManager placeManager;
    private com.ustadmobile.core.controller.LoginController mController;

    //Constructor with GWTP Inject 
    @Inject
    CoreLoginPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager) {
        super(eventBus, view, proxy, 
        		ApplicationPresenter.SLOT_CORELOGIN
        		);
        
        this.placeManager = placeManager;
        //getView().setUiHandlers(this);
        
        mController = new CoreLoginPresenterHandler((CoreLoginView) view);
        
        //This will create the CoreLoginView object within the CoreLoginController's from its onCreate()
        //Args are never used anyway..
        //mController.onCreate(null, null);
        //mController.
        //TOOD
        getView().setUiHandlers((CoreLoginPresenterHandler) mController);
        
    }

	public void dummyHandler() {
		// TODO Auto-generated method stub
		
	}
}
