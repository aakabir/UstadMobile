package com.ustadmobile.port.android.contenteditor;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.ContentEditorView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
 *     Use {@link ContentFormattingHelper#updateFormat(ContentFormat)} to update specific
 *     content format. i.e when active status changes.
 * </p>
 *
 * @see ContentEditorView for the command list
 * @author kileha3
 *
 */
public class ContentFormattingHelper {

    /**
     * Flag to indicate all text formats
     */
    private static final int FORMATTING_TEXT_INDEX = 0;

    /**
     * Flag to indicate all paragraph formats
     */
    private static final int FORMATTING_PARAGRAPH_INDEX = 1;

    /**
     * Flag to indicate all font formats
     */
    private static final int FORMATTING_FONT_INDEX = 2;

    /**
     * Flag to indicate all toolbar action formats
     */
    public static final int ACTIONS_TOOLBAR_INDEX = 3;

    /**
     * Flag to indicate all language directionality formats
     */
    private static final int LANGUAGE_DIRECTIONALITY = 4;

    private static ContentFormattingHelper instance;

    private static List<ContentFormat> formatList = new ArrayList<>();

    private static List<ContentFormat> quickActionList = new ArrayList<>();

    private List<StateChangeDispatcher> dispatcherList = new CopyOnWriteArrayList<>();

    /**
     * Get ContentFormattingHelper instance
     * @return New/Existing ContentFormattingHelper instance
     */
    public static ContentFormattingHelper getInstance() {
        if(instance == null){
            instance = new ContentFormattingHelper();
            formatList = prepareFormattingList();
            quickActionList = prepareQuickActions();
        }
        return instance;
    }


    /**
     * Construct quick action menu items
     * @return list of all quick action menus
     */
    List<ContentFormat> getQuickActions(){
        return quickActionList;
    }

    private static List<ContentFormat> prepareQuickActions(){
        List<ContentFormat> quickActions = new ArrayList<>();
        try{
            quickActions.add(getFormatByCommand(TEXT_FORMAT_TYPE_BOLD));
            quickActions.add(getFormatByCommand(TEXT_FORMAT_TYPE_ITALIC));
            quickActions.add(getFormatByCommand(TEXT_FORMAT_TYPE_UNDERLINE));
            quickActions.add(getFormatByCommand(TEXT_FORMAT_TYPE_STRIKE));
            quickActions.add(getFormatByCommand(PARAGRAPH_FORMAT_LIST_ORDERED));
            quickActions.add(getFormatByCommand(PARAGRAPH_FORMAT_LIST_UNORDERED));
            quickActions.add(getFormatByCommand(PARAGRAPH_FORMAT_INDENT_INCREASE));
            quickActions.add(getFormatByCommand(PARAGRAPH_FORMAT_INDENT_DECREASE));
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return quickActions;
    }

    private static List<ContentFormat> prepareFormattingList(){
        List<ContentFormat> mText = new ArrayList<>();
        List<ContentFormat> mDirection = new ArrayList<>();
        List<ContentFormat> mParagraph = new ArrayList<>();
        List<ContentFormat> mToolbar = new ArrayList<>();

        List<ContentFormat> mFont = new ArrayList<>();

        mText.add(new ContentFormat(R.drawable.ic_format_bold_black_24dp, TEXT_FORMAT_TYPE_BOLD,
                false,FORMATTING_TEXT_INDEX, R.id.content_action_bold));
        mText.add(new ContentFormat(R.drawable.ic_format_italic_black_24dp, TEXT_FORMAT_TYPE_ITALIC,
                false,FORMATTING_TEXT_INDEX,R.id.content_action_italic));
        mText.add(new ContentFormat(R.drawable.ic_format_underlined_black_24dp,
                TEXT_FORMAT_TYPE_UNDERLINE,false,FORMATTING_TEXT_INDEX,
                R.id.content_action_underline));
        mText.add(new ContentFormat(R.drawable.ic_format_strikethrough_black_24dp,
                TEXT_FORMAT_TYPE_STRIKE,false,FORMATTING_TEXT_INDEX,
                R.id.content_action_strike_through));
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
                PARAGRAPH_FORMAT_LIST_ORDERED,false,FORMATTING_PARAGRAPH_INDEX,
                R.id.content_action_ordered_list));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_list_bulleted_black_24dp,
                PARAGRAPH_FORMAT_LIST_UNORDERED,false,FORMATTING_PARAGRAPH_INDEX,
                R.id.content_action_uordered_list));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_indent_increase_black_24dp,
                PARAGRAPH_FORMAT_INDENT_INCREASE,false,FORMATTING_PARAGRAPH_INDEX,
                R.id.content_action_indent));
        mParagraph.add(new ContentFormat(R.drawable.ic_format_indent_decrease_black_24dp,
                PARAGRAPH_FORMAT_INDENT_DECREASE,false,FORMATTING_PARAGRAPH_INDEX,
                R.id.content_action_outdent));

        mDirection.add(new ContentFormat(R.drawable.ic_format_textdirection_l_to_r_white_24dp,
                ACTION_TEXT_DIRECTION_LTR,true,LANGUAGE_DIRECTIONALITY,
                R.id.direction_rightToLeft, R.string.content_direction_rtl));
        mDirection.add(new ContentFormat(R.drawable.ic_format_textdirection_r_to_l_white_24dp,
                ACTION_TEXT_DIRECTION_RTL,false,LANGUAGE_DIRECTIONALITY,
                R.id.direction_leftToRight,R.string.content_direction_ltr));

        mFont.add(new ContentFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                FORMATTING_FONT_INDEX, 8,R.string.content_font_8));
        mFont.add(new ContentFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                FORMATTING_FONT_INDEX, 10,R.string.content_font_10));
        mFont.add(new ContentFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                FORMATTING_FONT_INDEX, 12,R.string.content_font_12));
        mFont.add(new ContentFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                FORMATTING_FONT_INDEX, 14,R.string.content_font_14));
        mFont.add(new ContentFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                FORMATTING_FONT_INDEX, 18, R.string.content_font_18));
        mFont.add(new ContentFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                FORMATTING_FONT_INDEX, 24,R.string.content_font_24));
        mFont.add(new ContentFormat(0, TEXT_FORMAT_TYPE_FONT,false,
                FORMATTING_FONT_INDEX, 36,R.string.content_font_36));


        mToolbar.add(new ContentFormat(R.drawable.ic_undo_white_24dp,
                ACTION_UNDO,false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_undo));
        mToolbar.add(new ContentFormat(R.drawable.ic_redo_white_24dp,
                ACTION_REDO,false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_redo));
        mToolbar.add(new ContentFormat(R.drawable.ic_text_format_white_24dp,
                "",false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_format));
        mToolbar.add(new ContentFormat(R.drawable.fab_add,
                "",false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_insert));
        mToolbar.add(new ContentFormat(R.drawable.ic_document_preview,
                "",false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_preview));
        mToolbar.add(new ContentFormat(R.drawable.ic_format_textdirection_l_to_r_white_24dp,
                ACTION_TEXT_DIRECTION_LTR,false,ACTIONS_TOOLBAR_INDEX,
                R.id.content_action_direction));
        mToolbar.add(new ContentFormat(R.drawable.ic_menu_white_24dp,
                "",false,ACTIONS_TOOLBAR_INDEX,R.id.content_action_pages));

        List<ContentFormat> allFormats = new ArrayList<>();
        allFormats.addAll(mText);
        allFormats.addAll(mParagraph);
        allFormats.addAll(mDirection);
        allFormats.addAll(mToolbar);
        allFormats.addAll(mFont);
        return allFormats;
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
     * Get list of all formats by type
     * @param formatId id to be found
     * @return found list of all formats of
     */
    public ContentFormat getFormatById(int formatId,int formatType) {
        ContentFormat contentFormat = null;
        for(ContentFormat format: getFormatListByType(formatType)){
            if(format.getFormatId() == formatId){
                contentFormat = format;
                break;
            }
        }
        return contentFormat;
    }

    /**
     * Get content format by formatting tag
     * @param command formatting command to be found
     * @return found content format
     */
    public static ContentFormat getFormatByCommand(String command){
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
        for(ContentFormat format: formatList){
            if(format.getFormatCommand().equals(contentFormat.getFormatCommand())){
                int formatIndex = formatList.indexOf(format);
                formatList.get(formatIndex).setActive(contentFormat.isActive());
                break;
            }
        }
        dispatchUpdate(contentFormat);
    }


    /**
     * Get list of all directionality formatting
     * @param activeFormat Directionality format to be activated
     * @return List of all formats
     */
    public  List<ContentFormat> getLanguageDirectionalityList(ContentFormat activeFormat){
        List<ContentFormat> directionality = getFormatListByType(LANGUAGE_DIRECTIONALITY);
        for(ContentFormat format: directionality){
            if(activeFormat != null){
                directionality.get(directionality.indexOf(format))
                        .setActive(format.getFormatId() == activeFormat.getFormatId());

            }
        }
        return directionality;
    }

    /**
     * Get all font format list
     * @param activeFormat Font format to be activated
     * @return List of all fonts
     */
    public  List<ContentFormat> getFontList(ContentFormat activeFormat){
        List<ContentFormat> fontList = getFormatListByType(FORMATTING_FONT_INDEX);
        for(ContentFormat format: fontList){
            if(activeFormat != null){
                fontList.get(fontList.indexOf(format))
                        .setActive(format.getFormatId() == activeFormat.getFormatId());
            }
        }
        return fontList;
    }

    /**
     * Prevent all justification to be active at the same time, only one type at a time.
     * @param command current active justification command.
     */
    public void updateOtherJustification(String command){
        String mTag = "Justify";
        List<ContentFormat> paragraphFormatList = getFormatListByType(FORMATTING_PARAGRAPH_INDEX);
        for(ContentFormat format: paragraphFormatList){
            if(format.getFormatCommand().contains(mTag) && command.contains(mTag)
                    && !format.getFormatCommand().equals(command)){
                int index = paragraphFormatList.indexOf(format);
                format.setActive(false);
                formatList.set(index, format);
            }
        }
    }

    /**
     * Prevent all list types to active at the same time, only one at a time.
     * @param command current active list type command.
     */
    public void updateOtherListOrder(String command){
        String mTag = "List";
        List<ContentFormat> listOrdersTypes = listOrderFormats();
        for(ContentFormat format: listOrdersTypes){
            if(format.getFormatCommand().contains(mTag) && command.contains(mTag)
                    && !format.getFormatCommand().equals(command)){
                int index = listOrdersTypes.indexOf(format);
                format.setActive(false);
                formatList.set(index, format);
            }
        }
    }

    private List<ContentFormat> listOrderFormats(){
        List<ContentFormat> listOrders = new ArrayList<>();
        for(ContentFormat format: formatList){
            if(format.getFormatCommand().contains("List")){
               listOrders.add(format);
            }
        }
        return listOrders;
    }

    public void setStateDispatcher(StateChangeDispatcher stateDispatcher){
        if(dispatcherList != null){
            dispatcherList.add(stateDispatcher);
        }
    }

    /**
     * Dispatch update to both content formatting panels and quick actions.
     * @param format updated format to be dispatched
     */
    private void dispatchUpdate(ContentFormat format){
        for(StateChangeDispatcher dispatcher: dispatcherList){
            dispatcher.onStateChanged(format);
        }
    }

    /**
     * Delete all listeners from the list on activity destroy
     */
    public void destroy(){
        if(dispatcherList != null && dispatcherList.size()>0){
            dispatcherList.clear();
        }
    }


    /**
     * Check if the format is valid for highlight or not, for those format which
     * deals with increment like indentation they are not fit for active state
     * @param command format command to be checked for validity
     * @return True if valid otherwise False.
     */
    public static boolean isTobeHighlighted(String command){
        return !command.equals(TEXT_FORMAT_TYPE_FONT)
                && !command.equals(PARAGRAPH_FORMAT_INDENT_DECREASE)
                && !command.equals(PARAGRAPH_FORMAT_INDENT_INCREASE);
    }


    /**
     * Interface to listen for the state change of the formatting item
     */
    public interface StateChangeDispatcher {

        /**
         * Invoked when formatting item has been updated
         * @param format updated content format
         */
        void onStateChanged(ContentFormat format);
    }
}
