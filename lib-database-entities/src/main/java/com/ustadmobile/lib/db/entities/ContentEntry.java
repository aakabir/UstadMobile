package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;

import static com.ustadmobile.lib.db.entities.ContentEntry.TABLE_ID;

/**
 * Entity that represents content as it is browsed by the user. A ContentEntry can be either:
 * 1. An actual piece of content (e.g. book, course, etc), in which case there should be an associated
 * ContentEntryFile.
 * 2. A navigation directory (e.g. a category as it is scraped from another site, etc), in which case
 * there should be the appropriate ContentEntryParentChildJoin entities present.
 */
@UmEntity(tableId = TABLE_ID)
public class ContentEntry {

    public static final int TABLE_ID = 42;

    public static final int LICENSE_TYPE_CC_BY = 1;

    public static final int LICENSE_TYPE_CC_BY_SA = 2;

    public static final int LICENSE_TYPE_CC_BY_SA_NC = 3;

    public static final int LICENSE_TYPE_CC_BY_NC = 4;

    public static final int ALL_RIGHTS_RESERVED = 5;

    public static final int LICESNE_TYPE_CC_BY_NC_SA = 6;

    public static final int PUBLIC_DOMAIN = 7;

    public static final int UNDEFINED_TYPE = 0;

    public static final int COLLECTION_TYPE = 1;

    public static final int EBOOK_TYPE = 2;

    public static final int INTERACTIVE_EXERICSE_TYPE = 3;

    public static final int VIDEO_TYPE = 4;

    public static final int AUDIO_TYPE = 5;

    public static final int DOCUMENT_TYPE = 6;

    public static final int ARTICLE_TYPE = 7;

    @UmPrimaryKey(autoGenerateSyncable = true)
    private long contentEntryUid;

    private String title;

    private String description;

    private String entryId;

    private String author;

    private String publisher;

    private int licenseType;

    private String licenseName;

    private String licenseUrl;

    private String sourceUrl;

    private String thumbnailUrl;

    private long lastModified;

    //TODO: Migration : add to migration
    @UmIndexField
    private long primaryLanguageUid;

    private long languageVariantUid;

    private boolean leaf;

    private boolean publik;

    private int contentTypeFlag;

    @UmSyncLocalChangeSeqNum
    private long contentEntryLocalChangeSeqNum;

    @UmSyncMasterChangeSeqNum
    private long contentEntryMasterChangeSeqNum;

    @UmSyncLastChangedBy
    private int contentEntryLastChangedBy;

    public ContentEntry() {

    }

    public ContentEntry(String title, String description, boolean leaf, boolean publik) {
        this.title = title;
        this.description = description;
        this.leaf = leaf;
        this.publik = publik;
    }

    public long getLanguageVariantUid() {
        return languageVariantUid;
    }

    public void setLanguageVariantUid(long languageVariantUid) {
        this.languageVariantUid = languageVariantUid;
    }

    public long getPrimaryLanguageUid() {
        return primaryLanguageUid;
    }

    public void setPrimaryLanguageUid(long primaryLanguageUid) {
        this.primaryLanguageUid = primaryLanguageUid;
    }

    public long getContentEntryUid() {
        return contentEntryUid;
    }

    public void setContentEntryUid(long contentEntryUid) {
        this.contentEntryUid = contentEntryUid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the embedded unique ID which can be found in the underlying file, if any. For
     * example the EPUB identifier for EPUB files, or the ID attribute of an xAPI zip file.
     *
     * @return The embedded unique ID which can be found in the underlying file
     */
    public String getEntryId() {
        return entryId;
    }

    /**
     * Set the embedded unique ID which can be found in the underlying file, if any. For
     * example the EPUB identifier for EPUB files, or the ID attribute of an xAPI zip file.
     *
     * @param entryId The embedded unique ID which can be found in the underlying file
     */
    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(int licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    /**
     * Get the original URL this resource came from. In the case of resources that
     * were generated by scraping, this refers to the URL that the scraper targeted to
     * generated the resource.
     *
     * @return the original URL this resource came from
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Set the original URL this resource came from. In the case of resources that
     * were generated by scraping, this refers to the URL that the scraper targeted to
     * generated the resource.
     *
     * @param sourceUrl the original URL this resource came from
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }


    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }


    public long getContentEntryLocalChangeSeqNum() {
        return contentEntryLocalChangeSeqNum;
    }

    public void setContentEntryLocalChangeSeqNum(long contentEntryLocalChangeSeqNum) {
        this.contentEntryLocalChangeSeqNum = contentEntryLocalChangeSeqNum;
    }

    public long getContentEntryMasterChangeSeqNum() {
        return contentEntryMasterChangeSeqNum;
    }

    public void setContentEntryMasterChangeSeqNum(long contentEntryMasterChangeSeqNum) {
        this.contentEntryMasterChangeSeqNum = contentEntryMasterChangeSeqNum;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public int getContentEntryLastChangedBy() {
        return contentEntryLastChangedBy;
    }

    public void setContentEntryLastChangedBy(int contentEntryLastChangedBy) {
        this.contentEntryLastChangedBy = contentEntryLastChangedBy;
    }

    public int getContentTypeFlag() {
        return contentTypeFlag;
    }

    public void setContentTypeFlag(int contentTypeFlag) {
        this.contentTypeFlag = contentTypeFlag;
    }

    /**
     * Represents if this content entry is public for anyone to use
     *
     * @return true if this content entry is public for anyone to use, false otherwise
     */
    public boolean isPublik() {
        return publik;
    }

    /**
     * Set if this content entry is public for anyone to use
     *
     * @param publik true if this content entry is public for anyone to use, false otherwise
     */
    public void setPublik(boolean publik) {
        this.publik = publik;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentEntry entry = (ContentEntry) o;

        if (contentEntryUid != entry.contentEntryUid) return false;
        if (licenseType != entry.licenseType) return false;
        if (primaryLanguageUid != entry.primaryLanguageUid) return false;
        if (languageVariantUid != entry.languageVariantUid) return false;
        if (leaf != entry.leaf) return false;
        if (contentTypeFlag != entry.contentTypeFlag) return false;
        if (title != null ? !title.equals(entry.title) : entry.title != null) return false;
        if (description != null ? !description.equals(entry.description) : entry.description != null)
            return false;
        if (entryId != null ? !entryId.equals(entry.entryId) : entry.entryId != null) return false;
        if (author != null ? !author.equals(entry.author) : entry.author != null) return false;
        if (publisher != null ? !publisher.equals(entry.publisher) : entry.publisher != null)
            return false;
        if (licenseName != null ? !licenseName.equals(entry.licenseName) : entry.licenseName != null)
            return false;
        if (licenseUrl != null ? !licenseUrl.equals(entry.licenseUrl) : entry.licenseUrl != null)
            return false;
        if (sourceUrl != null ? !sourceUrl.equals(entry.sourceUrl) : entry.sourceUrl != null)
            return false;

        return thumbnailUrl != null ? thumbnailUrl.equals(entry.thumbnailUrl) : entry.thumbnailUrl == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (contentEntryUid ^ (contentEntryUid >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (entryId != null ? entryId.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (publisher != null ? publisher.hashCode() : 0);
        result = 31 * result + licenseType;
        result = 31 * result + (licenseName != null ? licenseName.hashCode() : 0);
        result = 31 * result + (licenseUrl != null ? licenseUrl.hashCode() : 0);
        result = 31 * result + (sourceUrl != null ? sourceUrl.hashCode() : 0);
        result = 31 * result + (thumbnailUrl != null ? thumbnailUrl.hashCode() : 0);
        result = 31 * result + (int) (primaryLanguageUid ^ (primaryLanguageUid >>> 32));
        result = 31 * result + (int) (languageVariantUid ^ (languageVariantUid >>> 32));
        result = 31 * result + contentTypeFlag;
        result = 31 * result + (leaf ? 1 : 0);
        return result;
    }

}
