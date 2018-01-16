package com.ustadmobile.core.opds;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.impl.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Handles async loading of UstadJSOPDSItem. This is normally done using threading, but is separated
 * out so that it can overriden on GWT (which does not support threads).
 *
 */
class UstadJSOPDSItemAsyncHelper implements Runnable {

    private UstadJSOPDSItem item;

    UstadJSOPDSItemAsyncHelper(UstadJSOPDSItem item) {
        this.item = item;
    }

    void load() {
        if(item.asyncLoadUrl.startsWith(OpdsEndpoint.OPDS_PROTOCOL)) {
            OpdsEndpoint.getInstance().loadItemAsync(item.asyncLoadUrl, item, item.asyncContext,
                    item.asyncLoadCallback);
        }else {
            //new Thread(this).start();
    		UstadMobileSystemImpl.getInstance().makeRequestAsync(new UmHttpRequest(item.asyncContext,
                    item.asyncLoadUrl), new UmHttpResponseCallback() {
                @Override
                public void onComplete(UmHttpCall call, UmHttpResponse response) {
                	try{
                		item.loadFromInputStream(response.getResponseAsStream());
                	}catch(IOException e){
                		e.printStackTrace();
                		item.asyncLoadCallback.onError(item, e);
                	}
                }

                @Override
                public void onFailure(UmHttpCall call, IOException exception) {
					item.asyncLoadCallback.onError(item, exception);
                }
            });
        	
        	
        }
    }

    @Override
    public void run() {
    	/*
        InputStream in = null;
        IOException ioe = null;
        UmHttpResponse response;
        try {
            response = UstadMobileSystemImpl.getInstance().makeRequestSync(
                    new UmHttpRequest(item.asyncContext, item.asyncLoadUrl));
            in = response.getResponseAsStream();
            item.loadFromInputStream(in);
        }catch(IOException e) {
            ioe = e;
        }finally {
            UMIOUtils.closeInputStream(in);
        }
        if(ioe != null && item.asyncLoadCallback != null){
            item.asyncLoadCallback.onError(item, ioe);
        }
        */
    	
    }

}
