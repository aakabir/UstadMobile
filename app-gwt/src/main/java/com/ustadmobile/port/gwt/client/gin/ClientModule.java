package com.ustadmobile.port.gwt.client.gin;

/**
 * This is the MAIN module. It is from this main module where all child modules
 *  like ApplicationModule and HomeModule are loaded. 
 * The DefaultPlacemanager is set up here.
 * 
 * @author varuna
 *
 */
import com.ustadmobile.port.gwt.client.application.ApplicationModule;
import com.ustadmobile.port.gwt.client.place.NameTokens;
import com.ustadmobile.port.gwt.client.resources.ResourceLoader;
import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;

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
                .defaultPlace(NameTokens.BASE)
                .errorPlace(NameTokens.BASE)
                .unauthorizedPlace(NameTokens.BASE)
                .build());
    	/**
    	 * TODO: Change to .LOGIN when Login functionality ? 
    	 * Or let startUI do that?
    	 * 
    	 * NameTokens.LOGIN
    	 */
    	
    	/**
    	 * GWTP: Install the Root Module
    	 */
        install(new ApplicationModule());
        
        /**
         * GWTP: Load and inject CSS resources
         */
        bind(ResourceLoader.class).asEagerSingleton();
    }
}
