package com.ustadmobile.port.gwt.client.db.dao;

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

	private T value;
	
	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void observe(UstadController controller, UmObserver<T> observer) {
		fetchValue(new UmCallback<T>() {

			@Override
			public void onSuccess(T result) {
				AbstractLiveDataGWT.this.value = value;
				observer.onChanged(value);
				
			}

			@Override
			public void onFailure(Throwable exception) {
				// TODO Auto-generated method stub
				
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
