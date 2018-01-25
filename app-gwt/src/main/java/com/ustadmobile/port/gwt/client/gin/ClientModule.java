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
    @Override
    protected void configure() {
    	
    	/*
    	 * We need to install the default module or else PlaceManager will not be able
    	 * to inject 
    	 */
        /*
    	install(new DefaultModule
                .Builder()
                .defaultPlace(NameTokens.LOGIN)
                .errorPlace(NameTokens.LOGIN)
                .unauthorizedPlace(NameTokens.LOGIN)
                .build());
        */
    	
    	install(new DefaultModule
                .Builder()
                .defaultPlace(NameTokens.BASE)
                .errorPlace(NameTokens.BASE)
                .unauthorizedPlace(NameTokens.BASE)
                .build());
    	
        install(new ApplicationModule());

        /*
        bindConstant().annotatedWith(DefaultPlace.class).to(NameTokens.LOGIN);
        bindConstant().annotatedWith(ErrorPlace.class).to(NameTokens.LOGIN);
        bindConstant().annotatedWith(UnauthorizedPlace.class).to(NameTokens.LOGIN);
        */
        
        bind(ResourceLoader.class).asEagerSingleton();
    }
}
