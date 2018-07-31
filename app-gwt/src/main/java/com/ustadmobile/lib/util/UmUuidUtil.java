package com.ustadmobile.lib.util;


/**
 * TEMPORARY OVERIDE OF UMUUIDUTIL FOR GWT!! 
 */
public class UmUuidUtil {

    /**
     * 
     * @param uuid UUID object
     * @return
     */
    public static final String encodeUuidWithAscii85(Object uuid) {
    	System.out.println("ATTENTION! TEMPORARY OVERRIDE "
    			+ "OF UmUuidUtil is being called. This should NOT happen. PLEASE CHECK CODE and CLEAN UP");
        return uuid.toString();
    }

}
