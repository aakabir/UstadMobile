package com.ustadmobile.port.gwt.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class SecondUiBinder extends Composite implements ClickHandler{

	private static SecondUiBinderUiBinder uiBinder = GWT.create(SecondUiBinderUiBinder.class);

	interface SecondUiBinderUiBinder extends UiBinder<Widget, SecondUiBinder> {
	}
	
	@UiField
	Label label1;
	@UiField
	Button btn1;
	Presenter presenter;
	

	public SecondUiBinder() {
		String message = "SeconfUIBinder";
		initWidget(uiBinder.createAndBindUi(this));
		btn1.setText("First UIBinder");
		label1.setText(message);
		btn1.addClickHandler(this);
	}


	@Override
	public void onClick(ClickEvent event) {
		presenter.goTo(new StartPlace("I came here from Second UiBinder"));
		
	}

	public Presenter getPresenter(){
		return presenter;
	}
	public void setPresenter(Presenter presenter){
		this.presenter = presenter;
	}

	public void setMessage(String message){
		label1.setText(message);
	}
	
	public interface Presenter{
	       public void goTo(Place place);
	       public void setMessage(String message);
	}
}
