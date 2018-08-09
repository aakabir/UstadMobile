package com.ustadmobile.port.gwt.client.application;

import com.ustadmobile.port.gwt.client.application.about.AboutModule;
import com.ustadmobile.port.gwt.client.application.base.BaseModule;
import com.ustadmobile.port.gwt.client.application.corelogin.CoreLoginModule;
import com.ustadmobile.port.gwt.client.application.login.LoginModule;
import com.ustadmobile.port.gwt.client.application.login.LoginPresenter;
import com.ustadmobile.port.gwt.client.application.login.LoginUiHandlers;
import com.ustadmobile.port.gwt.client.application.upload.UploadCourseModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * The main entry point for the GWTP app. This is installed at ClientModule class.
 * 
 * Every Module extends GWTP's AbstractPresenterModule. 
 * 
 * @author Varuna Singh
 *
 */
public class ApplicationModule extends AbstractPresenterModule {
    
	/**
	 * This gets automatically run from ClientModule. This method installs all 
	 * sub-Modules - which are all the screens (+core presenters+core view) of 
	 * this application. 
	 * 
	 * This method also binds the ApplicationPresenter and ApplicationView to this
	 * module. 
	 */
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
