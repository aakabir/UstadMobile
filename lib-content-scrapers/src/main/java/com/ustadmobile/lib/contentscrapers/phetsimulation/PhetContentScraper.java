package com.ustadmobile.lib.contentscrapers.phetsimulation;

import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.ContentEntry;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;


/**
 * The page for each simulation on the website follows the same format
 * <p>
 * The english simulation can be found by using the css selector
 * div.simulation-main-image-panel a.phet-button[href]
 * which provides the button with href link to download the html file.
 * <p>
 * The description of the simulation can be found at the div tag with id "about" to get its content.
 * Within the about html, there is a description used for purpose of tincan which can be found by the selector - p.simulation-panel-indent
 * <p>
 * The translations for the simulation can be found in the table table.phet-table tr
 * In the css selector for that tr row
 * you will get the language in the column with selector - "td.list-highlight-background a[href]"
 * you will get the download link column with selector - "td.img-container a[href]"
 * <p>
 * The download links url have eTag and last modified in the headers to identify new content
 */
public class PhetContentScraper {

    public static final String[] CATEGORY = {
            "iPad/Tablet", "New Sims", "Simulations", "HTML5"};
    private final String url;
    private final File destinationDirectory;
    private final String title;
    private Document simulationDoc;
    private String aboutText;
    private ArrayList<String> langugageList;
    private Map<String, Boolean> languageMapUpdate;

    private String aboutDescription;
    private boolean contentUpdated;

    public PhetContentScraper(String url, File destinationDir) {
        this.url = url;
        this.destinationDirectory = destinationDir;
        langugageList = new ArrayList<>();
        languageMapUpdate = new HashMap<>();
        this.title = url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: <phet html url> <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        try {
            new PhetContentScraper(args[0], new File(args[1])).scrapeContent();
        } catch (IOException e) {
            System.err.println("Exception running scrapeContent");
            e.printStackTrace();
        }

    }


    public void scrapeContent() throws IOException {

        URL simulationUrl = new URL(url);
        destinationDirectory.mkdirs();

        simulationDoc = Jsoup.connect(url).get();

        if (!simulationDoc.select("div.simulation-main-image-panel a span").hasClass("html-badge")) {
            throw new IllegalArgumentException("File Type not supported");
        }

        aboutText = simulationDoc.getElementById("about").html();
        aboutDescription = Jsoup.parse(aboutText).select("p.simulation-panel-indent").text();

        boolean contentUpdated = false;
        for (Element englishLink : simulationDoc.select("div.simulation-main-image-panel a.phet-button[href]")) {

            String hrefLink = englishLink.attr("href");

            File englishLocation = new File(destinationDirectory, "en");
            englishLocation.mkdirs();

            if (hrefLink.contains("download")) {
                contentUpdated = downloadContent(simulationUrl, hrefLink, englishLocation);
                languageMapUpdate.put(englishLocation.getName(), contentUpdated);
                break;
            }
        }

        File languageLocation = null;
        for (Element translations : simulationDoc.select("table.phet-table tr")) {

            for (Element langs : translations.select("td.list-highlight-background a[href]")) {

                String hrefLink = langs.attr("href");

                if (hrefLink.contains("translated")) {

                    String langCode = hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.length());
                    System.out.println(langCode);
                    langugageList.add(langCode);
                    languageLocation = new File(destinationDirectory, langCode);
                    languageLocation.mkdirs();
                    break;
                }
            }

            for (Element links : translations.select("td.img-container a[href]")) {

                String hrefLink = links.attr("href");

                if (hrefLink.contains("download")) {
                    boolean isLanguageUpdated = downloadContent(simulationUrl, hrefLink, languageLocation);
                    languageMapUpdate.put(languageLocation.getName(), isLanguageUpdated);
                    contentUpdated |= isLanguageUpdated;
                    break;
                }

            }

        }

        this.contentUpdated = contentUpdated;
    }

    public Map<String, Boolean> getLanguageUpdatedMap() {
        return languageMapUpdate;
    }

    public boolean isAnyContentUpdated() {
        return contentUpdated;
    }

    /**
     * Find the category for the phet simulation
     *
     * @param contentEntryDao
     * @return a list of categories a single phet simulation could be in
     */
    public ArrayList<ContentEntry> getCategoryRelations(ContentEntryDao contentEntryDao) {

        Elements selected = simulationDoc.select("ul.nav-ul div.link-holder span.selected");

        ArrayList<ContentEntry> categoryRelations = new ArrayList<>();
        for (Element category : selected) {

            if (Arrays.stream(CATEGORY).parallel().noneMatch(category.text()::contains)) {

                String categoryName = category.text(); // category name
                String path = category.parent().attr("href"); // url path to category

                ContentEntry categoryContentEntry = contentEntryDao.findBySourceUrl(path);
                if (categoryContentEntry == null) {
                    categoryContentEntry = new ContentEntry();
                    categoryContentEntry = setContentEntryData(categoryContentEntry, path, categoryName, path, ScraperConstants.ENGLISH_LANG_CODE);
                    categoryContentEntry.setContentEntryUid(contentEntryDao.insert(categoryContentEntry));
                } else {
                    categoryContentEntry = setContentEntryData(categoryContentEntry, path, categoryName, path, ScraperConstants.ENGLISH_LANG_CODE);
                    contentEntryDao.updateContentEntry(categoryContentEntry);
                }

                categoryRelations.add(categoryContentEntry);
                System.out.println(categoryName);
            }
        }

        return categoryRelations;

    }

    private ContentEntry setContentEntryData(ContentEntry entry, String id, String title, String sourceUrl, String lang) {
        entry.setEntryId(id);
        entry.setTitle(title);
        entry.setSourceUrl(sourceUrl);
        entry.setPublisher("Phet");
        entry.setLicenseType(LICENSE_TYPE_CC_BY);
        String[] country = lang.replaceAll("_", "-").split("-");
        entry.setPrimaryLanguage(country[0]);
        entry.setPrimaryLanguageCountry(country.length > 1 ? country[1] : "");
        return entry;
    }

    private boolean downloadContent(URL simulationUrl, String hrefLink, File languageLocation) throws IOException {

        URL link = new URL(simulationUrl, hrefLink);

        File simulationLocation = new File(languageLocation, title);
        simulationLocation.mkdirs();

        URLConnection conn = link.openConnection();

        String fileName = hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.lastIndexOf("?"));
        File simulationFile = new File(simulationLocation, fileName);

        if (!ContentScraperUtil.isFileModified(conn, simulationLocation, fileName)) {
            return false;
        }

        FileUtils.writeStringToFile(new File(simulationLocation, ScraperConstants.ABOUT_HTML), aboutText, ScraperConstants.UTF_ENCODING);

        FileUtils.copyURLToFile(link, simulationFile);
        String simulationTitle = Jsoup.parse(simulationFile, ScraperConstants.UTF_ENCODING).title();
        try {
            ContentScraperUtil.generateTinCanXMLFile(simulationLocation, simulationTitle,
                    languageLocation.getName(), fileName, ScraperConstants.simulationTinCanFile,
                    languageLocation.getName() + "\\" + this.title,
                    aboutDescription, "en");
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
        ContentScraperUtil.zipDirectory(simulationLocation, title, languageLocation);

        return true;
    }


    /**
     * Given a directory of phet simulation content, find the languages it was translated to
     *
     * @param destinationDirectory directory of the all phet simulations
     * @param thumbnailUrl
     * @return a list of languages the phet simulation was translated to
     * @throws IOException
     */
    public ArrayList<ContentEntry> getTranslations(File destinationDirectory, ContentEntryDao contentEntryDao, String thumbnailUrl) throws IOException {

        ArrayList<ContentEntry> translationsEntry = new ArrayList<>();

        for (File translationDir : destinationDirectory.listFiles()) {

            if (translationDir.isDirectory()) {
                String langCode = translationDir.getName();
                if (!langugageList.contains(langCode)) {
                    continue;
                }
                for (File contentDirectory : translationDir.listFiles()) {

                    if (title.equalsIgnoreCase(contentDirectory.getName())) {

                        for (File file : contentDirectory.listFiles()) {

                            if (file.getName().endsWith(".html")) {
                                // TODO recheck entry id for translations
                                String langTitle = Jsoup.parse(file, ScraperConstants.UTF_ENCODING).title();

                                String path = langCode + "/" + this.title;
                                ContentEntry languageContentEntry = contentEntryDao.findBySourceUrl(path);
                                if (languageContentEntry == null) {
                                    languageContentEntry = new ContentEntry();
                                    languageContentEntry = setContentEntryData(languageContentEntry, path, langTitle, path, langCode);
                                    languageContentEntry.setThumbnailUrl(thumbnailUrl);
                                    languageContentEntry.setContentEntryUid(contentEntryDao.insert(languageContentEntry));
                                } else {
                                    languageContentEntry = setContentEntryData(languageContentEntry, path, langTitle, path, langCode);
                                    contentEntryDao.updateContentEntry(languageContentEntry);
                                }

                                translationsEntry.add(languageContentEntry);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return translationsEntry;
    }

    /**
     * @return the title of the simulation in english
     */
    public String getTitle() {
        return title;
    }
}