package com.ustadmobile.port.gwt.client.application.home;

import javax.inject.Inject;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class HomeView extends ViewImpl implements HomePresenter.MyView {
    
	//This is how GWTP knows to use the HomeView.ui.xml file (bind it)
	interface Binder extends UiBinder<Widget, HomeView> {
    }

    @Inject
    HomeView(Binder uiBinder) {
    	//This method initialises any DOM elements
        initWidget(uiBinder.createAndBindUi(this));
    }
}
