package com.ustadmobile.port.android.contenteditor;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.view.ContentEditorView;

import java.util.ArrayList;
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

    private List<StateChangeDispatcher> dispatcherList = new CopyOnWriteArrayList<>();

    public static ContentFormattingHelper getInstance() {
        if(instance == null){
            instance = new ContentFormattingHelper();
            prepareFormattingList();
        }
        return instance;
    }


    /**
     * Construct quick action menu items
     * @return list of all quick action menus
     */
    public List<ContentFormat> getQuickActions(){
        List<ContentFormat> quickActions = new ArrayList<>();

        ContentFormat bold = getFormatByCommand(TEXT_FORMAT_TYPE_BOLD);
        bold.setFormatId(R.id.content_action_bold);
        quickActions.add(bold);

        ContentFormat italic = getFormatByCommand(TEXT_FORMAT_TYPE_ITALIC);
        italic.setFormatId(R.id.content_action_italic);
        quickActions.add(italic);

        ContentFormat underline = getFormatByCommand(TEXT_FORMAT_TYPE_UNDERLINE);
        underline.setFormatId(R.id.content_action_underline);
        quickActions.add(underline);

        ContentFormat strikeThrough = getFormatByCommand(TEXT_FORMAT_TYPE_STRIKE);
        strikeThrough.setFormatId(R.id.content_action_strike_through);
        quickActions.add(strikeThrough);

        ContentFormat ordered = getFormatByCommand(PARAGRAPH_FORMAT_LIST_ORDERED);
        ordered.setFormatId(R.id.content_action_ordered_list);
        quickActions.add(ordered);

        ContentFormat unordered = getFormatByCommand(PARAGRAPH_FORMAT_LIST_UNORDERED);
        unordered.setFormatId(R.id.content_action_uordered_list);
        quickActions.add(unordered);

        ContentFormat iIncrease = getFormatByCommand(PARAGRAPH_FORMAT_INDENT_INCREASE);
        iIncrease.setFormatId(R.id.content_action_indent);
        quickActions.add(iIncrease);

        ContentFormat iDecrease = getFormatByCommand(PARAGRAPH_FORMAT_INDENT_DECREASE);
        iDecrease.setFormatId(R.id.content_action_outdent);
        quickActions.add(iDecrease);
        return quickActions;
    }


    private static void prepareFormattingList(){
        List<ContentFormat> mText = new ArrayList<>();
        List<ContentFormat> mDirection = new ArrayList<>();
        List<ContentFormat> mParagraph = new ArrayList<>();
        mText.add(new ContentFormat(R.drawable.ic_format_bold_black_24dp,
                TEXT_FORMAT_TYPE_BOLD,false,FORMATTING_TEXT_INDEX));
        mText.add(new ContentFormat(R.drawable.ic_format_italic_black_24dp,
                TEXT_FORMAT_TYPE_ITALIC,false,FORMATTING_TEXT_INDEX));
        mText.add(new ContentFormat(R.drawable.ic_format_underlined_black_24dp,
                TEXT_FORMAT_TYPE_UNDERLINE,false,FORMATTING_TEXT_INDEX));
        mText.add(new ContentFormat(R.drawable.ic_format_strikethrough_black_24dp,
                TEXT_FORMAT_TYPE_STRIKE,false,FORMATTING_TEXT_INDEX));
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
                ACTION_TEXT_DIRECTION_LTR,false,FORMATTING_ACTIONS_INDEX));
        mDirection.add(new ContentFormat(R.drawable.ic_format_textdirection_r_to_l_white_24dp,
                ACTION_TEXT_DIRECTION_RTL,false,FORMATTING_ACTIONS_INDEX));
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
        dispatchUpdate(contentFormat);
    }

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

    public void dispatchUpdate(ContentFormat format){
        for(StateChangeDispatcher dispatcher: dispatcherList){
            dispatcher.onStateChanged(format);
        }
    }


    public void destroy(){
        if(dispatcherList != null && dispatcherList.size()>0){
            dispatcherList.clear();
        }
    }


    public static boolean isTobeHighlighted(String command){
        return !command.equals(TEXT_FORMAT_TYPE_FONT)
                && !command.equals(PARAGRAPH_FORMAT_INDENT_DECREASE)
                && !command.equals(PARAGRAPH_FORMAT_INDENT_INCREASE);
    }

    /**
     * Get all content formats
     * @return list of all content formats
     */
    public List<ContentFormat> getAllFormats(){
        return formatList;
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
