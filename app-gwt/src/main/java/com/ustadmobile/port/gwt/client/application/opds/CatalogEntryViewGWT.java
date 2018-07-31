package com.ustadmobile.port.gwt.client.application.opds;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.ustadmobile.core.controller.CatalogPresenter;
/* Aug 2018 : Disabled new master merge
import com.ustadmobile.core.db.DbManager;
*/
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.CatalogEntryView;
import com.ustadmobile.port.gwt.client.application.catalog.CatalogViewGWT;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntry.OpdsItemLoadCallback;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
/* Aug 2018 : Disabled new master merge
import com.ustadmobile.port.gwt.client.db.repository.OpdsEntryRepositoryGWT;
*/
import gwt.material.design.client.ui.MaterialImage;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialPanel;

import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;


public class CatalogEntryViewGWT extends ViewWithUiHandlers
	implements CatalogEntryView{
	
	/**
	 * Arguments for initialising OpdsEntryView. Set in constructor.
	 */
	private Hashtable args;
	
	OpdsEntryWithRelationsDao repository;
	
	public String baseURL = "/";
	
	private OpdsEntryWithRelations entry;
	private CatalogViewGWT parentView;
	
	private CatalogPresenter catalogPresenter;
	
	//This is how GWTP knows to use the OpdsEntryView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, CatalogEntryViewGWT> {
    }
    
    //Also needed for binding to xml file (uiBinder)
    private static final Binder uiBinder = GWT.create(Binder.class);
    
    @UiField
    MaterialPanel entryPanel; 
    
    @UiField
    MaterialLabel entryTitle;
    
    @UiField
    MaterialImage entryImage;
    
    @UiField
    MaterialLabel entryText;
    
    //@UiField
    //MaterialLink entryLink;
    
    public CatalogEntryViewGWT() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public CatalogEntryViewGWT(String title){
    	initWidget(uiBinder.createAndBindUi(this));
    	setTitle(title);
    }
    
    public CatalogEntryViewGWT(OpdsEntryWithRelations entry, CatalogViewGWT view) {
    	initWidget(uiBinder.createAndBindUi(this));
    	this.entry = entry;
    	this.parentView = view;
    	
    	this.baseURL = view.baseURL;
    	
    	List<OpdsLink> links = entry.getLinks();
    	
		String entryLink = entry.getLinks().get(0).getHref();
		
		OpdsLink thumbnail = entry.getThumbnailLink(false);
		String thumbnailLink = "";
		if(thumbnail != null) {
			thumbnailLink = thumbnail.getHref();
			GWT.log("Thumbnail link is: " + thumbnailLink);
		}else {
			GWT.log("No Thumbnail.");
		}
		
		String title = entry.getTitle();
		String text = entry.getSummary();
		
    	setTitle(title);
    	setLink(entryLink);
    	
    	if(thumbnailLink != null && thumbnailLink != "") {
    		String thumbnailLinkResolved = 
    				UMFileUtil.resolveLink(this.baseURL, thumbnailLink);
    		setImageLink(thumbnailLinkResolved);
    	}
    	
    	setText(text);
    	
    	catalogPresenter = new CatalogPresenter(null, null);
    }
    
    @UiHandler("go")
    public void onClick(ClickEvent event) {
    	GWT.log("Entry clicked: " + this.entry.getTitle());
    	
    	catalogPresenter.handleClickEntry(entry);
        
    }

	@Override
	protected void onAttach() {
		// Create a new CatalogPresenter etc. as needed here
		super.onAttach();
	}
	
	public void setTitle(String title){
		this.entryTitle.setText(title);
		this.entryTitle.setTitle(title);
	}
	
	public void setImageLink(String imageLink) {
		this.entryImage.setUrl(imageLink);
	}
	
	public void setText(String text) {
		this.entryText.setText(text);
		this.entryText.setTitle(text);
	}
	
	public void setLink(String link) {
		//Resolve link
		
		String linkResolved = UMFileUtil.resolveLink(this.baseURL, link);
		GWT.log("Resolved link: " + linkResolved);
		//	this.entryLink.setHref(linkResolved);
	}
	
	public void setArguments(Hashtable args) {
		this.args = args;
		System.out.print("DISABLED CLASS>> PLEASE CHECK CODE>>");
		
	}
	
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
	
	/************ CORE VIEW OVERRIDES: ************/

	
	@Override
	public void setButtonDisplayed(int buttonId, boolean display) {
		// TODO
		//1. Get Button from button ID 
		//2. Set its display to visible or not base don display boolean.
		
	}

	@Override
	public void setHeader(String headerFileUri) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMode(int mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocallyAvailableStatus(int status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSize(String entrySize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDescription(String description, String contentType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEntryTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEntryAuthors(String authors) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitlebarText(String titlebarText) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProgressVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProgress(float progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLearnerProgress(CourseProgress progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLearnerProgressVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProgressStatusText(String progressStatusText) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSeeAlsoItem(String[] itemLink, String iconUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeSeeAlsoItem(String[] itemLink) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSeeAlsoVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearSeeAlsoItems() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAlternativeTranslationLinks(String[] languages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setShareButtonVisible(boolean shareButtonVisible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayMode(int viewMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setThumbnail(String iconFileUri, String mimeType) {
		// TODO Auto-generated method stub
		setImageLink(iconFileUri);
		
	}
	
	/* END OF CORE VIEW OVERRIDE */
	
	
}
