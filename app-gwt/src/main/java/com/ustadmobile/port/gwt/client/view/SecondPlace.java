package com.ustadmobile.port.gwt.client.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class SecondPlace extends Place{

	String message;
	
	public SecondPlace(String message){
		this.message = message;
	}
	public String getMessage(){
		return message
	}
	public static class Tokenizer implements PlaceTokenizer<SecondPlace>{

		@Override
		public SecondPlace getPlace(String token) {
			return new SecondPlace(token);
		}

		@Override
		public String getToken(SecondPlace place) {
			return place.getMessage();
		}
		
	}
}
