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
import com.ustadmobile.port.gwt.client.application.base.BasePresenter.CoreBasePointPresenterHandler;
import com.ustadmobile.port.gwt.client.application.catalog.CatalogViewGWT;
import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.WavesType;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialRow;
import gwt.material.design.client.ui.MaterialTab;
import gwt.material.design.client.ui.MaterialTabItem;

/**
 * This is the GWT View for the Base Module. 
 * The View extends the CorePresenterHandler we made in the GWT Presenter. 
 * Any Core Presenter handlers are accessible here. 
 * 
 * This view class also implements GWT Presenters's MyView interface 
 * which extends the Core View. Any Core View methods are overridden
 *  here and thus made accessible.  
 *  
 * @author Varuna Singh
 *
 */
public class BaseView extends ViewWithUiHandlers<CoreBasePointPresenterHandler> 
  implements BasePresenter.MyView {
	
	private ArrayList<Hashtable> tabArgumentsList;
	private ArrayList<String> tabArgumentStringList;
	
	private int[] tabIconsIds = new int[]{0, 0};
	protected boolean classListVisible;
	private static final int BASEPOINT_MENU_CMD_ID_OFFSET = 5000;
	private int tabIndex = 0; //++ when new tab added.
	private BasePointMenuItem[] mNavigationDrawerItems;
	
	CoreBasePointPresenterHandler presenterUiHandler;
	
	@UiField
	MaterialTab tab;
	
	@UiField
	MaterialRow contentRow;
	
	/************ UI BINDER OVERRIDES: (ViewWithUiHandlers) *****************/
	
	//This is how GWTP knows to use the HomeView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, BaseView> {
    }   
    
    //This method initializes any DOM elements
    @Inject
    BaseView(Binder uiBinder) {
    	GWT.log("BaseView()");
        initWidget(uiBinder.createAndBindUi(this));
        tabArgumentsList = new ArrayList<>();
        tabArgumentStringList = new ArrayList<String>();
    }
    
    @Override
	public void setUiHandlers(CoreBasePointPresenterHandler uiHandlers) {
		GWT.log("BaseView.setUiHandlers()");
		presenterUiHandler = uiHandlers;
	}
    
    /************ UI XML VIEW HANDLERS *****************/
	
	@UiHandler("button_feed")
    void onFeed(ClickEvent event){
    	GWT.log("BaseView:onFeed(): Feed button clicked..sending to About for now.."
    			+ "via Override in BasePresenter via UiHandler()..");
    	
    	//TODO: CHeck why getUiHandlers() is null:
    	//getUiHandlers().feedClicked();
    	presenterUiHandler.feedClicked();
    }
	
    
    /************ GWT VIEW OVERRIDES: (ViewImpl) *****************/
    
    /**
     * This method is specially called when the view is refreshed 
     * and loaded every time AFTER Core's onCreate, etc. 
     * 
     * Its overriden from GWT's View class
     */
    @Override
    protected void onAttach() {
        super.onAttach();
        GWT.log("BaseView onAttach()");
	}
    
    
    /************ BASEPRESENTER's MyView Interface OVERRIDES: *****************/
    
    @Override
    public void refreshTabs() {
        tab.reload();        
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

	@Override
	public void addTab(Hashtable tabArguments) {
		// TODO: Finish this
		GWT.log("BaseView.addTab() Adding Tab..");
		
		//Check if the Tab is already present.
		String thisTabTitleNumber = (String) tabArguments.get("t");
		if(tabArgumentStringList.contains(thisTabTitleNumber)){
			GWT.log("BaseView.addTab(): Already contains this tab. Skipping.");
		}else {
			tabArgumentStringList.add(thisTabTitleNumber);
			tabArgumentsList.add(tabArguments);
	        if(tabArgumentsList.size() > 1){
	        	//TODO: 
	        	int x;
	        	//Set tab layout's visibility to visible on GWT's tabs 
	            //eg: Android: mTabLayout.setVisibility(View.VISIBLE);
	        }
	        
	        //TODO: Find GWT way for:
	        //Notify the data in the view that it has changed.
	        // eg: Android: mPagerAdapter.notifyDataSetChanged();
	        
	        tabIndex++;
	        MaterialTabItem newTab = newBasePointTabItem(this.tabIndex, tabArguments);
	        tab.add(newTab);
	        tab.setTabIndex(tabIndex);
		}
        
	}
	
	/************ COMMON METHODS: *****************/
    
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
