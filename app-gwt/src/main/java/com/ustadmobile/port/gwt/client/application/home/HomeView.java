package com.ustadmobile.port.gwt.client.application.home;

import javax.inject.Inject;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class HomeView extends ViewImpl 
	implements HomePresenter.MyView {
    
	//This is how GWTP knows to use the HomeView.ui.xml file (bind it)
	interface Binder extends UiBinder<Widget, HomeView> {
    }
    
	// The PresenterWidget's Contacts container goes in this panel.
    @UiField
    SimplePanel contactPanel; 

    @Inject
    HomeView(Binder uiBinder) {
    	//Tells to use HomeView.ui.binder
        initWidget(uiBinder.createAndBindUi(this));

        // Binding the Presenter's slot to the container
        bindSlot(HomePresenter.SLOT_CONTACTS, contactPanel); 
    }
}
