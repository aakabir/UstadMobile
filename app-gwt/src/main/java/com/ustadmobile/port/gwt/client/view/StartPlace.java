package com.ustadmobile.port.gwt.client.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class StartPlace extends Place{
	
	String message = "";
	
	public StartPlace(String message){
		this.message = message;
	}
	
	public StartPlace() {
		
	}
	
	public String getMessage(){
		return message;
	}
	
	public static class Tokenizer implements PlaceTokenizer<StartPlace> {

		@Override
		public StartPlace getPlace(String token) {
			return new StartPlace(token);
		}

		@Override
		public String getToken(StartPlace place) {
			return place.getMessage();
		}
		
	}

}
