package com.ustadmobile.port.gwt.client.application.login;

import com.gwtplatform.mvp.client.UiHandlers;

public interface LoginUiHandlers extends UiHandlers{
	
	void confirm(String username, String password);
	void aboutClicked();

}
