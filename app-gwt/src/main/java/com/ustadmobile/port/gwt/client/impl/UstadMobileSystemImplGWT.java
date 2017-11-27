 package com.ustadmobile.port.gwt.client.impl;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.ustadmobile.port.gwt.client.place.NameTokens;

public class UstadMobileSystemImplGWT {

	public static UstadMobileSystemImplGWT instance = new UstadMobileSystemImplGWT();
	
	public static UstadMobileSystemImplGWT getInstance() {
		return instance;
	}
	
	//PlaceManager
	private PlaceManager placeManager;
	
	public void go(Object context, String destination) {
		
		// Navigate to destination (value should be part of NameTokens)
    	System.out.println("Going to " + destination + ".");
    	PlaceRequest placeRequest = new PlaceRequest.Builder()
	            .nameToken(destination)
	            .build();
    	placeManager.revealPlace(placeRequest);		
		
	}
}
