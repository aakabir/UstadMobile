package com.ustadmobile.lib.contentscrapers.edraakK12;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;


/**
 * The Edraak Website uses json to generate their website to get all the courses and all the content within them.
 * https://programs.edraak.org/api/component/5a6087f46380a6049b33fc19/?states_program_id=41
 * <p>
 * Each section of the website is made out of categories and sections which follows the structure of the json
 * <p>
 * The main json has a component type named MainContentTrack
 * This has 6 children which are the main categories found in the website, they have a component type named Section
 * <p>
 * Each Section has list of Subsections or Course Content
 * SubSections are identified by the component type named SubSection
 * SubSections has list of Course Content
 * Course Content contains a Quiz(list of questions) or a Course that has video and list a questions.
 * Courses and Quizzes are both identified with the component type named ImportedComponent
 * <p>
 * The goal of the index class is to find all the importedComponent by going to the child of each component type
 * until the component type found is ImportedComponent. Once it is found, EdraakK12ContentScraper
 * will decide if its a quiz or course and scrap its content
 */
public class IndexEdraakK12Content {

    private List<OpdsEntryWithRelations> entryWithRelationsList;
    private List<OpdsEntryParentToChildJoin> parentToChildJoins;
    private URL url;
    private File destinationDirectory;
    private ContentResponse response;


    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: <edraak k12 json url> <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        new IndexEdraakK12Content().findContent(args[0], new File(args[1]));
    }


    /**
     * Given a url and destination directory, look for importedcomponent in the response object of the url to save its content
     *
     * @param urlString      url for edraak content
     * @param destinationDir directory the content will be saved
     */
    public void findContent(String urlString, File destinationDir) {

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            System.out.println("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDir.mkdirs();
        destinationDirectory = destinationDir;

        try {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            response = new GsonBuilder().disableHtmlEscaping().create().fromJson(IOUtils.toString(connection.getInputStream(), UTF_ENCODING), ContentResponse.class);
        } catch (IOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("JSON INVALID", e.getCause());
        }

        entryWithRelationsList = new ArrayList<>();
        parentToChildJoins = new ArrayList<>();

        OpdsEntryWithRelations edraakEntry = new OpdsEntryWithRelations(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()),
                "https://www.edraak.org/k12/", "Edraak K12");


        OpdsEntryWithRelations parentEntry = new OpdsEntryWithRelations(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()),
                urlString.substring(0, urlString.indexOf("component/")) + response.id, response.title);

        OpdsEntryParentToChildJoin join = new OpdsEntryParentToChildJoin(edraakEntry.getUuid(),
                parentEntry.getUuid(), 0);


        entryWithRelationsList.add(edraakEntry);
        entryWithRelationsList.add(parentEntry);
        parentToChildJoins.add(join);

        findImportedComponent(response, parentEntry);

    }

    private void findImportedComponent(ContentResponse parent, OpdsEntryWithRelations parentEntry) {

        if (ContentScraperUtil.isImportedComponent(parent.component_type)) {

            // found the last child
            EdraakK12ContentScraper scraper = new EdraakK12ContentScraper(
                    EdraakK12ContentScraper.generateUrl(url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? (":" + url.getPort()) : "") + "/api/", parent.id, parent.program == 0 ? response.program : parent.program),
                    destinationDirectory);
            try {
                scraper.scrapeContent();
            } catch (Exception e) {
                System.err.println(e.getCause());
                return;
            }

            OpdsLink newEntryLink = new OpdsLink(parentEntry.getUuid(), "application/zip",
                    destinationDirectory.getName() + "/" + parent.id + ".zip", OpdsEntry.LINK_REL_ACQUIRE);
            newEntryLink.setLength(new File(destinationDirectory, parent.id + ".zip").length());
            parentEntry.setLinks(Collections.singletonList(newEntryLink));

        } else {

            for (ContentResponse children : parent.children) {

                OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations(
                        UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()),
                        url.toString().substring(0, url.toString().indexOf("component/")) + children.id,
                        children.title);

                OpdsEntryParentToChildJoin join = new OpdsEntryParentToChildJoin(parentEntry.getUuid(),
                        newEntry.getUuid(), children.child_index);

                entryWithRelationsList.add(newEntry);
                parentToChildJoins.add(join);

                findImportedComponent(children, parentEntry);

            }

        }
    }


    /**
     * Generate the url based on the different parameters
     *
     * @param contentId      unique id of the course
     * @param baseUrl        baseurl for edraak
     * @param programId      program id for the course
     * @param destinationDir directory where the course will be saved
     */
    public void findContent(String contentId, String baseUrl, int programId, File destinationDir) {
        findContent(baseUrl + "component/" + contentId + "/?states_program_id=" + programId, destinationDir);
    }


}