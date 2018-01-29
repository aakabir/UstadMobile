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

import java.util.ArrayList;
import java.util.Hashtable;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.ustadmobile.core.view.BasePointMenuItem;
import com.ustadmobile.port.gwt.client.application.base.BasePresenter.CoreBasePointPresenterHandler;

import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.WavesType;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialRow;
import gwt.material.design.client.ui.MaterialTab;
import gwt.material.design.client.ui.MaterialTabItem;

public class BaseView extends ViewWithUiHandlers<CoreBasePointPresenterHandler> 
  implements BasePresenter.MyView {
	
	private ArrayList<Hashtable> tabArgumentsList;
	
	//private int[] tabIconsIds = new int[]{R.drawable.selector_tab_resources,
    //        R.drawable.selector_tab_classes};
	
	private int[] tabIconsIds = new int[]{0, 0};
	protected boolean classListVisible;
	private static final int BASEPOINT_MENU_CMD_ID_OFFSET = 5000;
	private int tabIndex = 0; //++ when new tab added.
	
	
	@UiField
	MaterialTab tab;
	
	@UiField
	MaterialRow dynamicTabsRow;
	
	/************ UI BINDER STUFF: *****************/
	
	//This is how GWTP knows to use the HomeView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, BaseView> {
    }   
    
    //This method initializes any DOM elements
    @Inject
    BaseView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        tabArgumentsList = new ArrayList<>();
    }
    
    /*
    @UiHandler("randomButton")
    void onConfirm(ClickEvent event) {
    	GWT.log("GWT:BaseView:onConfirm():UiHandler's updateText button handling..");
    	
    }
    */
    
    @Override
	public void setUiHandlers(CoreBasePointPresenterHandler uiHandlers) {
		// TODO Auto-generated method stub
		
	}
    
    @Override
    public void recalculateTabs() {
        tab.reload();
    }
    
    protected MaterialTabItem newTabItem(int index) {
        MaterialTabItem item = new MaterialTabItem();
        item.setWaves(WavesType.DEFAULT);
        MaterialLink link = new MaterialLink("Tab " + index);
        link.setTextColor(Color.WHITE);
        link.setHref("#dynamicTab" + index);
        item.add(link);
        MaterialLabel content = new MaterialLabel("Dynamic Content " + index);
        content.setId("dynamicTab" + index);
        dynamicTabsRow.add(content);
        return item;
    }
    
    @Override
    protected void onAttach() {
        super.onAttach();
    	
        //buildListTabIds();
        
        GWT.log("Adding dynamic tab");
        if(tabIndex == 0){
        	tabIndex = 3;
        }
        tabIndex++;
        MaterialTabItem newTab = newTabItem(tabIndex);
        tab.add(newTab);
        tab.setTabIndex(tabIndex);

        
        //tabEvents.addSelectionHandler(
        //selectionEvent -> MaterialToast.fireToast(selectionEvent.getSelectedItem() + " Selected Index"));
	}
    
    /************ CORE VIEW OVERRIDES: *****************/
    
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
		// TODO this:
		tabArgumentsList.add(tabArguments);
        if(tabArgumentsList.size() > 1){
        	//Set tab layout's visibility to visible on GWT's tabs 
            //eg: Android: mTabLayout.setVisibility(View.VISIBLE);
        }
        //Notify the data in the view that it has changed.
        //eg: Android: mPagerAdapter.notifyDataSetChanged();
	}
	

	/********* CORE's USTADVIEW OVERRIDE CAN BE PART OF USTADVIEW IN GWT ***************/
	
	@Override
	public Object getContext() {
		// TODO Check this
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
