package com.ustadmobile.port.gwt.client.impl;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.ustadmobile.port.gwt.client.view.StartActivity;
import com.ustadmobile.port.gwt.client.view.StartPlace;

public class MyActivityMapper implements ActivityMapper {
	
	//ClientFactory (that has all the views
	private ClientFactory clientFactory;
	
	//Constructor sets clientfactory
	public MyActivityMapper(ClientFactory clientFactory){
		this.clientFactory = clientFactory;
	}

	/**
	 * Maps places to activity
	 */
	@Override
	public Activity getActivity(Place place) {
		if(place instanceof StartPlace){
			return new StartActivity((StartPlace) place, clientFactory);
		}
		
		return null;
	}

}
