package com.ustadmobile.port.gwt.client.gin;

import com.ustadmobile.port.gwt.client.application.ApplicationModule;
import com.ustadmobile.port.gwt.client.place.NameTokens;
import com.ustadmobile.port.gwt.client.resources.ResourceLoader;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;

/**
 * This is the MAIN module. It is from this main module where all child modules
 *  like ApplicationModule and HomeModule are loaded. 
 * The DefaultPlacemanager is set up here.
 * 
 * @author varuna
 *
 */
public class ClientModule extends AbstractPresenterModule {
    
	/**
	 * The Server's REST API url
	 */
	String REST_SERVER_API = "/api/v1";
	
	@Override
    protected void configure() {
    
    	
    	/*
    	 * GWTP: We need to install the default module or else PlaceManager will not be able
    	 * to inject 
    	 */
		install(new DefaultModule
                .Builder()
                .defaultPlace(NameTokens.LOGIN) //Will load the Presenter and the View's constructor. 
                .errorPlace(NameTokens.BASE)
                .unauthorizedPlace(NameTokens.BASE)
                .build());
		
		
    	/**
    	 * GWTP: Install the Root Module
    	 */
        install(new ApplicationModule()); //Will load the Presenter and the View's constructor.
        
        /**
         * GWTP: Load and inject CSS resources
         */
        bind(ResourceLoader.class).asEagerSingleton();
    }
}
