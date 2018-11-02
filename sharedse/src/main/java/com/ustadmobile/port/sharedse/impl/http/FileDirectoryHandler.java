package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import static com.ustadmobile.port.sharedse.impl.http.MountedZipHandler.URI_ROUTE_POSTFIX;

public class FileDirectoryHandler implements RouterNanoHTTPD.UriResponder {


    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        File rootDir = uriResource.initParameter(File.class);
        String filePath = RouterNanoHTTPD.normalizeUri(session.getUri()).substring(uriResource.getUri().length()-(URI_ROUTE_POSTFIX.length()-1));
        FileInputStream inputStream = null;
        NanoHTTPD.Response response = null;
        try {
            String absPath = new File(rootDir,filePath).getAbsolutePath();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            inputStream = new FileInputStream(absPath);

            UMIOUtils.readFully(inputStream, bout, 10240);
            byte[] assetBytes = bout.toByteArray();
            String extension = UMFileUtil.getExtension(filePath);

            response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                    UstadMobileSystemImpl.getInstance().getMimeTypeFromExtension(extension),
                    new ByteArrayInputStream(assetBytes), assetBytes.length);
            response.addHeader("Cache-Control", "cache, max-age=86400");
            response.addHeader("Content-Length", String.valueOf(assetBytes.length));
        }catch(IOException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 88, session.getUri(), e);
        }finally {
            try {
                if(inputStream != null) {
                    inputStream.close();
                }

            }catch(IOException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 89, session.getUri(), e);
            }
        }

        return response;
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
        return null;
    }
}
