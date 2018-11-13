package com.ustadmobile.lib.contentscrapers.phetsimulation;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao_JdbcDaoImpl;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;

import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;

/**
 * The Phet Simulation Website provides a list of all the available Html5 Content in one of their categories found at
 * https://phet.colorado.edu/en/simulations/category/html
 * <p>
 * By using a css selector: td.simulation-list-item span.sim-badge-html
 * We can get the url to each simulation in that category to give to PhetContentScraper to scrap its content
 */
public class IndexPhetContentScraper {

    private File destinationDirectory;
    private URL url;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoin;
    private ContentEntryContentCategoryJoinDao contentEntryCategoryJoinDao;
    private ContentEntryRelatedEntryJoinDao contentEntryRelatedJoinDao;
    private ContentEntryFileStatusDao contentFileStatusDao;


    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: <phet html url> <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        try {
            new IndexPhetContentScraper().findContent(args[0], new File(args[1]));
        } catch (IOException e) {
            System.err.println("Exception running findContent");
            e.printStackTrace();
        }
    }

    /**
     * Given a phet url, find the content and download
     *
     * @param urlString      url link to phet category
     * @param destinationDir destination folder for phet content
     * @throws IOException
     */
    public void findContent(String urlString, File destinationDir) throws IOException {

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            System.out.println("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDir.mkdirs();
        destinationDirectory = destinationDir;

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        contentEntryDao = db.getContentEntryDao();
        contentParentChildJoinDao = db.getContentEntryParentChildJoinDao();
        contentEntryFileDao = db.getContentEntryFileDao();
        contentEntryFileJoin = db.getContentEntryContentEntryFileJoinDao();
        contentEntryCategoryJoinDao = db.getContentEntryContentCategoryJoinDao();
        contentEntryRelatedJoinDao = db.getContentEntryRelatedEntryJoinDao();
        contentFileStatusDao = db.getContentEntryFileStatusDao();

        Document document = Jsoup.connect(urlString).get();

        browseCategory(document);

    }


    private void browseCategory(Document document) throws IOException {

        Elements simulationList = document.select("td.simulation-list-item span.sim-badge-html");

        ContentEntry masterRootParent = contentEntryDao.findBySourceUrl("root");
        if (masterRootParent == null) {
            masterRootParent = new ContentEntry();
            masterRootParent= setContentEntryData(masterRootParent, "root",
                    "Ustad Mobile", "root", ScraperConstants.ENGLISH_LANG_CODE);
            masterRootParent.setContentEntryUid(contentEntryDao.insert(masterRootParent));
        } else {
            masterRootParent = setContentEntryData(masterRootParent, "root",
                    "Ustad Mobile", "root", ScraperConstants.ENGLISH_LANG_CODE);
            contentEntryDao.updateContentEntry(masterRootParent);
        }

        ContentEntry phetParentEntry = contentEntryDao.findBySourceUrl("https://phet.colorado.edu/");
        if (phetParentEntry == null) {
            phetParentEntry = new ContentEntry();
            phetParentEntry = setContentEntryData(phetParentEntry, "https://phet.colorado.edu/",
                    "Phet Interactive Simulations", "https://phet.colorado.edu/", ScraperConstants.ENGLISH_LANG_CODE);
            phetParentEntry.setThumbnailUrl("https://phet.colorado.edu/images/phet-social-media-logo.png");
            phetParentEntry.setContentEntryUid(contentEntryDao.insert(phetParentEntry));
        } else {
            phetParentEntry = setContentEntryData(phetParentEntry, "https://phet.colorado.edu/",
                    "Phet Interactive Simulations", "https://phet.colorado.edu/", ScraperConstants.ENGLISH_LANG_CODE);
            phetParentEntry.setThumbnailUrl("https://phet.colorado.edu/images/phet-social-media-logo.png");
            contentEntryDao.updateContentEntry(phetParentEntry);
        }

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, phetParentEntry, 1);

        for (Element simulation : simulationList) {

            String path = simulation.parent().attr("href");
            String simulationUrl = new URL(url, path).toString();
            String title = simulationUrl.substring(simulationUrl.lastIndexOf("/") + 1, simulationUrl.length());
            String thumbnail = simulation.parent().selectFirst("img").attr("src");

            ContentEntry englishSimContentEntry = contentEntryDao.findBySourceUrl(path);
            if (englishSimContentEntry == null) {
                englishSimContentEntry = new ContentEntry();
                englishSimContentEntry = setContentEntryData(englishSimContentEntry, path, title, path, ScraperConstants.ENGLISH_LANG_CODE);
                englishSimContentEntry.setThumbnailUrl(thumbnail);
                englishSimContentEntry.setContentEntryUid(contentEntryDao.insert(englishSimContentEntry));
            } else {
                englishSimContentEntry = setContentEntryData(englishSimContentEntry, path, title, path, ScraperConstants.ENGLISH_LANG_CODE);
                contentEntryDao.updateContentEntry(englishSimContentEntry);
            }

            ContentScraperUtil.insertOrUpdateRelatedContentJoin(contentEntryRelatedJoinDao, englishSimContentEntry, englishSimContentEntry, ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION);

            PhetContentScraper scraper = new PhetContentScraper(simulationUrl, destinationDirectory);
            try {
                scraper.scrapeContent();

                if (scraper.isAnyContentUpdated()) {

                    boolean isEnglishUpdated = scraper.getLanguageUpdatedMap().get("en");
                    if(isEnglishUpdated){

                        File langLocation = new File(destinationDirectory, "en");
                        File content = new File(langLocation, title + ScraperConstants.ZIP_EXT);
                        FileInputStream fis = new FileInputStream(content);
                        String md5 = DigestUtils.md5Hex(fis);
                        fis.close();

                        ContentEntryFile contentEntryFile = new ContentEntryFile();
                        contentEntryFile.setMimeType(ScraperConstants.MIMETYPE_ZIP);
                        contentEntryFile.setFileSize(content.length());
                        contentEntryFile.setLastModified(content.lastModified());
                        contentEntryFile.setMd5sum(md5);
                        contentEntryFile.setContentEntryFileUid(contentEntryFileDao.insert(contentEntryFile));

                        ContentEntryContentEntryFileJoin fileJoin = new ContentEntryContentEntryFileJoin();
                        fileJoin.setCecefjContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
                        fileJoin.setCecefjContentEntryUid(englishSimContentEntry.getContentEntryUid());
                        fileJoin.setCecefjUid(contentEntryFileJoin.insert(fileJoin));

                        ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
                        fileStatus.setCefsContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
                        fileStatus.setFilePath(content.getAbsolutePath());
                        fileStatus.setCefsUid(contentFileStatusDao.insert(fileStatus));

                    }

                    ArrayList<ContentEntry> categoryList = scraper.getCategoryRelations(contentEntryDao);
                    ArrayList<ContentEntry> translationList = scraper.getTranslations(destinationDirectory, contentEntryDao, thumbnail);

                    // TODO remove all categories that no longer exist
                    // TODO remove all categories that dont belong in a phet simulation anymore

                    int categoryCount = 0;
                    for (ContentEntry category : categoryList) {

                        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, phetParentEntry, category, categoryCount++);
                        ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao, category, englishSimContentEntry, 0);
                        ContentScraperUtil.insertOrUpdateChildWithMultipleCategoriesJoin(contentEntryCategoryJoinDao, category, englishSimContentEntry);

                        int translationsCount = 1;
                        for (ContentEntry translation : translationList) {

                            ContentScraperUtil.insertOrUpdateRelatedContentJoin(contentEntryRelatedJoinDao, translation, englishSimContentEntry, ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION);

                            String langCode = translation.getPrimaryLanguage() +
                                    ((translation.getPrimaryLanguageCountry() != null) ? "-" + translation.getPrimaryLanguageCountry() : "");

                            if(scraper.getLanguageUpdatedMap().get(langCode)){

                                File langLocation = new File(destinationDirectory, langCode);
                                File content = new File(langLocation, title + ScraperConstants.ZIP_EXT);
                                FileInputStream fis = new FileInputStream(content);
                                String md5 = DigestUtils.md5Hex(fis);
                                fis.close();

                                ContentEntryFile contentEntryFile = new ContentEntryFile();
                                contentEntryFile.setMimeType(ScraperConstants.MIMETYPE_ZIP);
                                contentEntryFile.setFileSize(content.length());
                                contentEntryFile.setLastModified(content.lastModified());
                                contentEntryFile.setMd5sum(md5);
                                contentEntryFile.setContentEntryFileUid(contentEntryFileDao.insert(contentEntryFile));

                                ContentEntryContentEntryFileJoin fileJoin = new ContentEntryContentEntryFileJoin();
                                fileJoin.setCecefjContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
                                fileJoin.setCecefjContentEntryUid(englishSimContentEntry.getContentEntryUid());
                                fileJoin.setCecefjUid(contentEntryFileJoin.insert(fileJoin));

                                ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
                                fileStatus.setCefsContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
                                fileStatus.setFilePath(content.getAbsolutePath());
                                fileStatus.setCefsUid(contentFileStatusDao.insert(fileStatus));


                            }

                            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao, category, translation, translationsCount++);
                            ContentScraperUtil.insertOrUpdateChildWithMultipleCategoriesJoin(contentEntryCategoryJoinDao, category, translation);

                        }
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getCause());
            }
        }
    }

    private ContentEntry setContentEntryData(ContentEntry entry, String id, String title, String sourceUrl, String lang) {
        entry.setEntryId(id);
        entry.setTitle(title);
        entry.setSourceUrl(sourceUrl);
        entry.setPublisher("Phet");
        entry.setLicenseType(LICENSE_TYPE_CC_BY);
        entry.setPrimaryLanguage(lang);
        return entry;
    }

}
