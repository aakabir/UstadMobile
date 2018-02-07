package com.ustadmobile.port.gwt.client.db.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntry.OpdsItemLoadCallback;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.port.gwt.client.db.dao.AbstractLiveDataGWT;
import com.ustadmobile.port.gwt.client.db.dao.UmLiveDataGWT;
import com.ustadmobile.port.gwt.client.db.dao.UmLiveDataGwtSync;
import com.ustadmobile.port.gwt.xmlpull.XmlPullParserGWT;

public class OpdsEntryRepositoryGWT extends OpdsEntryWithRelationsDao{

	private DbManager dbManager;
	
	UstadMobileSystemImpl impl;
	
	private HashMap<String, List<String>> opdsParentToChildListCache;
	
	private HashMap<String, OpdsEntryWithRelations> opdsEntryCache;
	
	public OpdsEntryRepositoryGWT(DbManager manager){
		this.dbManager = manager;
		opdsParentToChildListCache = new HashMap<>();
		opdsEntryCache = new HashMap<>();
		this.impl = UstadMobileSystemImpl.getInstance();
	}
	
	private OpdsItemLoadCallback mLoadcallback = new OpdsItemLoadCallback() {
		
		@Override
		public void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position) {
			GWT.log("OpdsEntryRepositoryGWT: Link added. link is: " + link.getHref());
			
		}
		
		@Override
		public void onError(OpdsEntry item, Throwable cause) {
			// TODO Auto-generated method stub
			GWT.log("OpdsEntryRepositoryGWT: ERROR..");
			
		}
		
		@Override
		public void onEntryAdded(OpdsEntryWithRelations childEntry, OpdsEntry parentFeed, int position) {
			//put the child entry in
			GWT.log("OpdsEntryRepositoryGWT: Child entry being put in: " + childEntry.getTitle());
			List<String> parentFeedList = opdsParentToChildListCache.get(parentFeed.getUuid());
			if(parentFeedList == null) {
				parentFeedList = new ArrayList<>();
				String parentFeedEntryId = parentFeed.getEntryId();
				opdsParentToChildListCache.put(parentFeedEntryId, parentFeedList);
			}
			
			
			if(parentFeedList.size() > position)
				parentFeedList.set(position, childEntry.getUuid());
			else
				parentFeedList.add(childEntry.getUuid());
			
			opdsEntryCache.put(childEntry.getUuid(), childEntry);
		}
		
		@Override
		public void onDone(OpdsEntry item) {
			// TODO Auto-generated method stub
			GWT.log("OpdsEntryRepositoryGWT: Done..");
		}
	};
	
	
	@Override
	public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String entryUuid,
			OpdsItemLoadCallback callbackMain) {
		
		return new AbstractLiveDataGWT<OpdsEntryWithRelations>() {
			@Override
			public void fetchValue(UmCallback<OpdsEntryWithRelations> callback) {
				UmHttpRequest request = new UmHttpRequest(dbManager.getContext(), url);
				impl.makeRequestAsync(request, new UmHttpResponseCallback() {
					
					@Override
					public void onFailure(UmHttpCall call, IOException exception) {
						// TODO Finish
						GWT.log("OpdsEntryRepositoryGWT:getEntryByUrl: Request failed!");
						GWT.log("Exception was: " + exception.getMessage());
						callback.onFailure(exception);
					}
					
					@Override
					public void onComplete(UmHttpCall call, UmHttpResponse response) {
						OpdsEntryWithRelations entry = new OpdsEntryWithRelations();
						entry.setUuid(entryUuid);
						String dataString = null;
						try {
							dataString = new String(response.getResponseBody());
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							GWT.log("OpdsEntryRespositoryGWT onComplete():"
									+ "Unable to read from response.");
						}
						
						XmlPullParser dataXPP = new XmlPullParserGWT(dataString);
						
						//Debugging:
						int dataXPPAttrCount = dataXPP.getAttributeCount();
						//end.
						
						try {
							entry.load(dataXPP, mLoadcallback);
							
							//Debugging:
							List<OpdsLink> entryLinks = entry.getLinks();
							String entryContent = entry.getContent();
							String entryId = entry.getEntryId();
							//end.
							
							callback.onSuccess(entry);
							callbackMain.onDone(entry);
						} catch (IOException | XmlPullParserException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							GWT.log("OpdsEntry.load exception: " + e.getMessage());
							callback.onFailure(e);
						}
						
					}
				});
			}
		};		
	}

	@Override
	public OpdsEntryWithRelations getEntryByUrlStatic(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	//In GWT gets it from localStorage
	@Override
	public UmProvider<OpdsEntryWithRelations> getEntriesByParent(String parentId) {
		// TODO Get from Local Storage
		return null;
	}
	
	@Override
	public UmLiveData<List<OpdsEntryWithRelations>> getEntriesByParentAsList(String parentId) {
		return new AbstractLiveDataGWT<List<OpdsEntryWithRelations>>() {
			@Override
			public void fetchValue(UmCallback<List<OpdsEntryWithRelations>> callback) {
				GWT.log("OpdsEntryRepositoryGWT: fetchValue()");
				List<String> childUuidList = opdsParentToChildListCache.get(parentId);
				List<OpdsEntryWithRelations> childEntriesList = new ArrayList<>();
				if(childUuidList != null) {
					for(String uuid : childUuidList) {
						childEntriesList.add(opdsEntryCache.get(uuid));
					}
				}
				GWT.log("    ...success.");
				callback.onSuccess(childEntriesList);
			}
			
			
		};
	}

	@Override
	public UmLiveData<OpdsEntryWithRelations> getEntryByUuid(String uuid) {
		return new UmLiveDataGwtSync<OpdsEntryWithRelations>(opdsEntryCache.get(uuid));
	}

	@Override
	public String getUuidForEntryUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UmLiveData<List<OpdsEntryWithRelations>> findEntriesByContainerFileDirectoryAsList(String dir) {
		throw new RuntimeException("Container files are not running on the web with GWT!");
	}

	@Override
	public UmProvider<OpdsEntryWithRelations> findEntriesByContainerFileDirectoryAsProvider(String dir) {
		throw new RuntimeException("Container files are not running on the web with GWT!");
	}

}
