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
package com.ustadmobile.port.gwt.client.application.about;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.port.gwt.client.application.about.AboutPresenter.CoreAboutPresenterHandler;

public class AboutView extends ViewWithUiHandlers<CoreAboutPresenterHandler> 
  implements AboutPresenter.MyView {
	
	/************ UI BINDER STUFF: *****************/
	
	//This is how GWTP knows to use the HomeView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, AboutView> {
    }
    
    @UiField
    TextBox textBox;
    
    @UiField
    TextArea version;
    
    //This method initializes any DOM elements
    @Inject
    AboutView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @UiHandler("updateText")
    void onConfirm(ClickEvent event) {
    	GWT.log("GWT:AboutView:onConfirm():UiHandler's updateText button handling..");
    	String newText = "Html updated from click in GWT's View";    	
    	setAboutHTML(newText);
    	
    	//Testing getView 
    	com.ustadmobile.core.view.AboutView coreView = 
    			(com.ustadmobile.core.view.AboutView) this.getUiHandlers().getView();
    	//coreView.setAboutHTML(newText); //Update: Did not work.
    	
    }
    
    /************ CORE VIEW OVERRIDES: *****************/

	@Override
	public void setVersionInfo(String versionInfo) {
		version.setText(versionInfo);
	}

	@Override
	public void setAboutHTML(String aboutHTML) {
		GWT.log("GWT:AboutView:setAboutHTML():.. ");
		textBox.setText(aboutHTML);
		//TOOD: REFRESH VIEW ? 
		
	}

	/********* CORE's USTADVIEW OVERRIDE CAN BE PART OF USTADVIEW IN GWT ***************/
	
	@Override
	public Object getContext() {
		// TODO Check this
		//return null;
		GWT.log("GWT:AboutView:getContext() NOT TESTED!");
		return this;
	}

	@Override
	public int getDirection() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDirection(int dir) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAppMenuCommands(String[] labels, int[] ids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUIStrings() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runOnUiThread(Runnable r) {
		// TODO Auto-generated method stub
		
	}   
   
}
