package com.ustadmobile.port.gwt.client.application.catalog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.opds.OpdsFilterOptions;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithStatusCache;
import com.ustadmobile.port.gwt.client.application.opds.CatalogEntryViewGWT;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;


public class CatalogViewGWT extends ViewWithUiHandlers
	implements CatalogPresenterGWT.MyView{
	
	/**
	 * Arguments for initialising CatalogView. Set in constructor.
	 */
	private Hashtable args;
	
	OpdsEntryWithRelationsDao repository;
	
	public String baseURL = "/";
	
	/************ UI BINDER STUFF: *****************/
	
	//This is how GWTP knows to use the CatalogView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, CatalogViewGWT> {
    }
    
    //Also needed for binding to xml file (uiBinder)
    private static final Binder uiBinder = GWT.create(Binder.class);
    
    @UiField
    FlowPanel entriesPanel; 
    
    public CatalogViewGWT() {
        initWidget(uiBinder.createAndBindUi(this));
    }

	@Override
	protected void onAttach() {
		// Create a new CatalogPresenter etc. as needed here
		super.onAttach();
		
	}
	
	public void setArguments(Hashtable args) {
		this.args = args;
		System.out.println("DISABLED CLASS>> PLEASE CHECK CODE>>");
	}
	
	public void handleEntryChanged(OpdsEntryWithRelations entry) {
		
		if(entry != null){
			String entryId = entry.getEntryId();
			
			repository.getEntriesByParentAsList(entryId).observeForever(
					new UmObserver<List<OpdsEntryWithRelations>>() {
				
				@Override
				public void onChanged(List<OpdsEntryWithRelations> t) {
					GWT.log("CatalogView: handleEntryChanged().onChanged():");
					
					//UI stuff:
					for (int i=0; i<t.size(); i++){
						OpdsEntryWithRelations entry = t.get(i);
						String entryTitle = entry.getTitle();
						GWT.log("CatalogView: Entry: " + entryTitle + ". "
								+ "Adding to View..");
						addUiEntry(entry);
					}
					
				}
			});
			
		}else{
			GWT.log("CatalogView.handleEntryChanged: ERROR. entry is null!");
		}
		
	}
	
	public void addUiEntry(OpdsEntryWithRelations entry){
		GWT.log("CatalogView: Adding entry to UI: " + entry.getTitle());
		
		CatalogEntryViewGWT entryCardComplete = new CatalogEntryViewGWT(entry, this);
		entriesPanel.add(entryCardComplete);
		
	}
	
	/************ CORE VIEW OVERRIDES: ************/

	@Override
	public void setEntryStatus(String entryId, int status) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setEntryBackground(String entryId, String backgroundFileURI) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCatalogBackground(String backgroundFileURI) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateDownloadAllProgress(int loaded, int total) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDownloadEntryProgressVisible(String entryId, boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateDownloadEntryProgress(String entryId, float progress, String statusText) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEntryProgress(String entryId, CourseProgress progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelectedEntries(Set<String> entries) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFooterButtonVisible(boolean buttonVisible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFooterButtonLabel(String browseButtonLabel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDeleteOptionAvailable(boolean deleteOptionAvailable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAddOptionAvailable(boolean addOptionAvailable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAlternativeTranslationLinks(String[] translationLinks, int disabledItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRefreshing(boolean isRefreshing) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFilterOptions(OpdsFilterOptions filterOptions) {
		// TODO Auto-generated method stub
		
	}
	
	/* END OF CORE VIEW OVERRIDE */
	
	
	/*
	 * CORE USTADVIEW OVERRIDES: 
	 * TODO: CAN BE PART OF USTADVIEW IN GWT
	 * */

	@Override
	public Object getContext() {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public void setEntryProvider(UmProvider<OpdsEntryWithStatusCache> entryProvider) {
		// TODO Auto-generated method stub
		
	}
	
	/* End of CORE USTADVIEW OVERRIDE */
	
}
