package com.ustadmobile.port.gwt.client.place;

import com.ustadmobile.core.view.AboutView;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.CatalogView;
import com.ustadmobile.core.view.LoginView;

/**
 * This class identifies all Places available as String urls.
 * @author varuna
 * 
 * Note: Important to have these strings unique
 *
 */
public class NameTokens {
    public static final String HOME = "!home";
    public static final String LOGIN = "!login";
    //public static final String ABOUT = "!about";
    public static final String ABOUT = AboutView.VIEW_NAME;
    //public static final String CORELOGIN = "!corelogin";
    public static final String CORELOGIN = LoginView.VIEW_NAME; 
    //public static final String BASE = "!base";
    public static final String BASE = BasePointView.VIEW_NAME;
    
    public static final String CATALOG = CatalogView.VIEW_NAME;
    
    public static final String COURSE_UPLOAD = "!upload";
}
