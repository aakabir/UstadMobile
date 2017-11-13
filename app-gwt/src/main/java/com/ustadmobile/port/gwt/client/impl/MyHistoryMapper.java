package com.ustadmobile.port.gwt.client.impl;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.ustadmobile.port.gwt.client.view.StartPlace;

@WithTokenizers({StartPlace.Tokenizer.class})
public interface MyHistoryMapper extends PlaceHistoryMapper {

}
