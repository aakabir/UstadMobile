package com.ustadmobile.port.gwt.client.application.catalog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntry.OpdsItemLoadCallback;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.port.gwt.client.application.opds.OpdsEntryView;
import com.ustadmobile.port.gwt.client.db.repository.OpdsEntryRepositoryGWT;

import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;


public class CatalogView extends ViewWithUiHandlers{
	
	/**
	 * Arguments for initialising CatalogView. Set in constructor.
	 */
	private Hashtable args;
	
	OpdsEntryWithRelationsDao repository;
	
	/************ UI BINDER STUFF: *****************/
	
	//This is how GWTP knows to use the CatalogView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, CatalogView> {
    }
    
    //Also needed for binding to xml file (uiBinder)
    private static final Binder uiBinder = GWT.create(Binder.class);
    
    @UiField
    FlowPanel entriesPanel; 
    
    public CatalogView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

	@Override
	protected void onAttach() {
		// Create a new CatalogPresenter etc. as needed here
		super.onAttach();
		
		
	}
	
	public void setArguments(Hashtable args) {
		this.args = args;
		
		if(args != null && args.get("url") != null) {
			repository = DbManager.getInstance(this)
					.getOpdsEntryWithRelationsRepository();
			String url = (String)args.get("url");
			
			///*
			UmLiveData<OpdsEntryWithRelations> dataLive = 
					repository.getEntryByUrl(url, null, new OpdsItemLoadCallback() {
				
				@Override
				public void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position) {
					// TODO Auto-generated method stub
					GWT.log("CatalogView: Link Aded..");
					
				}
				
				@Override
				public void onError(OpdsEntry item, Throwable cause) {
					// TODO Auto-generated method stub
					GWT.log("CatalogView: ERROR!");
					
				}
				
				@Override
				public void onEntryAdded(OpdsEntryWithRelations childEntry, OpdsEntry parentFeed, int position) {
					// TODO Auto-generated method stu
					GWT.log("CatalogView: Entry added..");
				}
				
				@Override
				public void onDone(OpdsEntry item) {
					// TODO Auto-generated method stub
					String itemTitle = item.getTitle();
					GWT.log("CatalogView:setArguments():getEntryByUrl:onDone(): "
							+ "Its done. Item: " + itemTitle);
					
				}
			});
			
			GWT.log("CatalogView: ObserveForever..");
			dataLive.observeForever(this::handleEntryChanged);
			
			
		}
	}
	
	public void handleEntryChanged(OpdsEntryWithRelations entry) {
		
		if(entry != null){
			String entryId = entry.getEntryId();
			
			repository.getEntriesByParentAsList(entryId).observeForever(
					new UmObserver<List<OpdsEntryWithRelations>>() {
				
				@Override
				public void onChanged(List<OpdsEntryWithRelations> t) {
					GWT.log("CatalogView: handleEntryChanged().onChanged():");
					
					//testing
					if(t.size() < 0){
						GWT.log("CatalogView: ENTRY LIST IS ZERO.");
					}else{
						GWT.log("CatalogView: Entry size: " + t.size());
					}
					
					//UI stuff:
					for (int i=0; i<t.size(); i++){
						OpdsEntryWithRelations entry = t.get(i);
						String entryTitle = entry.getTitle();
						GWT.log("CatalogView: Entry: " + entryTitle + ". "
								+ "Adding to View..");
						addUiEntry(entryTitle);
					}
					
				}
			});
			
			
		}else{
			GWT.log("CatalogView.handleEntryChanged: ERROR. entry is null!");
		}
		
		
	}
	
	public void addUiEntry(String title){
		GWT.log("CatalogView: Adding entry to UI: " + title);
		TextBox textBox = new TextBox();
		textBox.setName(title);
		textBox.setText(title);
		
		OpdsEntryView entryCard = new OpdsEntryView(title);
		entriesPanel.add(entryCard);
		
		//entriesPanel.add(textBox);
	}
	
	
	
}
