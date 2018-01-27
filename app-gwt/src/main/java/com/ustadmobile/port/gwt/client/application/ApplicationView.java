package com.ustadmobile.port.gwt.client.application;

import javax.inject.Inject;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

import gwt.material.design.client.ui.MaterialPanel;

public class ApplicationView extends ViewImpl implements ApplicationPresenter.MyView {
    interface Binder extends UiBinder<Widget, ApplicationView> {
    }

    @UiField
    MaterialPanel content;
    
    /*
    @UiField
    MaterialPanel main;
    
    @UiField
    MaterialPanel login;
    
    @UiField
    MaterialPanel about;
    
    @UiField
    MaterialPanel corelogin;
    
    @UiField
    MaterialPanel base;
	*/
    
    @Inject
    ApplicationView(
            Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        //Bind slots in ApplicationPresenter to ApplicationView's Ui Binder.
        bindSlot(ApplicationPresenter.SLOT_CONTENT, content);
        /*
        bindSlot(ApplicationPresenter.SLOT_MAIN, main);
        bindSlot(ApplicationPresenter.SLOT_ABOUT, about);
        bindSlot(ApplicationPresenter.SLOT_LOGIN, login);
        bindSlot(ApplicationPresenter.SLOT_CORELOGIN, corelogin);
        bindSlot(ApplicationPresenter.SLOT_BASE, base);
        */
    }
}
