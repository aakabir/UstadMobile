package com.ustadmobile.port.gwt.client.db.dao;

import com.google.gwt.core.client.GWT;
import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.impl.UmCallback;

/**
 * This wrapper for UmLiveData will fetch data asynchronously the first time
 * observe is called. Implementing subclasses only need to implement 
 * fetchValue . 
 * 
 * @author mike
 *
 * @param <T>
 */
public abstract class AbstractLiveDataGWT<T> implements UmLiveData<T> {

	private T gwtresult;
	
	@Override
	public T getValue() {
		return gwtresult;
	}

	public void setValue(T newValue){
		gwtresult = newValue;
	}
	
	@Override
	public void observe(UstadController controller, UmObserver<T> observer) {
		fetchValue(new UmCallback<T>() {

			@Override
			public void onSuccess(T result) {
				GWT.log("AbstractLiveData: observe(). Success..");
				AbstractLiveDataGWT.this.gwtresult = result;
				setValue(result);
				gwtresult = result;
				observer.onChanged(gwtresult);
				
			}

			@Override
			public void onFailure(Throwable exception) {
				// TODO Auto-generated method stub
				GWT.log("AbstractLiveDataGWT:observe: onFailure!");
				
			}
		});
	}

	@Override
	public void observeForever(UmObserver<T> observer) {
		observe(null, observer);
	}

	@Override
	public void removeObserver(UmObserver<T> observer) {
		//This is really irrelevant on GWT, as there is no real push change notification etc.
	}
	
	public abstract void fetchValue(UmCallback<T> callback);

}
