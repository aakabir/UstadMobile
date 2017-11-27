package com.ustadmobile.port.gwt.client.application.home.widget;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * This binds the PresenterWidget
 * @author varuna
 *
 */
public class ContactsWidgetModule extends AbstractPresenterModule{

	@Override
	protected void configure() {
		
		//bind presenter
		bindSingletonPresenterWidget(ContactsWidgetPresenter.class,
				ContactsWidgetPresenter.MyView.class,
				ContactsWidgetView.class);
		
		//If you don't want to have only a single instance :
		/*
		bindPresenterWidget(ContactsWidgetPresenter.class, 
					ContactsWidgetPresenter.MyView.class, 
					ContactsWidgetView.class);
		*/
		
	}

}
