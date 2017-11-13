package com.ustadmobile.port.gwt.client.impl;

import java.util.HashMap;

import com.google.gwt.place.shared.Place;
import com.ustadmobile.port.gwt.client.view.SecondPlace;
import com.ustadmobile.port.gwt.client.view.StartPlace;

public class UstadMobileSystemImplGWT {
		
	public static UstadMobileSystemImplGWT instance = new UstadMobileSystemImplGWT();
	
	public static UstadMobileSystemImplGWT getInstance() {
		return instance;
	}
	
	public void go(Object context, String destination, HashMap args) {
		ClientFactory factory = (ClientFactory)context;
		Place place = null;
		if(destination.equals("Start")) {
			place = new StartPlace();
		}else if(destination.equals("Second")) {
			place = new SecondPlace();
		}
		
		factory.getPlaceController().goTo(place);
	}
	
}
