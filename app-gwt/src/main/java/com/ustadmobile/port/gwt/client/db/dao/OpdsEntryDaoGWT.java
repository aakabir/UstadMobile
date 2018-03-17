package com.ustadmobile.port.gwt.client.db.dao;

import java.util.List;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.OpdsEntry;

public class OpdsEntryDaoGWT extends OpdsEntryDao {

	UstadMobileSystemImpl impl;
	
	@Override
	public long insert(OpdsEntry entry) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void insertList(List<OpdsEntry> entries) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UmLiveData<Boolean> isEntryPresent(String entryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findTitleByUuid(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

}
