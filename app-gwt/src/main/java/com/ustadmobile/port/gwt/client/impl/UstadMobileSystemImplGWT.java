 package com.ustadmobile.port.gwt.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dev.protobuf.UnknownFieldSet.Field;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.ustadmobile.port.gwt.client.impl.http.UmHttpResponseGWT;
import com.ustadmobile.port.gwt.client.place.NameTokens;
import com.ustadmobile.core.catalog.contenttype.ContentTypePlugin;
import com.ustadmobile.core.impl.ContainerMountRequest;
import com.ustadmobile.core.impl.TinCanQueueListener;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.impl.http.UmHttpCall;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.core.impl.http.UmHttpResponseCallback;
import com.ustadmobile.core.model.CourseProgress;
import com.ustadmobile.core.opds.db.UmOpdsDbManager;
import com.ustadmobile.core.tincan.TinCanResultListener;
import com.ustadmobile.core.view.AppView;

public class UstadMobileSystemImplGWT extends UstadMobileSystemImpl{

	public static UstadMobileSystemImplGWT instance = new UstadMobileSystemImplGWT();
	
	public static UstadMobileSystemImplGWT getInstance() {
		return instance;
	}
	
	//PlaceManager
	private PlaceManager placeManager;

	@Override
	public void go(String viewName, Hashtable args, Object context) {
		//TODO: WIP
		
		/**
		 * 1. Validate viewName
		 * In GWT viewName should be part of NameTokens
		 */
		//Loop through the public final string variables and check
		//Lets assume its valid..
		
		Map<String, String> argsMap = new HashMap();
		Set<String> keys = args.keySet();
		for(String key: keys){
			argsMap.put(key, (String) args.get(key));
		}
		
		int destinationQueryPos = viewName.indexOf('?');
		String viewNameWithoutArgs = viewName;
		if(destinationQueryPos > 0) {
			viewNameWithoutArgs = viewName.substring(0, destinationQueryPos);
		}
		GWT.log("Going to: " + viewNameWithoutArgs + " .");
		PlaceRequest placeRequest = new PlaceRequest.Builder()
				.nameToken(viewNameWithoutArgs)
				//.with(argsMap)
				.build();
		((PlaceManager) context).revealPlace(placeRequest);
		
	}

	@Override
	public boolean loadActiveUserInfo(Object context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getString(int messageCode, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getImplementationName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isJavascriptSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHttpsSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean queueTinCanStatement(JSONObject stmt, Object context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addTinCanQueueStatusListener(TinCanQueueListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTinCanQueueListener(TinCanQueueListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCacheDir(int mode, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UMStorageDir[] getStorageDirs(int mode, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSharedContentDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserContentDirectory(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSystemLocale(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Hashtable getSystemInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long fileLastModified(String fileURI) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public OutputStream openFileOutputStream(String fileURI, int flags) throws IOException, SecurityException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream openFileInputStream(String fileURI) throws IOException, SecurityException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getAsset(Object context, String path, final UmCallback<InputStream> callback) {
		// TODO Auto-generated method stub
		GWT.log("Getting Asset from path in GWT will lookup to a known url and fetch it via http");
		UmHttpRequest request = new UmHttpRequest(path);
		//request.setMethod(RequestBuilder.GET.toString());
		makeRequestAsync(request, new UmHttpResponseCallback() {
			
			@Override
			public void onFailure(UmHttpCall call, IOException exception) {
				// TODO Auto-generated method stub
				GWT.log("getAsset:makeRequestAsync: FAIL");
			}
			
			@Override
			public void onComplete(UmHttpCall call, UmHttpResponse response) {
				// TODO Auto-generated method stub
				GWT.log("getAsset:makeRequestAsync: SUCCESS");
				//TODO: Add method to UmHttpCall to read contents of the query
				try {
					callback.onSuccess(response.getResponseAsStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					GWT.log("EXCEPTION : getAsset:makeRequestAsync:onComplete:response->Stream");
				}
				
			}
		});
	}

	@Override
	public boolean fileExists(String fileURI) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean dirExists(String dirURI) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeFile(String fileURI) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] listDirectory(String dirURI) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getFileDownloadStatus(String downloadID, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean renameFile(String fromFileURI, String toFileURI) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long fileSize(String fileURI) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long fileAvailableSize(String fileURI) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean makeDirectory(String dirURI) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeRecursively(String dirURI) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean makeDirectoryRecursive(String dirURI) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getActiveUser(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActiveUserAuth(String password, Object context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getActiveUserAuth(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUserPref(String key, String value, Object context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUserPref(String key, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getUserPrefKeyList(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveUserPrefs(Object context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAppPref(String key, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAppPrefKeyList(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAppPref(String key, String value, Object context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UmHttpCall makeRequestAsync(UmHttpRequest request, 
			final UmHttpResponseCallback responseListener) {
		// TODO Auto-generated method stub
		Hashtable headers = request.getHeaders();
		String url = request.getUrl();
		Method httpMethod = RequestBuilder.GET;
		String requestData="";
		
		RequestBuilder requestBuilder = new RequestBuilder(httpMethod, url);
		try{
			
			requestBuilder.sendRequest(requestData, new RequestCallback() {
				
				@Override
				public void onResponseReceived(Request request, Response response) {
					// TODO Auto-generated method stub
					GWT.log("sendRequest Succes");
					//responseListener.onComplete(call, response);
					//UmHttpCall call = null;
					UmHttpResponseGWT responseGWT = new UmHttpResponseGWT(response);
					//UmHttpCall never used as far as I can see, so returning null - Varuna
					responseListener.onComplete(null, responseGWT);
					
				}
				
				@Override
				public void onError(Request request, Throwable exception) {
					// TODO Auto-generated method stub
					GWT.log("sendRequest FAIL");
				}
			});
		} catch (RequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			UmHttpCall call = new UmHttpCall() {
				
				@Override
				public void cancel() {
					// TODO Auto-generated method stub
					GWT.log("PLEASE CHECK ME");
					
				}
			};
			
			IOException ie = new IOException(e.getMessage());
			ie.setStackTrace(e.getStackTrace());
			responseListener.onFailure(call, ie);
		}
		
		return null;
	}

	@Override
	public UmHttpCall sendRequestAsync(UmHttpRequest request, UmHttpResponseCallback responseListener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected UmHttpResponse sendRequestSync(UmHttpRequest request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UmHttpResponse makeRequestSync(UmHttpRequest request) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mountContainer(ContainerMountRequest request, int id, UmCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public XmlPullParser newPullParser() throws XmlPullParserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XmlSerializer newXMLSerializer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppView getAppView(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UMLog getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ZipFileHandle openZip(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUMProfileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getResumableRegistrations(String activityId, Object context, TinCanResultListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getVersion(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String hashAuth(Object context, String auth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getAppSetupFile(Object context, boolean zip, UmCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CourseProgress getCourseProgress(String[] entryIds, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int registerUser(String username, String password, Hashtable fields, Object context) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateUser(String username, String password, Hashtable fields, Object context) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean handleLoginLocally(String username, String password, Object context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createUserLocally(String username, String password, String uuid, Object context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ContentTypePlugin[] getSupportedContentTypePlugins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String formatInteger(int integer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getManifestPreference(String key, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAppConfigString(String key, String defaultVal, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserDetail(String username, int field, Object dbContext) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UmOpdsDbManager getOpdsDbManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedHashMap<String, String> getSyncHistory(Object node, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedHashMap<String, String> getMainNodeSyncHistory(Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getMainNodeLastSyncDate(Object context) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void triggerSync(Object context) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String convertTimeToReadableTime(long time) {
		// TODO Auto-generated method stub
		return null;
	}
}
