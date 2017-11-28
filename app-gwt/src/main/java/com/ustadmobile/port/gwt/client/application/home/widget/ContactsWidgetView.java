package com.ustadmobile.port.gwt.client.application.home.widget;

import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class ContactsWidgetView extends ViewWithUiHandlers<ContactsWidgetUiHandlers> 
  implements ContactsWidgetPresenter.MyView{
	
	//This is how GWTP knows to use the ContactsWidgetView.ui.xml file (bind it)
	public interface Binder extends 
		UiBinder<Widget, ContactsWidgetView> {
	}
	
	private final Widget widget;
	
	@UiField
	Button confirm;
	
	@Inject
	ContactsWidgetView(Binder uiBinder) {
		//Another way
		widget = uiBinder.createAndBindUi(this);
		
		//Normal way
		//This method initializes any DOM elements
		//initWidget(uiBinder.createAndBindUi(this));
		//widget = null;
	}
	
	//Handling events for Button in this View.
	@UiHandler("confirm")
	void onConfirm(ClickEvent event){
		getUiHandlers().confirm();
	}
	
	//Returning widget set here.
	@Override
	public Widget asWidget() {
		return widget;
	}
	
}
