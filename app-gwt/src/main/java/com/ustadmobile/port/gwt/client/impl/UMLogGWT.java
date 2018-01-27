package com.ustadmobile.port.gwt.client.impl;

import com.google.gwt.core.client.GWT;
import com.ustadmobile.core.impl.UMLog;

public class UMLogGWT extends UMLog{
	
	public static String LOGTAG = "UMLogGWT";

	@Override
	public void l(int level, int code, String message) {
		//TODO: Do this properly
		String logMessage = code + " : " + message;
		logMessage = LOGTAG + logMessage;
        switch(level) {
            case UMLog.DEBUG:
                GWT.log(logMessage);
                break;
            case UMLog.INFO:
            	GWT.log(logMessage);
                break;
            case UMLog.CRITICAL:
            	GWT.log(logMessage);
                break;
            case UMLog.WARN:
            	GWT.log(logMessage);
                break;
            case UMLog.VERBOSE:
            	GWT.log(logMessage);
                break;
            case UMLog.ERROR:
            	GWT.log(logMessage);
                break;
        }
	}

	@Override
	public void l(int level, int code, String message, Exception exception) {
		// TODO Do this properly 
		String logMessage = message + exception.getMessage();
		l(level, code, logMessage);
		
	}
	

}
