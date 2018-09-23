package com.ustadmobile.lib.contentscrapers;

import java.util.Arrays;
import java.util.List;

public class ScraperConstants {

    public static final String CONTENT_JSON = "content.json";
    public static final String QUESTIONS_JSON = "questions.json";
    public static final String ETAG_TXT = "etag.txt";
    public static final String LAST_MODIFIED_TXT = "last-modified.txt";
    public static final String ABOUT_HTML = "about.txt";
    public static final String VIDEO_MP4 = "video.mp4";
    public static final String UTF_ENCODING = "UTF-8";
    public static final String EDRAAK_INDEX_HTML_TAG = "/com/ustadmobile/lib/contentscrapers/edraak/index.html";
    public static final String CK12_INDEX_HTML_TAG = "/com/ustadmobile/lib/contentscrapers/ck12/index.html";
    public static final String JS_TAG = "/com/ustadmobile/lib/contentscrapers/jquery-3.3.1.min.js";
    public static final String MATERIAL_JS_LINK = "/com/ustadmobile/lib/contentscrapers/materialize.min.js";
    public static final String MATERIAL_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/materialize.min.css";
    public static final String REGULAR_ARABIC_FONT_LINK = "/com/ustadmobile/lib/contentscrapers/edraak/DroidNaskh-Regular.woff2";
    public static final String BOLD_ARABIC_FONT_LINK = "/com/ustadmobile/lib/contentscrapers/edraak/DroidNaskh-Bold.woff2";
    public static final String CIRCULAR_CSS_LINK = "/com/ustadmobile/lib/contentscrapers/ck12/css-circular-prog-bar.css";
    public static final String CIRCULAR_CSS_NAME = "css-circular-prog-bar.css";
    public static final String TIMER_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/timer.svg";
    public static final String TIMER_NAME = "timer.svg";
    public static final String TROPHY_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/trophy.svg";
    public static final String TROPHY_NAME = "trophy.svg";
    public static final String CHECK_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/check.svg";
    public static final String CHECK_NAME = "check.svg";

    public static final String MATERIAL_JS = "materialize.min.js";
    public static final String MATERIAL_CSS = "materialize.min.css";

    public static final String brainGenieLink = "braingenie.ck12.org";
    public static final String slideShareLink = "www.slideshare.net";


    public static final List<String> QUESTION_SET_HOLDER_TYPES = Arrays.asList(
           ComponentType.EXCERCISE.getType(), ComponentType.ONLINE.getType(),
           ComponentType.TEST.getType());


    public static final String PNG_EXT = ".png";
    public static final String ARABIC_FONT_REGULAR = "DroidNaskh-Regular.woff2";
    public static final String ARABIC_FONT_BOLD = "DroidNaskh-Bold.woiff2";
    public static final String INDEX_HTML = "index.html";
    public static final String JQUERY_JS = "jquery-3.3.1.min.js";

    public enum QUESTION_TYPE{

        MULTI_CHOICE("multiple-choice"),
        FILL_BLANKS("fill-in-the-blanks"),
        SHORT_ANSWER("short-answer");

        private String type;

        QUESTION_TYPE(String questionType) {
            this.type = questionType;
        }

        public String getType() {
            return type;
        }

    }


    public enum ComponentType{
        MAIN("MainContentTrack"),
        SECTION("Section"),
        SUBSECTION("SubSection"),
        IMPORTED("ImportedComponent"),
        MULTICHOICE("MultipleChoiceQuestion"),
        QUESTIONSET("QuestionSet"),
        TEST("Test"),
        VIDEO("Video"),
        EXCERCISE("Exercise"),
        NUMERIC_QUESTION("NumericResponseQuestion"),
        ONLINE("OnlineLesson");

        private String type;

        ComponentType(String compType) {
            this.type = compType;
        }

        public String getType() {
            return type;
        }
    }

    public enum HtmlName{
        DESC("description"),
        FULL_DESC("full_description"),
        EXPLAIN("explaination"),
        CHOICE("choice"),
        HINT("hint");

        private String name;

        HtmlName(String compType) {
            this.name = compType;
        }

        public String getName() {
            return name;
        }

    }

}