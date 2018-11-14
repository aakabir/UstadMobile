package com.ustadmobile.port.android.contenteditor;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.toughra.ustadmobile.R;

import java.util.List;

/**
 * Customized toolbar view which handles quick action menus on the editor.
 *
 * <b>Operational flow:</b>
 * <p>
 *     Use {@link BottomToolbarView#inflateMenu(int)} to inflate all your menus to be shown
 *     as quick action menus.
 *
 *     Use {@link BottomToolbarView#setOnQuickActionMenuItemClicked(OnQuickActionMenuItemClicked)}
 *     to set listener which listens for quick action menu clicks.
 *
 *     Use {@link BottomToolbarView#updateMenu(ContentFormat)} to send updated state of the
 *     quick action menu. i.e change background color and icon tint color
 * </p>
 */
public class BottomToolbarView extends Toolbar {

    private OnQuickActionMenuItemClicked onQuickActionMenuItemClicked;

    /**
     * Constructor to be used for Java instantiation.
     * @param context application context
     */
    public BottomToolbarView(Context context) {
        super(context);
    }

    /**
     * Constructor to be used when used as Resource tag.
     * @param context application context
     * @param attrs attribute sets
     * @param defStyleAttr style sets
     */
    public BottomToolbarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Constructor to be used when used as Resource tag.
     * @param context application context
     * @param attrs attribute sets
     */
    public BottomToolbarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * Set quick action menu item click listener
     * @param clickListener Listener to be set
     */
    public void setOnQuickActionMenuItemClicked(OnQuickActionMenuItemClicked clickListener){
        this.onQuickActionMenuItemClicked = clickListener;
    }

    @Override
    public void inflateMenu(int resId) {
        super.inflateMenu(resId);
        List<ContentFormat> formatList = ContentFormattingHelper.getInstance().getQuickActions();
        for(ContentFormat format: formatList){
            MenuItem menuItem = getMenu().getItem(formatList.indexOf(format));
            FrameLayout rootView = (FrameLayout) menuItem.getActionView();
            ImageView imageIcon = rootView.findViewById(R.id.format_icon);
            FrameLayout iconHolder = rootView.findViewById(R.id.icon_holder);
            imageIcon.setImageResource(format.getFormatIcon());
            changeState(imageIcon,iconHolder,format.isActive());
            iconHolder.setOnClickListener(v -> onQuickActionMenuItemClicked
                    .onQuickActionClicked(format.getFormatCommand()));
        }
    }

    /**
     * Update the MenuItem corresponding to the content format.
     * @param format updated content format to be set to the MenuItem.
     */
    public void updateMenu(ContentFormat format){
        MenuItem menuItem = findById(format.getFormatId());
        if(menuItem != null){
            FrameLayout rootView = (FrameLayout) menuItem.getActionView();
            ImageView imageIcon = rootView.findViewById(R.id.format_icon);
            FrameLayout iconHolder = rootView.findViewById(R.id.icon_holder);
            changeState(imageIcon,iconHolder,format.isActive());
        }
    }

    /**
     * Set state of the MenuItem
     * @param imageIcon ImageView as icon holder
     * @param iconHolder FrameLayout as menu holder
     * @param isActivated state which indicate whether the munu is activated ot not.
     */
    private void changeState(ImageView imageIcon,
                             FrameLayout iconHolder, boolean isActivated){
        imageIcon.setColorFilter(ContextCompat.getColor(getContext(),
                isActivated ? R.color.icons:R.color.text_secondary));
        iconHolder.setBackgroundColor(ContextCompat.getColor(getContext(),
                isActivated ? R.color.content_icon_active:R.color.icons));
    }

    /**
     * Find exactly menu to be updated by its ID
     * @param itemId id to be found
     * @return MenuItem to be updated
     */
    private MenuItem findById(int itemId){
        MenuItem menuItem = null;
        for(int i = 0; i < 7;i++){
            menuItem = getMenu().getItem(i);
            if(menuItem.getItemId() == itemId){
                break;
            }
        }
        return menuItem;
    }

    /**
     * Interface which listen for the clicks on inflated menu.
     */
    public interface OnQuickActionMenuItemClicked {
        /**
         * Invoked when an quick action menu item is clicked.
         * @param command command to be executed.
         */
        void onQuickActionClicked(String command);
    }
}
