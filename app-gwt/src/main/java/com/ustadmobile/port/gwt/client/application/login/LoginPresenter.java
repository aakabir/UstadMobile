/*
 * #%L
 * GwtMaterial
 * %%
 * Copyright (C) 2015 - 2017 GwtMaterialDesign
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.ustadmobile.port.gwt.client.application.login;

//import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.port.gwt.client.application.ApplicationPresenter;
import com.ustadmobile.port.gwt.client.place.NameTokens;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
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
public class LoginPresenter 
  extends Presenter<LoginPresenter.MyView, LoginPresenter.MyProxy> 
	implements LoginUiHandlers{
	
	
    interface MyView extends View, HasUiHandlers<LoginUiHandlers> {
    }

    private String currentUser;
    private PlaceManager placeManager;
    
    @ProxyStandard
    @NameToken(NameTokens.LOGIN)
    @NoGatekeeper
    interface MyProxy extends ProxyPlace<LoginPresenter> {
    }
    //A Presenter having a NameToken is a Place.

    @Inject
    LoginPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy
            //,String currentUser) {
            , PlaceManager placeManager
    		) {
        super(eventBus, view, proxy, 
        		ApplicationPresenter.SLOT_LOGIN
        		);
        
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
        
    }
    
    
	@Override
	public void confirm(String username, String password) {
		//Validate..
		 if (validateCredentials(username, password)) {
			 //Do something.
			 currentUser = username;
		 
			 //  Navigate to the HomePresenter
			 System.out.println("Going home..");
			 PlaceRequest placeRequest = new PlaceRequest.Builder()
		                .nameToken(NameTokens.HOME)
		                .build();
			 placeManager.revealPlace(placeRequest);
			 
		 }else{
			 System.out.println("Username and Password incorrect.");
			 //show alert?
		 }
		
	}
	
	private boolean validateCredentials(String username, String password) {
        return username.equals("test") && password.equals("test");
    }


	@Override
	public void aboutClicked() {
		GWT.log("LoginPresenter:aboutClicked(): Going to About page.");
		//  Navigate to the AboutPresenter
		 System.out.println("Going home..");
		 PlaceRequest placeRequest = new PlaceRequest.Builder()
	                .nameToken(NameTokens.ABOUT)
	                .build();
		 placeManager.revealPlace(placeRequest);
		
	}
}
