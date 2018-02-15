package com.ustadmobile.port.gwt.client.application.opds;

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
import com.ustadmobile.port.gwt.client.db.repository.OpdsEntryRepositoryGWT;

import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialPanel;

import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;


public class OpdsEntryView extends ViewWithUiHandlers{
	
	/**
	 * Arguments for initialising OpdsEntryView. Set in constructor.
	 */
	private Hashtable args;
	
	OpdsEntryWithRelationsDao repository;
	
	/************ UI BINDER STUFF: *****************/
	
	//This is how GWTP knows to use the OpdsEntryView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, OpdsEntryView> {
    }
    
    //Also needed for binding to xml file (uiBinder)
    private static final Binder uiBinder = GWT.create(Binder.class);
    
    @UiField
    MaterialPanel entryPanel; 
    
    @UiField
    MaterialLabel title;
    
    public OpdsEntryView() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public OpdsEntryView(String title){
    	initWidget(uiBinder.createAndBindUi(this));
    	setTitle(title);
    }

	@Override
	protected void onAttach() {
		// Create a new CatalogPresenter etc. as needed here
		super.onAttach();
		
		
	}
	
	public void setTitle(String title){
		this.title.setText(title);
		this.title.setText(title);
	}
	
	public void setArguments(Hashtable args) {
		this.args = args;
		
		if(args != null && args.get("url") != null) {
			repository = DbManager.getInstance(this)
					.getOpdsEntryWithRelationsRepository();
			
			//String url = (String)args.get("url");
			
		}
	}
	
}
