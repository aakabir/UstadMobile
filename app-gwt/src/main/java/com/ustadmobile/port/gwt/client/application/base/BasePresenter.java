package com.ustadmobile.port.gwt.client.application.base;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.view.BasePointView;
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
 *  
 *  BasePresenter for BasePointController
 * @author varuna
 *
 */
public class BasePresenter 
  extends Presenter<BasePresenter.MyView, BasePresenter.MyProxy> 
	implements BaseUiHandlers{
	
	/**
     * This class extends the core controller/presenter and implements UiHandler
     * This class is extended by BasePresenter' view interface so it indirectly also implements UiHandler
     * 
     * @author varuna
     *
     */
    public class CoreBasePointPresenterHandler 	extends com.ustadmobile.core.controller.BasePointController 
    										implements UiHandlers {

		public CoreBasePointPresenterHandler(Object context, BasePointView view) {
			super(context, view);
			// TODO Auto-generated constructor stub
			GWT.log("CoreBasePointPresenterHandler constructor. TODO.");
		}
		
    }
    
    //Base Presenter's View Interface
    interface MyView extends 	View, 
								HasUiHandlers<CoreBasePointPresenterHandler>, 
								com.ustadmobile.core.view.BasePointView {
    }
    
    //A Presenter having a NameToken is a Place.
    @ProxyStandard
    @NameToken(NameTokens.BASE)
    @NoGatekeeper
    interface MyProxy extends ProxyPlace<BasePresenter> {
    }
    
    private String currentUser;
    private PlaceManager placeManager;
    private com.ustadmobile.core.controller.BasePointController mController;

    //Constructor with GWTP Inject 
    @Inject
    BasePresenter(EventBus eventBus, 
    		MyView view, 
    		MyProxy proxy, 
    		PlaceManager placeManager) {
        super(eventBus, view, proxy, 
        		ApplicationPresenter.SLOT_BASE
        		);
        
        this.placeManager = placeManager;
        //getView().setUiHandlers(this);
        
        mController = new CoreBasePointPresenterHandler(placeManager, view);
        
        //This will create the BasePointView (or BaseView?) object within the BasePointController's from its onCreate()
    	//Needs to be called from go's :TODO
        //go(CoreBuildConfig.FIRST_DESTINATION, context);
        //mController.onCreate(null, null);
        getView().setUiHandlers((CoreBasePointPresenterHandler) mController);
        
    }
    
	@Override
	public void baseDummyHandler() {
		GWT.log("baseDummyHandler() : Alora!");
	}
}
