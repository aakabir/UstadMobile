package com.ustadmobile.port.gwt.client.db.dao;

import java.util.List;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.lib.db.entities.OpdsEntry.OpdsItemLoadCallback;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelationsAndContainerMimeType;

public class OpdsEntryWithRelationsDaoGWT extends OpdsEntryWithRelationsDao{

	@Override
	public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String entryUuid,
			OpdsItemLoadCallback callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OpdsEntryWithRelations getEntryByUrlStatic(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UmProvider<OpdsEntryWithRelations> getEntriesByParent(String parentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UmLiveData<List<OpdsEntryWithRelations>> getEntriesByParentAsList(String parentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UmLiveData<OpdsEntryWithRelations> getEntryByUuid(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUuidForEntryUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OpdsEntryWithRelations> getEntriesByParentAsListStatic(String parentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OpdsEntryWithRelations getEntryByUuidStatic(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UmLiveData<List<OpdsEntryWithRelations>> findEntriesByContainerFileDirectoryAsList(List<String> dirList,
			OpdsItemLoadCallback callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UmProvider<OpdsEntryWithRelations> findEntriesByContainerFileDirectoryAsProvider(List<String> dirList,
			OpdsItemLoadCallback callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OpdsEntryWithRelations> findEntriesByContainerFileNormalizedPath(String normalizedPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findParentUrlByChildUuid(String childUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int deleteOpdsEntriesByUuids(List<String> entryUuids) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteLinksByOpdsEntryUuids(List<String> entryUuids) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<OpdsEntryWithRelationsAndContainerMimeType> findByUuidsWithContainerMimeType(List<String> uuids) {
		// TODO Auto-generated method stub
		return null;
	}

}
