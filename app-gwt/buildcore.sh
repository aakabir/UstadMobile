echo "Copying and building core for UstadMoble's GWT module: app-gwt"
rm -rf core-src
mkdir core-src
cp -r ../core/src/main core-src/

mv core-src/main/java/com/ustadmobile/core/impl/UstadMobileSystemImplFactory.java core-src/main/java/com/ustadmobile/core/impl/UstadMobileSystemImplFactory.java.test

mv core-src/main/java/com/ustadmobile/core/opds/OpdsEndpointAsyncHelper.java core-src/main/java/com/ustadmobile/core/opds/OpdsEndpointAsyncHelper.java.test

mv core-src/main/java/com/ustadmobile/core/opds/UstadJSOPDSItemAsyncHelper.java core-src/main/java/com/ustadmobile/core/opds/UstadJSOPDSItemAsyncHelper.java.test

mv core-src/main/java/com/ustadmobile/core/util/UMCalendarUtil.java core-src/main/java/com/ustadmobile/core/util/UMCalendarUtil.java.test

echo "Core moved. Now you can build the maven project: mvn install, or the usual eclipse + GWT + maven way"
