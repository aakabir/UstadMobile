package com.ustadmobile.port.gwt.client.view;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.ustadmobile.port.gwt.client.impl.ClientFactory;

public class SecondActivity extends AbstractActivity implements SecondView.Presenter{

	ClientFactory clientFactory;
	String message;
	
	public SecondActivity(SecondPlace secondPlace, ClientFactory clientFactory){
		this.clientFactory = clientFactory;
		this.message = secondPlace.getMessage();
		
	}
	public void bindEvents(){
		//TODO
	}
	
	@Override
	public void setMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus arg1) {
		SecondView view = clientFactory.getSecondView();
		view.setPresenter(this);
		view.setMessage(this.message);
		
		panel.setWidget(view.asWidget());
		bindEvents();
		
	}
	
	@Override
	public void goTo(Place newPlace){
		clientFactory.getPlaceController().goTo(newPlace);
	}

}
