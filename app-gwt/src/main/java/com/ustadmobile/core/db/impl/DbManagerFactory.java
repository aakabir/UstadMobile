package com.ustadmobile.core.db.impl;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.port.gwt.client.db.DbManagerGWT;

/**
 * Factory for DbManager . This returns the GWT implementation of 
 * DbManager. 
 * @author varuna
 *
 */
public class DbManagerFactory {
	
	public static DbManager makeDbManager(Object context) {
        return new DbManagerGWT(context);
    }


}
