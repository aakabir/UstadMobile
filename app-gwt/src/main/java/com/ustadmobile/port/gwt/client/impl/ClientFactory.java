package com.ustadmobile.port.gwt.client.impl;

import com.google.web.bindery.event.shared.EventBus;
import com.ustadmobile.port.gwt.client.view.SecondView;
import com.ustadmobile.port.gwt.client.view.Start;
import com.ustadmobile.port.gwt.client.view.StartView;
import com.google.gwt.place.shared.PlaceController;

public interface ClientFactory {

	EventBus getEventBus();
    PlaceController getPlaceController();
    
    //Views getters
    //CatalogView getCatalogView();
    StartView getStartView();
    Start getStartUIBinder();
    SecondView getSecondView();
    
}
