package com.ustadmobile.port.gwt.client.application;

import com.google.inject.Inject;
import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

@DefaultGatekeeper
public class LoggedInGatekeeper implements Gatekeeper{
	
	private String currentUser;
	
	/*
	@Inject
    public LoggedInGatekeeper(String currentUser) {
        this.currentUser = currentUser;
    }
    */
	
	@Inject
    public LoggedInGatekeeper() {
        this.currentUser = null;
    }
	
	@Override
	public boolean canReveal() {
		System.out.println("Can I reveal?");
		
		/*
		if(currentUser == null){
			return false;
		}
		if(currentUser.isEmpty() || currentUser.length() == 0){
			return false;
		}
		*/
		return true;
	
	}
	

}
