package com.ustadmobile.port.gwt.client.db;

import com.google.gwt.core.client.GWT;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.dao.ContainerFileDao;
import com.ustadmobile.core.db.dao.ContainerFileEntryDao;
import com.ustadmobile.core.db.dao.DownloadJobDao;
import com.ustadmobile.core.db.dao.EntryStatusResponseDao;
import com.ustadmobile.core.db.dao.NetworkNodeDao;
import com.ustadmobile.core.db.dao.OpdsEntryDao;
import com.ustadmobile.core.db.dao.OpdsEntryParentToChildJoinDao;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.db.dao.OpdsLinkDao;
import com.ustadmobile.port.gwt.client.db.repository.AppDatabaseImplGWT;
import com.ustadmobile.port.gwt.client.db.repository.OpdsEntryRepositoryGWT;

/**
 * GWT's implementation of DbManager in package port.gwt.client.db 
 * @author varuna
 *
 */
public class DbManagerGWT extends DbManager {

	//PlaceManager
	private PlaceManager placeManager; //?
	
	//OpdsEntryWithRelationsRepository
	private OpdsEntryRepositoryGWT opdsEntryRepository;
	
	//OpdsEntryWithRelationsDao
	//OpdsEntryDao
	private AppDatabaseImplGWT appDatabase;

	public DbManagerGWT(Object context){
		//TODO: Initialise appDatabase stuff!
		
	}

	//Needed
	@Override
	public OpdsEntryWithRelationsDao getOpdsEntryWithRelationsRepository() {
		// TODO Check
		GWT.log("getOpdsEntryWithRelationsRepository");
		if(opdsEntryRepository == null){
			GWT.log("getOpdsEntryWithRelationsRepository opdsEntryRepository is null");
			//opdsEntryRepository = new OpdsEntryRepositoryGWT(this, executorService);
			opdsEntryRepository = new OpdsEntryRepositoryGWT(this);
		}

        return opdsEntryRepository;
	}

	//Needed
	@Override
	public OpdsEntryDao getOpdsEntryDao() {
		// TODO Test
		GWT.log("getOpdsEntryDao");
		return appDatabase.getOpdsEntryDao();
	}

	//Needed
	@Override
	public OpdsEntryWithRelationsDao getOpdsEntryWithRelationsDao() {
		// TODO Test
		GWT.log("getOpdsEntryWithRelationsDao");
		return opdsEntryRepository;
	}

	@Override
	public OpdsLinkDao getOpdsLinkDao() {
		// TODO Auto-generated method stub
		GWT.log("getOpdsLinkDao");
		return null;
	}

	@Override
	public OpdsEntryParentToChildJoinDao getOpdsEntryParentToChildJoinDao() {
		// TODO Auto-generated method stub
		GWT.log("getOpdsEntryParentToChildJoinDao");
		return null;
	}

	@Override
	public ContainerFileDao getContainerFileDao() {
		// TODO Auto-generated method stub
		GWT.log("getContainerFileDao");
		return null;
	}

	@Override
	public ContainerFileEntryDao getContainerFileEntryDao() {
		// TODO Auto-generated method stub
		GWT.log("getContainerFileEntryDao");
		return null;
	}

	@Override
	public NetworkNodeDao getNetworkNodeDao() {
		// TODO Auto-generated method stub
		GWT.log("getNetworkNodeDao");
		return null;
	}

	@Override
	public EntryStatusResponseDao getEntryStatusResponseDao() {
		// TODO Auto-generated method stub
		GWT.log("getEntryStatusResponseDao");
		return null;
	}

	@Override
	public DownloadJobDao getDownloadJobDao() {
		// TODO Auto-generated method stub
		GWT.log("getDownloadJobDao");
		return null;
	}

	@Override
	public Object getContext() {
		// TODO Auto-generated method stub
		GWT.log("getContext");
		return null;
	}
	
}
