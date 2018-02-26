package com.ustadmobile.port.gwt.client.application.upload;


import com.ustadmobile.port.gwt.client.application.ApplicationPresenter;
import com.ustadmobile.port.gwt.client.application.base.BasePresenter;
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
public class UploadCoursePresenter 
  extends Presenter<UploadCoursePresenter.MyView, UploadCoursePresenter.MyProxy> 
	implements UploadCourseUiHandlers{
	
	
    //Upload Course Presenter's View Interface
    interface MyView extends 	View, 
								HasUiHandlers<UploadCourseUiHandlers> 
								//any View to extend goes here 
								{
    }
    
    //A Presenter having a NameToken is a Place.
    @ProxyStandard
    @NameToken(NameTokens.COURSE_UPLOAD)
    @NoGatekeeper
    interface MyProxy extends ProxyPlace<UploadCoursePresenter> {
    }
    
    private String currentUser;
    private PlaceManager placeManager;
    //private ???? mController;

    //Constructor with GWTP Inject 
    @Inject
    UploadCoursePresenter(EventBus eventBus, 
    		MyView view, 
    		MyProxy proxy, 
    		PlaceManager placeManager) {
        super(eventBus, view, proxy, 
        		ApplicationPresenter.SLOT_CONTENT
        		);
        
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
        
    }
    
	@Override
	public void dummyHandler() {
		GWT.log("Alora!");
	}
}
