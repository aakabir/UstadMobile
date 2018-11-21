package com.ustadmobile.lib.contentscrapers.prathambooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neovisionaries.i18n.LanguageAlpha3Code;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.Language;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;


/**
 * Storyweaver has an api for all their epub books.
 * To download each book, i need to have a cookie session id
 * I get session by logging in the website and entering the credentials and retrieving the cookie
 * To get the total number of books,
 * hit the api with just 1 book request and in the json, the total number of books is stored in metadata.hits
 * Call the api again with the request for all books
 * create the url to get the epub, open the url connection and add the cookie session
 * <p>
 * If IOException is thrown, might be because the session expired so login again.
 * otherwise file is downloaded in its folder
 */
public class IndexPrathamContentScraper {

    String prefixUrl = "https://storyweaver.org.in/api/v1/books-search?page=";

    String prefixEPub = "https://storyweaver.org.in/v0/stories/download-story/";
    String ePubExt = ".epub";

    String signIn = "https://storyweaver.org.in/users/sign_in";

    private Gson gson;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao;
    private ContentEntryFileStatusDao contentFileStatusDao;
    private ContentEntry prathamParentEntry;
    private Language englishLang;
    private LanguageDao languageDao;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        try {
            new IndexPrathamContentScraper().findContent(new File(args[0]));
        } catch (IOException | URISyntaxException e) {
            System.err.println("Exception running findContent");
            e.printStackTrace();
        }
    }

    public void findContent(File destinationDir) throws IOException, URISyntaxException {

        destinationDir.mkdirs();

        String cookie = loginPratham();

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentEntryFileDao = repository.getContentEntryFileDao();
        contentEntryFileJoinDao = repository.getContentEntryContentEntryFileJoinDao();
        contentFileStatusDao = repository.getContentEntryFileStatusDao();
        languageDao = repository.getLanguageDao();

        englishLang = ContentScraperUtil.insertOrUpdateLanguage(languageDao, "English");

        ContentEntry masterRootParent = contentEntryDao.findBySourceUrl("root");
        if (masterRootParent == null) {
            masterRootParent = new ContentEntry();
            masterRootParent= setContentEntryData(masterRootParent, "root",
                    "Ustad Mobile", "root", englishLang, false, "");
            masterRootParent.setContentEntryUid(contentEntryDao.insert(masterRootParent));
        } else {
            masterRootParent = setContentEntryData(masterRootParent, "root",
                    "Ustad Mobile", "root", englishLang, false, "");
            contentEntryDao.update(masterRootParent);
        }


        prathamParentEntry = contentEntryDao.findBySourceUrl("https://storyweaver.org.in/");
        if (prathamParentEntry == null) {
            prathamParentEntry = new ContentEntry();
            prathamParentEntry = setContentEntryData(prathamParentEntry, "https://storyweaver.org.in/",
                    "Pratham Books", "https://storyweaver.org.in/", englishLang, false, "Every Child in School & Learning Well");
            prathamParentEntry.setThumbnailUrl("https://prathambooks.org/wp-content/uploads/2018/04/Logo-black.png");
            prathamParentEntry.setContentEntryUid(contentEntryDao.insert(prathamParentEntry));
        } else {
            prathamParentEntry = setContentEntryData(prathamParentEntry, "https://storyweaver.org.in/",
                    "Pratham Books", "https://storyweaver.org.in/", englishLang, false, "Every Child in School & Learning Well");
            prathamParentEntry.setThumbnailUrl("https://prathambooks.org/wp-content/uploads/2018/04/Logo-black.png");
            contentEntryDao.update(prathamParentEntry);
        }

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, prathamParentEntry, 3);

        gson = new GsonBuilder().disableHtmlEscaping().create();

        downloadPrathamContentList(generatePrathamUrl(String.valueOf(1)), cookie, destinationDir);

    }


    private void downloadPrathamContentList(URL contentUrl, String cookie, File destinationDir) throws URISyntaxException, IOException {

        BooksResponse contentBooksList = gson.fromJson(IOUtils.toString(contentUrl.toURI(), ScraperConstants.UTF_ENCODING), BooksResponse.class);

        if(contentBooksList.data.size() == 0){
            return;
        }

        int retry = 0;
        for (int contentCount = 0; contentCount < contentBooksList.data.size(); contentCount++) {

            try {

                BooksResponse.Data data = contentBooksList.data.get(contentCount);

                URL epubUrl = generatePrathamEPubFileUrl(data.slug);

                URLConnection connection = epubUrl.openConnection();
                connection.setRequestProperty("Cookie", cookie);

                String lang = getLangCode(data.language);
                Language langEntity = ContentScraperUtil.insertOrUpdateLanguage(languageDao,LanguageAlpha3Code.getByCode(lang).getName());
                File resourceFolder = new File(destinationDir, String.valueOf(data.id));
                resourceFolder.mkdirs();
                String resourceFileName = data.slug + ePubExt;

                ContentEntry contentEntry = contentEntryDao.findBySourceUrl(epubUrl.getPath());
                if (contentEntry == null) {
                    contentEntry = new ContentEntry();
                    contentEntry = setContentEntryData(contentEntry, data.slug,
                            data.title , epubUrl.getPath(), langEntity, true, data.description);
                    contentEntry.setThumbnailUrl(data.coverImage.sizes.get(0).url);
                    contentEntry.setContentEntryUid(contentEntryDao.insert(contentEntry));
                } else {
                    contentEntry = setContentEntryData(contentEntry, data.slug,
                            data.title, epubUrl.getPath(), langEntity, true, data.description);
                    contentEntry.setThumbnailUrl(data.coverImage.sizes.get(0).url);
                    contentEntryDao.update(contentEntry);
                }

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao,
                        prathamParentEntry, contentEntry, contentCount);

                File content = new File(resourceFolder, resourceFileName);
                if (!ContentScraperUtil.isFileModified(connection, resourceFolder, String.valueOf(data.id))) {
                    continue;
                }
                try {
                    FileUtils.copyInputStreamToFile(connection.getInputStream(), content);
                } catch (IOException io) {
                    cookie = loginPratham();
                    retry++;
                    System.err.println("Login and retry the link again attempt" + retry);
                    if (retry == 2) {
                        retry = 0;
                        continue;
                    }
                    contentCount--;
                    continue;
                }
                retry = 0;

                FileInputStream fis = new FileInputStream(content);
                String md5 = DigestUtils.md5Hex(fis);
                fis.close();

                ContentEntryFile contentEntryFile = new ContentEntryFile();
                contentEntryFile.setMimeType(ScraperConstants.MIMETYPE_EPUB);
                contentEntryFile.setFileSize(content.length());
                contentEntryFile.setLastModified(content.lastModified());
                contentEntryFile.setMd5sum(md5);
                contentEntryFile.setContentEntryFileUid(contentEntryFileDao.insert(contentEntryFile));

                ContentEntryContentEntryFileJoin fileJoin = new ContentEntryContentEntryFileJoin();
                fileJoin.setCecefjContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
                fileJoin.setCecefjContentEntryUid(contentEntry.getContentEntryUid());
                fileJoin.setCecefjUid(contentEntryFileJoinDao.insert(fileJoin));

                ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
                fileStatus.setCefsContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
                fileStatus.setFilePath(content.getAbsolutePath());
                fileStatus.setCefsUid(contentFileStatusDao.insert(fileStatus));


            } catch (Exception e) {
                System.err.println("Error saving book " + contentBooksList.data.get(contentCount).slug);
                e.printStackTrace();
            }

        }

        downloadPrathamContentList(generatePrathamUrl(String.valueOf(++contentBooksList.metadata.page)), cookie, destinationDir);

    }

    private String getLangCode(String language) {
        String[] list = language.split("-");
        return LanguageAlpha3Code.findByName(list[0]).get(0).name();
    }

    private ContentEntry setContentEntryData(ContentEntry entry, String id, String title, String sourceUrl, Language lang, boolean isLeaf, String desc) {
        entry.setEntryId(id);
        entry.setTitle(title);
        entry.setDescription(desc);
        entry.setSourceUrl(sourceUrl);
        entry.setPublisher("Pratham");
        entry.setLicenseType(ContentEntry.LICENSE_TYPE_CC_BY);
        entry.setPrimaryLanguageUid(lang.getLangUid());
        entry.setLeaf(isLeaf);
        return entry;
    }

    public URL generatePrathamEPubFileUrl(String resourceId) throws MalformedURLException {
        return new URL(prefixEPub + resourceId + ePubExt);
    }

    public URL generatePrathamUrl(String number) throws MalformedURLException {
        return new URL(prefixUrl + number);
    }

    public String loginPratham() {
        ChromeDriver driver = ContentScraperUtil.setupChrome(false);

        String cookie = "";
        driver.get(signIn);
        WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);

        driver.findElement(By.id("user_email")).sendKeys("samihmustafa@gmail.com");
        driver.findElement(By.id("user_password")).sendKeys("reading123");
        driver.findElement(By.name("commit")).click();

        for (Cookie ck : driver.manage().getCookies()) {

            if (ck.getName().equalsIgnoreCase("_session_id")) {
                cookie = ck.getName() + "=" + ck.getValue();
                System.out.println(cookie);
            }
        }

        driver.close();

        return cookie;
    }


}