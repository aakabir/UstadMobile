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

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

//import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class LoginView extends ViewWithUiHandlers<LoginUiHandlers> 
  implements LoginPresenter.MyView {
	
	//This is how GWTP knows to use the HomeView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, LoginView> {
    }

    @UiField
    Button confirm;
    @UiField
    TextBox username;
    @UiField
    PasswordTextBox password;
    
    
    @Inject
    LoginView(Binder uiBinder) {
    	GWT.log("LoginView()");
    	//This method initialises any DOM elements
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @UiHandler("confirm")
    void onConfirm(ClickEvent event) {
    	System.out.println("Confirming..");
        getUiHandlers().confirm(username.getText(), password.getText());
    }
    
    @UiHandler("about")
    void onAbout(ClickEvent event){
    	GWT.log("LoginView:onAbout(): About button clicked..Calling Override in LoginPresenter via UiHandler()..");
    	getUiHandlers().aboutClicked();
    }
    
    public void doNothing(){
    	GWT.log("hi");
    }
}
