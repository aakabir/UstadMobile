package com.ustadmobile.port.gwt.client.db.repository;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.gwt.client.db.dao.OpdsEntryDaoGWT;
import com.ustadmobile.port.gwt.client.db.dao.OpdsEntryWithRelationsDaoGWT;

public class AppDatabaseImplGWT {
	
	UstadMobileSystemImpl impl;
	
	private volatile OpdsEntryDaoGWT opdsEntryDaoGWT;

	private volatile OpdsEntryWithRelationsDaoGWT opdsEntryWithRelationsDaoGWT;
	
	public AppDatabaseImplGWT(){
		//TODO: GWT: Initialise opdsEntryDaoGWT and opdsEntryWithReationsDaoGWT!
		
	}
	
	public OpdsEntryDaoGWT getOpdsEntryDao(){
		//TODO Test
		if (opdsEntryDaoGWT != null) {
		      return opdsEntryDaoGWT;
		}
		return null;
	}

    public OpdsEntryWithRelationsDaoGWT getOpdsEntryWithRelationsDao(){
    	//TODO Test
    	if (opdsEntryWithRelationsDaoGWT != null) {
    	      return opdsEntryWithRelationsDaoGWT;
		}
    	return null;
    }

}
