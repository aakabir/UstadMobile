echo "Copying and building core, lib db/annotation/util for UstadMoble's GWT module: app-gwt"
rm -rf core-src
rm -rf lib-database-annotation-src
rm -rf lib-database-src
rm -rf lib-util-src

mkdir core-src
mkdir lib-database-annotation-src
mkdir lib-database-src
mkdir lib-util-src

cp -r ../core/src/main core-src/
cp -r ../lib-database-annotation/src/main lib-database-annotation-src/
cp -r ../lib-database-src/src/main lib-database-src/
cp -r ../lib-util-src/src/main lib-util-src/

mv core-src/main/java/com/ustadmobile/core/impl/UstadMobileSystemImplFactory.java core-src/main/java/com/ustadmobile/core/impl/UstadMobileSystemImplFactory.java.test

mv core-src/main/java/com/ustadmobile/core/opds/OpdsEndpointAsyncHelper.java core-src/main/java/com/ustadmobile/core/opds/OpdsEndpointAsyncHelper.java.test

mv core-src/main/java/com/ustadmobile/core/opds/UstadJSOPDSItemAsyncHelper.java core-src/main/java/com/ustadmobile/core/opds/UstadJSOPDSItemAsyncHelper.java.test

mv core-src/main/java/com/ustadmobile/core/util/UMCalendarUtil.java core-src/main/java/com/ustadmobile/core/util/UMCalendarUtil.java.test

mv lib-util-src/main/java/com/ustadmobile/lib/util/UmUuidUtil.java lib-util-src/main/java/com/ustadmobile/lib/util/UmUuidUtil.java.test

echo "Core, lib db, annotations and utils moved. Now you can build the maven project: mvn install, or the usual eclipse + GWT + maven way"
