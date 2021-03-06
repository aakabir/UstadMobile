package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModuleResponse {

    public String description;

    public String icon;

    public String kind;

    public String slug;

    public String title;

    public String topicId;

    public String url;

    public List<Tutorial> tutorials;

    public List<ModuleResponse> modules;

    public class Tutorial {

        public List<ContentItem> contentItems;

        public String description;

        public String id;

        public String slug;

        public String title;

        public String url;

        public class ContentItem {

            public String contentId;

            public String description;

            public String kind;

            public String thumbnailUrl;

            public String slug;

            public String nodeUrl;

            public String title;

            public int expectedDoNCount;

            public DownloadUrl downloadUrls;

            public class DownloadUrl {

                String mp4;

                String png;

                @SerializedName("mp4-low")
                String mp4Low;

            }


        }
    }

    public SubjectChallenge subjectChallenge;

    public class SubjectChallenge {

        // TODO

    }

}
