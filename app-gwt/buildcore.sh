echo "Copying core, sharedse, lib db, lib db annotation and util for UstadMoble's GWT module: app-gwt"
echo ""
echo "Cleaning.."
rm -rf core-src
rm -rf lib-database-annotation-src
rm -rf lib-database-src
rm -rf lib-util-src
rm -rf sharedse-src

#    api project(':core')
#    api project(':sharedse')
#    implementation project(":lib-database-annotation")
#    implementation project(":lib-util")

mkdir core-src
mkdir lib-database-annotation-src
mkdir lib-database-src
mkdir lib-util-src
mkdir sharedse-src

echo "Copying sources.."

cp -r ../core/src/main core-src/
cp -r ../sharedse/src/main sharedse-src/
cp -r ../lib-database-annotation/src/main lib-database-annotation-src/
cp -r ../lib-database/src/main lib-database-src/
cp -r ../lib-util/src/main lib-util-src/

echo "Copied. Moving files to be replaced.."

mv core-src/main/java/com/ustadmobile/core/impl/UstadMobileSystemImplFactory.java core-src/main/java/com/ustadmobile/core/impl/UstadMobileSystemImplFactory.java.test

mv core-src/main/java/com/ustadmobile/core/opds/OpdsEndpointAsyncHelper.java core-src/main/java/com/ustadmobile/core/opds/OpdsEndpointAsyncHelper.java.test

mv core-src/main/java/com/ustadmobile/core/opds/UstadJSOPDSItemAsyncHelper.java core-src/main/java/com/ustadmobile/core/opds/UstadJSOPDSItemAsyncHelper.java.test

mv core-src/main/java/com/ustadmobile/core/util/UMCalendarUtil.java core-src/main/java/com/ustadmobile/core/util/UMCalendarUtil.java.test

mv lib-util-src/main/java/com/ustadmobile/lib/util/UmUuidUtil.java lib-util-src/main/java/com/ustadmobile/lib/util/UmUuidUtil.java.test

mv core-src/main/java/com/ustadmobile/core/db/impl/DbManagerFactory.java core-src/main/java/com/ustadmobile/core/db/impl/DbManagerFactory.java.test

echo "..OK."
echo ""
echo "Now you can build the maven project: mvn install, or the usual eclipse + GWT + maven way"
