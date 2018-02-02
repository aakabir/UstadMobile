package com.ustadmobile.port.gwt.client.db.repository;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.gwt.core.client.GWT;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntry.OpdsItemLoadCallback;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.port.gwt.client.db.dao.UmLiveDataGWT;
import com.ustadmobile.port.gwt.xmlpull.XmlPullParserGWT;

public class OpdsEntryRepositoryGWT extends OpdsEntryWithRelationsDao{

	private DbManager dbManager;
	UstadMobileSystemImpl impl;
	
	
	public OpdsEntryRepositoryGWT(DbManager manager){
		this.dbManager = manager;
		this.impl = UstadMobileSystemImpl.getInstance();
	}
	
	@Override
	public UmLiveData<OpdsEntryWithRelations> getEntryByUrl(String url, String entryUuid,
			OpdsItemLoadCallback callback) {
		// TODO Test this
		
		UmLiveData<OpdsEntryWithRelations> data = 
				new UmLiveDataGWT<OpdsEntryWithRelations>();
		
		@SuppressWarnings("deprecation")
		UmHttpRequest request = new UmHttpRequest(url);
		impl.makeRequestAsync(request, new UmHttpResponseCallback() {
			
			@Override
			public void onFailure(UmHttpCall call, IOException exception) {
				// TODO Finish
				GWT.log("OpdsEntryRepositoryGWT:getEntryByUrl: Request failed!");
				GWT.log("Exception was: " + exception.getMessage());
			}
			
			@Override
			public void onComplete(UmHttpCall call, UmHttpResponse response) {
				OpdsEntry entry = new OpdsEntry();
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

				try {
					entry.load(dataXPP, new OpdsItemLoadCallback() {
						
						//2nd callback called
						@Override
						public void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position) {
							// TODO Handle if needed
							
						}
						
						//If any error, this is called. ie: Failed
						@Override
						public void onError(OpdsEntry item, Throwable cause) {
							// TODO Coplete this
							//data = null ?
						}
						
						//1st callback called
						@Override
						public void onEntryAdded(OpdsEntryWithRelations childEntry, OpdsEntry parentFeed, int position) {
							// TODO Handle if needed.
							
						}
						
						//3rd/Last callback called
						@Override
						public void onDone(OpdsEntry item) {
							// TODO Add entry to the data set
							//data.onChanged(entry);
							
							//TODO: Set entried with id in local Storage 
							
						}
					});
				} catch (IOException | XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					GWT.log("OpdsEntry.load exception: " + e.getMessage());
				}
				
			}
		});
		

		return data;
		
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
	//In GWT gets it from localStorage
	@Override
	public UmLiveData<List<OpdsEntryWithRelations>> getEntriesByParentAsList(String parentId) {
		// TODO Get it from local storage 
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
	public UmLiveData<List<OpdsEntryWithRelations>> findEntriesByContainerFileDirectoryAsList(String dir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UmProvider<OpdsEntryWithRelations> findEntriesByContainerFileDirectoryAsProvider(String dir) {
		// TODO Auto-generated method stub
		return null;
	}

}
