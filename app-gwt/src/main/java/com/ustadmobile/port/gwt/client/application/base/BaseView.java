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
package com.ustadmobile.port.gwt.client.application.base;

import java.util.Hashtable;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.ustadmobile.core.view.BasePointMenuItem;
import com.ustadmobile.port.gwt.client.application.base.BasePresenter.CoreBasePointPresenterHandler;

public class BaseView extends ViewWithUiHandlers<CoreBasePointPresenterHandler> 
  implements BasePresenter.MyView {
	
	/************ UI BINDER STUFF: *****************/
	
	//This is how GWTP knows to use the HomeView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, BaseView> {
    }   
    
    //This method initializes any DOM elements
    @Inject
    BaseView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @UiHandler("randomButton")
    void onConfirm(ClickEvent event) {
    	GWT.log("GWT:BaseView:onConfirm():UiHandler's updateText button handling..");
    	
    	
    }
    
    /************ CORE VIEW OVERRIDES: *****************/
    
    @Override
	public void setUiHandlers(CoreBasePointPresenterHandler uiHandlers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClassListVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMenuItems(BasePointMenuItem[] menuItems) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showShareAppDialog() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setShareAppDialogProgressVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dismissShareAppDialog() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTab(Hashtable tabArguments) {
		// TODO Auto-generated method stub
		
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
