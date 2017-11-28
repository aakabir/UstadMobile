package com.ustadmobile.port.gwt.client.application.home.widget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;


/**
 * This is a presenterwidget. That can be reused. Notice
 * there is no MyProxy. This is NOT a place. We cannot use
 * reveal either.  
 * @author varuna
 *
 */
public class ContactsWidgetPresenter 
  extends PresenterWidget<ContactsWidgetPresenter.MyView> 
	implements ContactsWidgetUiHandlers{

	//The Presenter's View
	public interface MyView extends View, 
	  HasUiHandlers<ContactsWidgetUiHandlers> {
    }
	
	//Constructor
	@Inject
    ContactsWidgetPresenter(
            EventBus eventBus,
            MyView view) {
        super(eventBus, view);

        getView().setUiHandlers(this);
    }
	
	/*
	@Inject
	ContactsWidgetPresenter(
            EventBus eventBus,
            MyView view
            //,MyProxy proxy
            ,Object proxy
            ,PlaceManager placeManager
            ,ContactsWidgetPresenter contactsWidgetPresenter
    		){
		super(eventBus, view);
		getView().setUiHandlers(this);
	}
	*/
	
	//UiHandlers methods
	@Override
	public void confirm(String field) {
		//Do something?
		System.out.println("ContactsWidget confirm");
		
	}
	
	@Override
	public void confirm() {
		//Do something?
		System.out.println("ContactsWidget confirm no arg");
		
	}

}
