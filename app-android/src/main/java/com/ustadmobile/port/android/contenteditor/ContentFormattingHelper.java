package com.ustadmobile.port.android.contenteditor;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.ContentEditorView;

import java.util.ArrayList;
import java.util.List;

import static com.ustadmobile.core.view.ContentEditorView.*;

/**
 * Class which handles all content formatting tasks
 *
 * <b>Operation flow</b>
 * <p>
 *     Use {@link ContentFormattingHelper#getFormatListByType(int)} to get
 *     all formats by their type. i.e we have two types (Text and Paragraph)
 *
 *     Use {@link ContentFormattingHelper#getFormatByCommand(String)} to get
 *     a content formatting by its command name.
 *
 *     Use {@link ContentFormattingHelper#getAllFormats()} to get the list of all content formats
 *
 *     Use {@link ContentFormattingHelper#updateFormat(ContentFormat)} to update specific
 *     content format. i.e when active status changes.
 * </p>
 *
 * @see ContentEditorView for the command list
 * @author kileha3
 *
 */
public class ContentFormattingHelper {

    private static final int FORMATTING_TEXT_INDEX = 0;

    private static final int FORMATTING_PARAGRAPH_INDEX = 1;

    public static final int FORMATTING_ACTIONS_INDEX = 2;

    private static ContentFormattingHelper instance;

    private static List<ContentFormat> formatList = new ArrayList<>();

    public static ContentFormattingHelper getInstance() {
        if(instance == null){
            instance = new ContentFormattingHelper();
            prepareFormattingList();
        }
        return instance;
    }


    private static void prepareFormattingList(){
        List<ContentFormat> mText = new ArrayList<>();
        List<ContentFormat> mDirection = new ArrayList<>();
        List<ContentFormat> mParagraph = new ArrayList<>();
        mText.add(new ContentFormat(R.drawable.ic_format_bold_black_24dp,
                TEXT_FORMAT_TYPE_BOLD,false,FORMATTING_TEXT_INDEX));
        mText.add(new ContentFormat(R.drawable.ic_format_italic_black_24dp,
                TEXT_FORMAT_TYPE_ITALIC,false,FORMATTING_TEXT_INDEX));
        mText.add(new ContentFormat(R.drawable.ic_format_strikethrough_black_24dp,
                TEXT_FORMAT_TYPE_STRIKE,false,FORMATTING_TEXT_INDEX));
        mText.add(new ContentFormat(R.drawable.ic_format_underlined_black_24dp,
                TEXT_FORMAT_TYPE_UNDERLINE,false,FORMATTING_TEXT_INDEX));
        mText.add(new ContentFormat(R.drawable.ic_format_size_black_24dp,
                TEXT_FORMAT_TYPE_FONT,false,FORMATTING_TEXT_INDEX));
        mText.add(new ContentFormat(R.drawable.ic_number_superscript,
                TEXT_FORMAT_TYPE_SUP,false,FORMATTING_TEXT_INDEX));
        mText.add(new ContentFormat(R.drawable.ic_number_subscript,
                TEXT_FORMAT_TYPE_SUB,false,FORMATTING_TEXT_INDEX));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_align_justify_black_24dp,
                PARAGRAPH_FORMAT_ALIGN_JUSTIFY, false,FORMATTING_PARAGRAPH_INDEX));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_align_right_black_24dp,
                PARAGRAPH_FORMAT_ALIGN_RIGHT,false,FORMATTING_PARAGRAPH_INDEX));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_align_center_black_24dp,
                PARAGRAPH_FORMAT_ALIGN_CENTER,false,FORMATTING_PARAGRAPH_INDEX));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_align_left_black_24dp,
                PARAGRAPH_FORMAT_ALIGN_LEFT,false,FORMATTING_PARAGRAPH_INDEX));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_list_numbered_black_24dp,
                PARAGRAPH_FORMAT_LIST_ORDERED,false,FORMATTING_PARAGRAPH_INDEX));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_list_bulleted_black_24dp,
                PARAGRAPH_FORMAT_LIST_UNORDERED,false,FORMATTING_PARAGRAPH_INDEX));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_indent_increase_black_24dp,
                PARAGRAPH_FORMAT_INDENT_INCREASE,false,FORMATTING_PARAGRAPH_INDEX));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_indent_decrease_black_24dp,
                PARAGRAPH_FORMAT_INDENT_DECREASE,false,FORMATTING_PARAGRAPH_INDEX));
        mDirection.add(new ContentFormat(R.drawable.ic_format_textdirection_l_to_r_white_24dp,
                ACTION_TEXT_DIRECTION,false,FORMATTING_ACTIONS_INDEX));
        formatList.addAll(mText);
        formatList.addAll(mParagraph);
        formatList.addAll(mDirection);
    }

    /**
     * Get list of all formats by type
     * @param formatType type to be found
     * @return found list of all formats of
     */
    public List<ContentFormat> getFormatListByType(int formatType) {
        List<ContentFormat> formats = new ArrayList<>();
        for(ContentFormat format: formatList){
            if(format.getFormatType() == formatType){
                formats.add(format);
            }
        }
        return formats;
    }

    /**
     * Get content format by formatting tag
     * @param command formatting command to be found
     * @return found content format
     */
    public ContentFormat getFormatByCommand(String command){
        ContentFormat contentFormat = null;
        for(ContentFormat format: formatList){
            if(format.getFormatCommand().equals(command)){
                contentFormat = format;
                break;
            }
        }
        return contentFormat;
    }

    /**
     * Update content format status
     * @param contentFormat updated content format
     */
    public void updateFormat(ContentFormat contentFormat) {
        int formatIndex = 0;
        for(ContentFormat format: formatList){
            if(format.getFormatCommand().equals(contentFormat.getFormatCommand())){
                formatIndex = formatList.indexOf(format);
                break;
            }
        }
        formatList.set(formatIndex,contentFormat);
    }

    /**
     * Get all content formats
     * @return list of all content formats
     */
    public List<ContentFormat> getAllFormats(){
        return formatList;
    }
}
