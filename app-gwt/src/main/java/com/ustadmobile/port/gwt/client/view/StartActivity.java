package com.ustadmobile.port.gwt.client.view;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.ustadmobile.port.gwt.client.impl.ClientFactory;
import com.ustadmobile.port.gwt.client.impl.UstadMobileSystemImplGWT;

public class StartActivity extends AbstractActivity implements Start.Presenter{

	ClientFactory clientFactory;
	
	String message;
	
	public StartActivity(StartPlace startPlace, ClientFactory clientFactory){
		this.clientFactory = clientFactory;
		this.message = startPlace.getMessage();
	}
	
	public void bindEvents(){
		//
	}
	
	@Override
	public void goTo(Place newPlace) {
		clientFactory.getPlaceController().goTo(newPlace);
		
	}

	@Override
	public void setMessage(String message) {
		
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus arg1) {
		//StartView view = clientFactory.getStartView();
		Start view = clientFactory.getStartUIBinder();
		view.setPresenter(this);
		view.setMessage(this.message);
		
		panel.setWidget(view.asWidget());
		bindEvents();
	}

	@Override
	public void handleClickButton() {
		// TODO Auto-generated method stub
		UstadMobileSystemImplGWT.getInstance().go(clientFactory, "Second", null);
	}

	
	
}
