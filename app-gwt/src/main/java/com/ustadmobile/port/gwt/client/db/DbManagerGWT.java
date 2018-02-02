package com.ustadmobile.port.gwt.client.db;

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
		if(opdsEntryRepository == null){
			//opdsEntryRepository = new OpdsEntryRepositoryGWT(this, executorService);
			opdsEntryRepository = new OpdsEntryRepositoryGWT(this);
		}

        return opdsEntryRepository;
	}

	//Needed
	@Override
	public OpdsEntryDao getOpdsEntryDao() {
		// TODO Test
		return appDatabase.getOpdsEntryDao();
	}

	//Needed
	@Override
	public OpdsEntryWithRelationsDao getOpdsEntryWithRelationsDao() {
		// TODO Test
		return appDatabase.getOpdsEntryWithRelationsDao();
	}

	@Override
	public OpdsLinkDao getOpdsLinkDao() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OpdsEntryParentToChildJoinDao getOpdsEntryParentToChildJoinDao() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContainerFileDao getContainerFileDao() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContainerFileEntryDao getContainerFileEntryDao() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetworkNodeDao getNetworkNodeDao() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntryStatusResponseDao getEntryStatusResponseDao() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DownloadJobDao getDownloadJobDao() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getContext() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
