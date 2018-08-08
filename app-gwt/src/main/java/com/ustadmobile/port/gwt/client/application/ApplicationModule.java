package com.ustadmobile.port.gwt.client.application;

import com.ustadmobile.port.gwt.client.application.about.AboutModule;
import com.ustadmobile.port.gwt.client.application.base.BaseModule;
import com.ustadmobile.port.gwt.client.application.corelogin.CoreLoginModule;
import com.ustadmobile.port.gwt.client.application.login.LoginModule;
import com.ustadmobile.port.gwt.client.application.login.LoginPresenter;
import com.ustadmobile.port.gwt.client.application.login.LoginUiHandlers;
import com.ustadmobile.port.gwt.client.application.upload.UploadCourseModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class ApplicationModule extends AbstractPresenterModule {
    
	@Override
    protected void configure() {
		
		//Install Modules (sub to ApplicationModule)
        install(new LoginModule());
        install(new AboutModule());
        install(new UploadCourseModule());
        install(new CoreLoginModule());
        install(new BaseModule());

        //Bind the Application Presenter to this Application Module (main)
        bindPresenter(ApplicationPresenter.class, 
        		ApplicationPresenter.MyView.class, 
        		ApplicationView.class,
                ApplicationPresenter.MyProxy.class);

        //Bind any UI handlers if you have.
    }
}
