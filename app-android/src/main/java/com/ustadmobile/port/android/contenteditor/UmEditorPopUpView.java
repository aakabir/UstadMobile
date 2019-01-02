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

public class UmEditorPopUpView {

    private Context context;

    private View anchorView;

    private PopupWindow popupWindow;

    private PopUpAdapter popUpAdapter;

    private boolean visible = true;

    private OnPopUpMenuClickListener listener;


    public interface OnPopUpMenuClickListener {

        void onMenuClicked(ContentFormat format);
    }

    public static class UmPopUpDim{
        public static int DIMEN_FULL_WIDTH = ListPopupWindow.WRAP_CONTENT;
        public static int DIMEN_MIN_WIDTH = 300;
        public static int DIMEN_MID_WIDTH = 450;
    }

    public UmEditorPopUpView(Context context,View anchor) {
        this.context = context;
        this.anchorView = anchor;
        initializePopUpWindow();
        setWidthDimen(UmPopUpDim.DIMEN_MID_WIDTH);
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

    public UmEditorPopUpView setMenuList(List<ContentFormat> menuList){
        popUpAdapter.setMenuList(menuList);
        return this;
    }

    public UmEditorPopUpView setWidthDimen(int popUpWidth){
        popupWindow.setWidth(popUpWidth);
        return this;
    }


    public UmEditorPopUpView showIcons(boolean visible){
       this.visible = visible;
        return this;
    }

    public void show(boolean isToolBarAnchored,OnPopUpMenuClickListener listener){
        this.listener = listener;
        if(!isToolBarAnchored){
            popupWindow.showAtLocation(anchorView,Gravity.BOTTOM,-250,100);
        }else{
            popupWindow.showAsDropDown(anchorView);
        }
    }



    private class PopUpAdapter extends RecyclerView.Adapter<PopUpItemViewHolder>{

        private List<ContentFormat> menuList;

        PopUpAdapter (List<ContentFormat> menuList){
            this.menuList = menuList;
        }

        void setMenuList(List<ContentFormat> menuList){
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
            ContentFormat format = menuList.get(position);
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
