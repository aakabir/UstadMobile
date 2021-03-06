package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.impl.http.UmHttpResponse;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

/**
 * Simple wrapper to wrap the OK HTTP library response object
 */

public class UmHttpResponseSe extends UmHttpResponse {

    private Response response;

    public UmHttpResponseSe(Response response) {
        this.response = response;
    }

    @Override
    public String getHeader(String headerName) {
        return response.header(headerName);
    }

    @Override
    public byte[] getResponseBody() throws IOException {
        return response.body().bytes();
    }

    @Override
    public InputStream getResponseAsStream() throws IOException {
        return response.body().byteStream();
    }

    @Override
    public boolean isSuccessful() {
        return response.isSuccessful();
    }

    @Override
    public int getStatus() {
        return response.code();
    }
}
