package com.ustadmobile.port.gwt.client.application;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

import gwt.material.design.client.ui.MaterialPanel;

/**
 * The parent View class for the whole application. All other presenters
 * will be sub presenters and modules to this View which can have 
 * global UI bits like hamburger menu, etc in here. 
 * 
 * This class's constructor sets itself in the SLOT of the VIEW XML
 * @author Varuna Singh
 *
 */
public class ApplicationView extends ViewImpl implements ApplicationPresenter.MyView {
    interface Binder extends UiBinder<Widget, ApplicationView> {
    }

    @UiField
    MaterialPanel content;
    
    @Inject
    ApplicationView(
            Binder uiBinder) {
    	GWT.log("ApplicationView()");
        initWidget(uiBinder.createAndBindUi(this));
        //Bind slots in ApplicationPresenter to ApplicationView's Ui Binder.
        bindSlot(ApplicationPresenter.SLOT_CONTENT, content);
    }
}
