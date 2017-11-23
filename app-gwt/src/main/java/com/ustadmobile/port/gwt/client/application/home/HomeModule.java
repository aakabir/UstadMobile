package com.ustadmobile.port.gwt.client.application.home;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * This is a sub Module (parent: ApplicationModule). 
 * It is bound in its parent's (ApplicationModule) configure().
 * @author varuna
 *
 */
public class HomeModule extends AbstractPresenterModule {
    
	@Override
    protected void configure() {
        bindPresenter(HomePresenter.class, 
        		HomePresenter.MyView.class, 
        		HomeView.class,
                HomePresenter.MyProxy.class);
    }
}
