package com.ustadmobile.port.gwt.client.impl.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.gwt.http.client.Response;
import com.ustadmobile.core.impl.http.UmHttpResponse;

public class UmHttpResponseGWT extends UmHttpResponse{
	
	private Response response;
	
	public UmHttpResponseGWT(Response respponse){
		this.response = respponse;
	}

	@Override
	public String getHeader(String headerName) {
		//return null;
		return response.getHeader(headerName);
	}

	@Override
	public byte[] getResponseBody() throws IOException {
		//return null;
		return response.getText().getBytes();
	}

	@Override
	public InputStream getResponseAsStream() throws IOException {
		//return null;
		InputStream responseStream = 
				new ByteArrayInputStream(response.getText().getBytes());
		return responseStream;
	}

	@Override
	public boolean isSuccessful() {
		//return false;
		int status = getStatus();
		if(status == 200){
			return true;
		}else{
			return false;
		}
		
	}

	@Override
	public int getStatus() {
		//return 0;
		return response.getStatusCode();
		
	}

}
