package com.ustadmobile.port.gwt.client.impl;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.ustadmobile.port.gwt.client.view.StartView;

//Creates new views, eventBus and placeController.
//This class makes all views at once during application 
//initialization and those views are always got via here.

public class ClientFactoryImpl implements ClientFactory {
	

	EventBus eventBus = new SimpleEventBus();
	PlaceController placeController = new PlaceController(eventBus);
	StartView startView = new StartView();
	
	@Override
	public EventBus getEventBus() {
		return eventBus;
	}
	@Override
	public PlaceController getPlaceController() {
		return placeController;
	}
	
	/* Create view like so and return it via getters. 
	 
	 CatalogView catalogView = new CatalogView();
	 
	 @Override
	 public CatalogView getCatalogView(){
	 	return catalogView;
	 } 
	 */
	@Override
	public StartView getStartView(){
		return startView;
	}
	

}
