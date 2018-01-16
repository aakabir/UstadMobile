package com.ustadmobile.core.impl;

import com.ustadmobile.port.gwt.client.impl.UstadMobileSystemImplGWT;

public class UstadMobileSystemImplFactory {

    /**
     * @return
     */
    public static UstadMobileSystemImpl makeSystemImpl() {	
        return new UstadMobileSystemImplGWT();
    }

}