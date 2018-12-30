package com.ustadmobile.port.sharedse.impl.http;

import java.io.File;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import static com.ustadmobile.port.sharedse.impl.http.MountedZipHandler.URI_ROUTE_POSTFIX;

public class FileDirectoryHandler implements RouterNanoHTTPD.UriResponder {


    private File findRequestFile(RouterNanoHTTPD.UriResource uriResource,
                                 NanoHTTPD.IHTTPSession session) {
        File rootDir = uriResource.initParameter(File.class);
        String filePath = RouterNanoHTTPD.normalizeUri(session.getUri()).substring(uriResource.getUri().length()-(URI_ROUTE_POSTFIX.length()-1));
        return new File(rootDir,filePath);
    }

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        return FileResponder.newResponseFromFile(uriResource, session,
                new FileResponder.FileSource(findRequestFile(uriResource, session)));
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return null;
    }

    @Override
    public NanoHTTPD.Response other(String method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        if(method.equalsIgnoreCase("head"))
            return FileResponder.newResponseFromFile(NanoHTTPD.Method.HEAD, uriResource, session,
                new FileResponder.FileSource(findRequestFile(uriResource, session)));
        else
            return null;
    }
}
