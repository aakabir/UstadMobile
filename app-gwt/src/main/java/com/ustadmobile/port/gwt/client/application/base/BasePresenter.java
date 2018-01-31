package com.ustadmobile.port.gwt.client.application.base;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.port.gwt.client.application.ApplicationPresenter;
import com.ustadmobile.port.gwt.client.application.about.AboutPresenter;
import com.ustadmobile.port.gwt.client.place.NameTokens;

import java.util.Iterator;
import java.util.Set;

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
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
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
    	//To re-calcuate Tabs:
    	 void recalculateTabs();
    }
    
    //A Presenter having a NameToken is a Place.
    @ProxyStandard
    @NameToken(NameTokens.BASE)
    @NoGatekeeper
    interface MyProxy extends ProxyPlace<BasePresenter> {
    }
    
    private String currentUser;
    private PlaceManager placeManager;
    private Hashtable args;
    private com.ustadmobile.core.controller.BasePointController mController;
    
    private AboutPresenter aboutPresenter;
    
    //Main Tab Content slot
    public static final NestedSlot SLOT_TAB = new NestedSlot();

    //Constructor with GWTP Inject 
    @Inject
    BasePresenter(EventBus eventBus, 
    		MyView view, 
    		MyProxy proxy, 
    		PlaceManager placeManager) {
        super(eventBus, view, proxy, 
        		//ApplicationPresenter.SLOT_BASE
        		ApplicationPresenter.SLOT_CONTENT
        		);
        
        this.placeManager = placeManager;
        //getView().setUiHandlers(this);
        
        mController = new CoreBasePointPresenterHandler(placeManager, view);
        
        getView().setUiHandlers((CoreBasePointPresenterHandler) mController);
        
    }
    
    @Override
	protected void onBind() {
		// TODO Auto-generated method stub
		//super.onBind();
    	if(aboutPresenter == null){
			GWT.log("Presenter not set");
    	}
		setInSlot(SLOT_TAB, aboutPresenter);
	}    
    
    
    
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		GWT.log("BasePresenter:prepareFromRequest()");
		super.prepareFromRequest(request);
		Set<String> requestArgNames = request.getParameterNames();
		this.args = new Hashtable();
		Iterator<String> it = requestArgNames.iterator();
		while(it.hasNext()){
			String key = it.next();
			this.args.put(key, request.getParameter(key, ""));
		}
		GWT.log("BasePresenter: Argument creation done.");
		mController.onCreate(args, null);
	}



	@Override
	public void baseDummyHandler() {
		GWT.log("baseDummyHandler() : Alora!");
	}
}
