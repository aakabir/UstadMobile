package com.ustadmobile.port.gwt.client.application;

import com.ustadmobile.port.gwt.client.application.home.HomeModule;
import com.ustadmobile.port.gwt.client.application.login.LoginModule;
import com.ustadmobile.port.gwt.client.application.login.LoginPresenter;
import com.ustadmobile.port.gwt.client.application.login.LoginUiHandlers;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class ApplicationModule extends AbstractPresenterModule {
    
	@Override
    protected void configure() {
		
		//Install Modules (sub to ApplicationModule)
        install(new HomeModule());
        install(new LoginModule());

        //Bind Presenter (main)
        bindPresenter(ApplicationPresenter.class, 
        		ApplicationPresenter.MyView.class, 
        		ApplicationView.class,
                ApplicationPresenter.MyProxy.class);
        
        //Bind any UI Handlers:
        bind(LoginUiHandlers.class).to(LoginPresenter.class);
    }
}
