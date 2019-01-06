package com.ustadmobile.port.android.contenteditor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Customized PopUpWindow which handle both language directionality and content font size option.
 * It can handle any option with format as content list.
 *
 * <b>Operational flow:</b>
 * <p>
 *     Use {@link UmEditorPopUpView#setMenuList(List)} to to add list of all formats you want to
 *     appear as popup options
 *
 *     Use {@link UmEditorPopUpView#setWidthDimen(int, boolean)} to set width of the popup view
 *
 *     Use {@link UmEditorPopUpView#showIcons(boolean)} to enable icons on the popup list items
 *
 *     Use {@link UmEditorPopUpView#showIcons(boolean)} to showWithListener the options
 * </p>
 *
 * @author kileha3
 *
 */

public class UmEditorPopUpView {

    private Context context;

    private View anchorView;

    private PopupWindow popupWindow;

    private PopUpAdapter popUpAdapter;

    private boolean visible = true;

    private boolean isToolBarAnchored = false;

    private OnPopUpMenuClickListener listener;


    /**
     * Interface which is used to handle popup item clicks
     */
    public interface OnPopUpMenuClickListener {
        /**
         * Invoked when an item is clicked
         * @param format selected format
         */
        void onMenuClicked(UmFormat format);
    }


    /**
     * Constrictor used to initialize PopUp view.
     * @param context Application context
     * @param anchor View where popup view will be anchored
     */
    public UmEditorPopUpView(Context context,View anchor) {
        this.context = context;
        this.anchorView = anchor;
        initializePopUpWindow();
    }

    private void initializePopUpWindow(){
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = layoutInflater.inflate(R.layout.popup_item_container_view,null);
        popupWindow = new PopupWindow(rootView);
        popupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(20);
        }

        RecyclerView recyclerView = rootView.findViewById(R.id.menu_items);
        popUpAdapter = new PopUpAdapter(new ArrayList<>());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(popUpAdapter);
    }

    /**
     * Set popup list items
     * @param menuList Lust of all items to be set on popup view
     * @return UmEditorPopUpView instance
     */
    public UmEditorPopUpView setMenuList(List<UmFormat> menuList){
        popUpAdapter.setMenuList(menuList);
        return this;
    }

    /**
     * Set width of the popup view
     * @param displayWidth dimension to be set
     * @param isToolBarAnchored True when popup will be anchored on toolbar view
     *                          otherwise false.
     * @return UmEditorPopUpView instance
     */
    public UmEditorPopUpView setWidthDimen(int displayWidth,boolean isToolBarAnchored){
        this.isToolBarAnchored = isToolBarAnchored;
        double width = displayWidth <= 320 ? (isToolBarAnchored ? 0.77 : 0.57):
                (isToolBarAnchored ? 1.25 : 0.83);
        popupWindow.setWidth((int) (width * displayWidth));
        return this;
    }


    /**
     * Enable view to showWithListener/hide icons on the list
     * @param visible Show when true otherwise hide.
     * @return UmEditorPopUpView instance
     */
    public UmEditorPopUpView showIcons(boolean visible){
       this.visible = visible;
        return this;
    }

    /**
     * Show popup window to the UI
     * @param listener Listen to listen for the popup list item clicks.
     */
    public void showWithListener(OnPopUpMenuClickListener listener){
        this.listener = listener;
        if(!isToolBarAnchored){
            popupWindow.showAtLocation(anchorView,Gravity.BOTTOM,-250,100);
        }else{
            popupWindow.showAsDropDown(anchorView);
        }
    }



    private class PopUpAdapter extends RecyclerView.Adapter<PopUpItemViewHolder>{

        private List<UmFormat> menuList;

        PopUpAdapter (List<UmFormat> menuList){
            this.menuList = menuList;
        }

        void setMenuList(List<UmFormat> menuList){
            this.menuList = menuList;
            notifyDataSetChanged();
        }


        private void changeState(ImageView formatIcon,
                                 RelativeLayout formatHolder, TextView formatTile, boolean isActivated){
            formatIcon.setColorFilter(ContextCompat.getColor(context,
                    isActivated ? R.color.icons:R.color.text_primary));
            formatTile.setTextColor(ContextCompat.getColor(context,
                    isActivated ? R.color.icons:R.color.text_primary));
            formatHolder.setBackgroundColor(ContextCompat.getColor(context,
                    isActivated ? R.color.content_icon_active:R.color.icons));
        }

        @NonNull
        @Override
        public PopUpItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PopUpItemViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_popup_formatting_view, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PopUpItemViewHolder holder, int position) {
            UmFormat format = menuList.get(position);
            holder.menuIcon.setVisibility(visible ? View.VISIBLE:View.GONE);
            holder.menuIcon.setImageResource(format.getFormatIcon());
            holder.meuTitle.setText(format.getFormatTitle());
            changeState(holder.menuIcon,holder.menuHolder,holder.meuTitle,format.isActive());

            holder.itemView.setOnClickListener(v -> {
                listener.onMenuClicked(format);
                popupWindow.dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return menuList.size();
        }
    }

    private class PopUpItemViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout menuHolder;

        ImageView menuIcon;

        TextView meuTitle;

        PopUpItemViewHolder(View itemView) {
            super(itemView);
            menuHolder = itemView.findViewById(R.id.icon_holder);
            menuIcon = itemView.findViewById(R.id.format_icon);
            meuTitle = itemView.findViewById(R.id.format_title);
        }
    }
}
