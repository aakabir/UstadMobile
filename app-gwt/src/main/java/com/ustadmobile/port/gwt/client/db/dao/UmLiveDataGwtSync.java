package com.ustadmobile.port.gwt.client.db.dao;

import com.ustadmobile.core.controller.UstadController;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;

/**
 * Implements the LiveData interface on GWT for values that are already
 * present in memory. This essentially just calls onChanged the first time
 * it is observed.
 * 
 * @author mike
 *
 * @param <T>
 */
public class UmLiveDataGwtSync<T> implements UmLiveData<T> {
	
	private T value;
	
	public UmLiveDataGwtSync(T value) {
		this.value = value;
	}
	
	
	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void observe(UstadController controller, UmObserver<T> observer) {
		observer.onChanged(value);
	}

	@Override
	public void observeForever(UmObserver<T> observer) {
		observer.onChanged(value);
		
	}

	@Override
	public void removeObserver(UmObserver<T> observer) {
		// This is redundant on GWT - we don't do live change detection on GWT
	}

}
