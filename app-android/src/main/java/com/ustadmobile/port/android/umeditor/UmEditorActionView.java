package com.ustadmobile.port.android.umeditor;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.port.android.view.ContentEditorActivity;

import java.util.List;

import static com.ustadmobile.port.android.view.ContentEditorActivity.UmFormatHelper.ACTIONS_TOOLBAR_INDEX;

/**
 * Customized toolbar view which handles quick action menus on the editor.
 *
 * <b>Operational flow:</b>
 * <p>
 *     Use {@link UmEditorActionView#inflateMenu(int)} to inflate all your menus to be shown
 *     as quick action menus.
 *
 *     Use {@link UmEditorActionView#setQuickActionMenuItemClickListener(OnQuickActionMenuItemClicked)}
 *     to set listener which listens for quick action menu clicks.
 *
 *     Use {@link UmEditorActionView#updateMenu()} to send updated state of the
 *     quick action menu. i.e change background color and icon tint color
 * </p>
 *
 * @author kileha3
 *
 */
public class UmEditorActionView extends Toolbar {

    private OnQuickActionMenuItemClicked onQuickActionMenuItemClicked;

    private boolean isQuickAction = false;

    private List<UmFormat> formatList;

    private ContentEditorActivity.UmFormatHelper umFormatHelper;

    /**
     * Constructor to be used for Java instantiation.
     * @param context application context
     */
    public UmEditorActionView(Context context) {
        super(context);
    }

    /**
     * Constructor to be used when used as Resource tag.
     * @param context application context
     * @param attrs attribute sets
     * @param defStyleAttr style sets
     */
    public UmEditorActionView(Context context, @Nullable AttributeSet attrs,
                              int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Constructor to be used when used as Resource tag.
     * @param context application context
     * @param attrs attribute sets
     */
    public UmEditorActionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set formatting helper
     * @param umFormatHelper format helper instance
     */
    public void setUmFormatHelper(ContentEditorActivity.UmFormatHelper umFormatHelper){
        this.umFormatHelper = umFormatHelper;
    }


    /**
     * Set quick action menu item click listener
     * @param clickListener Listener to be set
     */
    public void setQuickActionMenuItemClickListener(OnQuickActionMenuItemClicked clickListener){
        this.onQuickActionMenuItemClicked = clickListener;
    }

    /**
     * Inflate menu to the toolbar
     * @param resId menu resource id to be inflated
     * @param isQuickAction True if toolbar will be used as quick actions otherwise false.
     */
    public void inflateMenu(int resId, boolean isQuickAction){
        this.isQuickAction = isQuickAction;
        formatList = isQuickAction ? umFormatHelper.getQuickActions() :
                umFormatHelper.getFormatListByType(ACTIONS_TOOLBAR_INDEX);
        inflateMenu(resId);
    }

    @Override
    public void inflateMenu(int resId) {
        super.inflateMenu(resId);

        for(UmFormat format: formatList){
            MenuItem menuItem = getMenu().getItem(formatList.indexOf(format));
            FrameLayout rootView = (FrameLayout) menuItem.getActionView();
            ImageView formatIcon = rootView.findViewById(R.id.format_icon);
            FrameLayout formatHolder = rootView.findViewById(R.id.icon_holder);
            formatIcon.setImageResource(format.getFormatIcon());
            changeState(formatIcon,formatHolder,format.isActive());
            formatHolder.setOnClickListener(v -> {
                if(!isQuickAction){
                    onQuickActionMenuItemClicked.onActionViewClicked(format.getFormatId());
                }else{
                    onQuickActionMenuItemClicked.onQuickActionClicked(format.getFormatCommand());
                }
            });
        }
    }

    /**
     * Update the MenuItem corresponding to the content format.
     */
    public void updateMenu(){
        for(UmFormat umFormat : formatList){
            MenuItem menuItem = findById(umFormat.getFormatId());
            if(menuItem != null){
                FrameLayout rootView = (FrameLayout) menuItem.getActionView();
                ImageView formatIcon = rootView.findViewById(R.id.format_icon);
                FrameLayout formatHolder = rootView.findViewById(R.id.icon_holder);
                changeState(formatIcon,formatHolder, umFormat.isActive() &&
                        umFormatHelper.isTobeHighlighted(umFormat.getFormatCommand()));
            }
        }
    }

    /**
     * Update specific menu item
     * @param formatMenu Newly update Menu item to be set
     */
    public void updateMenu(UmFormat formatMenu){
        MenuItem menuItem = findById(formatMenu.getFormatId());
        if(menuItem != null){
            FrameLayout rootView = (FrameLayout) menuItem.getActionView();
            ImageView formatIcon = rootView.findViewById(R.id.format_icon);
            FrameLayout formatHolder = rootView.findViewById(R.id.icon_holder);
            formatIcon.setImageResource(formatMenu.getFormatIcon());
            changeState(formatIcon,formatHolder,formatMenu.isActive() &&
                    umFormatHelper.isTobeHighlighted(formatMenu.getFormatCommand()));
        }
    }


    /**
     * Set menu item visibility
     * @param isVisible True if menu item should be visible otherwise False
     * @param itemIds Menu item id to be affected.
     */
    public void setMenuVisible(boolean isVisible,int ...itemIds){
        if(itemIds.length > 0){
            for(int itemId : itemIds){
                MenuItem menuItem = findById(itemId);
                if(menuItem != null){
                    menuItem.setVisible(isVisible);
                }
            }
        }else {
            for(UmFormat umFormat : formatList){
                MenuItem menuItem = findById(umFormat.getFormatId());
                if(menuItem != null){
                    menuItem.setVisible(isVisible);
                }
            }
        }
    }

    /**
     * Set state of the MenuItem
     * @param formatIcon ImageView as icon holder
     * @param formatHolder FrameLayout as menu holder
     * @param isActivated state which indicate whether the munu is activated ot not.
     */
    private void changeState(ImageView formatIcon,
                             FrameLayout formatHolder, boolean isActivated){
        formatIcon.setColorFilter(ContextCompat.getColor(getContext(),
                isActivated || !isQuickAction? R.color.icons:R.color.text_secondary));
        formatHolder.setBackgroundColor(ContextCompat.getColor(getContext(),
                isQuickAction ? (isActivated ? R.color.content_icon_active:R.color.icons):R.color.primary));
    }

    /**
     * Find exactly menu to be updated by its ID
     * @param itemId id to be found
     * @return MenuItem to be updated
     */
    private MenuItem findById(int itemId){
        MenuItem menuItem = null;
        for(int i = 0; i < getMenu().size() ;i++){
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

        /**
         * Invoked when menu view is clicked
         * @param itemId menu item id
         */
        void onActionViewClicked(int itemId);
    }
}
