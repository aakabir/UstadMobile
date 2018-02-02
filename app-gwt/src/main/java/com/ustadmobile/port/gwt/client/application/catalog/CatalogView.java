package com.ustadmobile.port.gwt.client.application.catalog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Hashtable;

import javax.inject.Inject;


public class CatalogView extends ViewWithUiHandlers{
	
	/**
	 * Arguments for initialising CatalogView. Set in constructor.
	 */
	private Hashtable args;
	
	/************ UI BINDER STUFF: *****************/
	
	//This is how GWTP knows to use the CatalogView.ui.xml file (bind it)
    interface Binder extends UiBinder<Widget, CatalogView> {
    }
    
    //Also needed for binding to xml file (uiBinder)
    private static final Binder uiBinder = GWT.create(Binder.class);
    
    @UiField
    TextBox textBox;
    
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
			this.textBox.setText(args.get("url").toString());
		}
	}
	
	
	
	
}
