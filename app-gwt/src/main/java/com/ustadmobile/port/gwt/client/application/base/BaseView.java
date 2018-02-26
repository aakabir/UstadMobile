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
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointMenuItem;
import com.ustadmobile.port.gwt.client.application.ApplicationPresenter;
import com.ustadmobile.port.gwt.client.application.base.BasePresenter.CoreBasePointPresenterHandler;
import com.ustadmobile.port.gwt.client.application.catalog.CatalogViewGWT;

import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.Flex;
import gwt.material.design.client.constants.WavesType;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialPanel;
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
	private BasePointMenuItem[] mNavigationDrawerItems;
	
	@UiField
	MaterialTab tab;
	
	@UiField
	MaterialRow dynamicTabsRow;
	
	//@UiField
    //MaterialPanel content;
	
	/************ UI BINDER STUFF: *****************/
	
	//This is how GWTP knows to use the HomeView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, BaseView> {
    }   
    
    //This method initializes any DOM elements
    @Inject
    BaseView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        tabArgumentsList = new ArrayList<>();
        //bindSlot(BasePresenter.SLOT_TAB, content);
    }
    
    @Override
	public void setUiHandlers(CoreBasePointPresenterHandler uiHandlers) {
		// TODO Auto-generated method stub
		
	}
    
    @Override
    public void recalculateTabs() {
        tab.reload();
    }
    
    protected MaterialTabItem newTestTabItem(int index) {
        MaterialTabItem item = new MaterialTabItem();
        item.setWaves(WavesType.DEFAULT);
        //MaterialLink link = new MaterialLink("Test " + index);
        MaterialLink link = new MaterialLink("Available");
        link.setTextColor(Color.WHITE);
        link.setHref("#dynamicTab" + index);
        item.add(link);
        MaterialPanel catalogPanel = new MaterialPanel();
        catalogPanel.setId("dynamicTab" + index);
        
        CatalogViewGWT catalogView = new CatalogViewGWT();
        Hashtable args= new Hashtable();
        //args.put("url", "com/ustadmobile/core/test.opds");
        
        //Testing SAL:
        args.put("url", "http://www.ustadmobile.com/files/saltracker/index.opds");
        catalogView.setArguments(args);
        
        catalogPanel.add(catalogView);
        dynamicTabsRow.add(catalogPanel);
        
        
        return item;
    }
    
    protected MaterialTabItem newBasePointTabItem(int index, String id, String title, 
    		Color textColor, String materialLink, String href, String contentLabel, WavesType wavesType){
    	
    	MaterialTabItem item = new MaterialTabItem();
    	item.setWaves(wavesType);
    	
    	MaterialLink link = new MaterialLink(materialLink);
    	link.setTextColor(textColor);
    	link.setHref(href);
    	
    	item.add(link);
    	
    	MaterialLabel content = new MaterialLabel(contentLabel);
    	content.setId(id);
    	
    	return null;
    }
    
    protected MaterialTabItem newBasePointTabItem(int index, Hashtable tabArguments){
    	
    	//TODO: Check if we should use this here
		UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
		
    	MaterialTabItem item = new MaterialTabItem();
    	item.setWaves(WavesType.DEFAULT);
    	
    	//MaterialLink link = new MaterialLink("#matlink" + index);
    	String titleNumber = (String) tabArguments.get("t");
    	int messageCode = Integer.valueOf((String) titleNumber);
    	String titleFromImpl = impl.getString(messageCode, this);
    	String url = (String) tabArguments.get("url");
    	titleFromImpl = "Get String : " + titleNumber;
    	if(messageCode == 80) {
    		titleFromImpl = "Libraries";
    	}
    	if(messageCode == 81) {
    		titleFromImpl = "Downloaded";
    	}
    	MaterialLink link = new MaterialLink(titleFromImpl);
    	link.setTextColor(Color.WHITE);
    	link.setHref(url);
    	
    	item.add(link);
    	
    	MaterialLabel content = new MaterialLabel("content: " + index);
    	content.setId("id:" + index);
    	
    	return item;
    }
        
    @Override
    protected void onAttach() {
        super.onAttach();
        
        GWT.log("BaseView: Adding dynamic tab");
        if(tabIndex == 0){
        	tabIndex = 1;
        }
        tabIndex++;
        MaterialTabItem newTab = newTestTabItem(tabIndex);
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
		//TODO: Finish this
		
		//TODO: Check if we should use this here
		UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
		
		//1. Set Navigation Drawer Items
		this.mNavigationDrawerItems = menuItems;
		//2. Clear the drawer Navigation View
		//refresh SLOT.NAVIGATION ?
		//3. Put items in
		for(int i = 0; i < menuItems.length; i++) {
			//3a. Add the menu item to navigation menu.
			String menuItemString = impl.getString(menuItems[i].getTitleStringId(), this);
            //eg: item = drawerMenu.add(0, BASEPOINT_MENU_CMD_ID_OFFSET+ i, 0, menuItemString);
            //Set navigation menu item's icon:
            
        }
		
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
	
	public void validateTab(){
		
	}

	@Override
	public void addTab(Hashtable tabArguments) {
		// TODO: Finish this
		
		tabArgumentsList.add(tabArguments);
        if(tabArgumentsList.size() > 1){
        	//Set tab layout's visibility to visible on GWT's tabs 
            //eg: Android: mTabLayout.setVisibility(View.VISIBLE);
        }
        //Notify the data in the view that it has changed.
        //eg: Android: mPagerAdapter.notifyDataSetChanged();
        
        /**
         * addTab() : Adds tab args to tagArgumentList variable
         * notified new item added
         * handler: onTabSelected has the new position index 
         * handler then gets the item. If fragment doesn't exist (this case), a new
         * CatalogOPDSFragment Fragment object is created and returned.
         * This fragment then gets added to a member variable fragmentMap
         * 
         */
        //GWT add the tab to the list as well
        /* DISABLING FOR NOW> TODO: PLEASE UNCOMMENT TO MAKE IT WORK!
        MaterialTabItem newTab = newBasePointTabItem(this.tabIndex, tabArguments);
        tab.add(newTab);
        tab.setTabIndex(tabIndex);
        */
        
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
