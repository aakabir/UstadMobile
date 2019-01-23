package com.ustadmobile.port.android.view;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ContentEditorPageListPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opf.UstadJSOPFItem;
import com.ustadmobile.core.view.ContentEditorPageListView;
import com.ustadmobile.port.android.umeditor.UmOnStartDragListener;
import com.ustadmobile.port.android.umeditor.UmPageActionListener;
import com.ustadmobile.port.android.umeditor.UmPageItemTouchAdapter;
import com.ustadmobile.port.android.umeditor.UmPageItemTouchCallback;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.List;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContentEditorPageListFragment extends DialogFragment
        implements UmOnStartDragListener, ContentEditorPageListView {

    private List<UstadJSOPFItem> pageList;

    private ItemTouchHelper mItemTouchHelper;

    private UmPageActionListener pageActionListener;

    private ContentEditorPageListPresenter presenter;

    private boolean isScrollDirectionUp = false;

    public ContentEditorPageListFragment() {
        // Required empty public constructor
    }


    private class PageListAdapter extends RecyclerView.Adapter<PageListAdapter.PageViewHolder>
            implements UmPageItemTouchAdapter {
        private UmOnStartDragListener mDragStartListener;

        PageListAdapter(UmOnStartDragListener mDragStartListener){
            this.mDragStartListener = mDragStartListener;
        }

        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.umcontenteditor_filelist_item, parent, false);
            return new PageViewHolder(view);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
            final UstadJSOPFItem pageItem = pageList.get(holder.getAdapterPosition());
            holder.pageTitle.setText(pageItem.title);
            holder.pageReorderHandle.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onDragStarted(holder);
                }
                return false;
            });
            holder.pageOptionHandle.setOnClickListener(v ->
                    showPopUpMenu(holder.itemView.getContext(),holder.pageOptionHandle,pageItem));
            holder.itemView.setOnClickListener(v ->
                    presenter.handlePageSelected(pageItem));
        }

        private void showPopUpMenu(Context context, View anchorView,UstadJSOPFItem pageItem){
            PopupMenu popup = new PopupMenu(context, anchorView);
            popup.getMenuInflater().inflate(R.menu.menu_content_editor_page_option, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
              if(item.getItemId() == R.id.action_page_update){
                    presenter.handleUpdatePage(pageItem);
                }else if(item.getItemId() ==R.id.action_page_delete){
                    presenter.handleRemovePage(pageItem);
                }
                return true;
            });
            popup.show();
        }

        @Override
        public int getItemCount() {
            return pageList.size();
        }

        @Override
        public void onPageItemMove(int fromPosition, int toPosition) {
            UstadJSOPFItem prev = pageList.get(fromPosition);
            pageList.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
            pageList.remove(fromPosition);
            presenter.handleReOrderPages(pageList);
            notifyItemMoved(fromPosition, toPosition);
        }

        class PageViewHolder extends RecyclerView.ViewHolder{
            ImageView pageReorderHandle;
            TextView pageTitle;
            ImageView pageOptionHandle;
            PageViewHolder(View itemView) {
                super(itemView);
                pageReorderHandle = itemView.findViewById(R.id.page_handle);
                pageOptionHandle = itemView.findViewById(R.id.page_option);
                pageTitle = itemView.findViewById(R.id.page_title);
            }
        }
    }


    public void setPageActionListener(Activity activity){
        pageActionListener = (UmPageActionListener) activity;
    }

    public void setPageList(List<UstadJSOPFItem> pageList){
        this.pageList = pageList;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_content_editor_page_list,
                container, false);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(view1 -> dismiss());
        toolbar.setTitle("Document title");

        RecyclerView pageListView = rootView.findViewById(R.id.page_list);
        FloatingTextButton btnAddPage = rootView.findViewById(R.id.btn_add_page);
        PageListAdapter mPageListAdapter = new PageListAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        pageListView.setLayoutManager(layoutManager);
        pageListView.setAdapter(mPageListAdapter);

        ItemTouchHelper.Callback callback = new UmPageItemTouchCallback(mPageListAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(pageListView);

        presenter = new ContentEditorPageListPresenter(this, null,this);
        presenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        pageListView.clearOnScrollListeners();
        pageListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        btnAddPage.setVisibility(View.VISIBLE);
                        break;
                    default:
                        btnAddPage.setVisibility(View.GONE);
                        break;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        pageListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                isScrollDirectionUp = dy > 0;
                super.onScrolled(recyclerView, dx, dy);
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                btnAddPage.setVisibility(newState != RecyclerView.SCROLL_STATE_IDLE
                        && !isScrollDirectionUp ? View.VISIBLE:View.GONE);
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        btnAddPage.setOnClickListener(v -> showPageAddDialog(null));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private void showPageAddDialog(UstadJSOPFItem pageItem){
        final boolean isNewPage = pageItem.title == null;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String dialogTitle = isNewPage ? impl.getString(MessageID.content_add_page,
                getActivity()):impl.getString(MessageID.content_update_page_title,
                getActivity());
        pageItem.title  = isNewPage ? impl.getString(MessageID.content_untitled_page,
                getActivity()):pageItem.title;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setTitle(dialogTitle);
        builder.setPositiveButton("", (dialog, which) -> {
            pageItem.title = "";
            if(isNewPage){
                pageActionListener.onPageCreate(pageItem);
            }else{
                pageActionListener.onPageUpdate(pageItem);
            }
            dialog.dismiss();
        });
        builder.setNegativeButton("", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    @Override
    public void onDragStarted(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void updatePageList(List<UstadJSOPFItem> newPageList) {
        pageActionListener.onOrderChanged(newPageList);
    }

    @Override
    public void addNewPage(UstadJSOPFItem page) {
        showPageAddDialog(page);
    }

    @Override
    public void removePage(UstadJSOPFItem page) {
        pageActionListener.onPageRemove(page);
    }

    @Override
    public void loadPage(UstadJSOPFItem page) {
        pageActionListener.onPageSelected(page.href);
    }

    @Override
    public void updatePage(UstadJSOPFItem page) {
        showPageAddDialog(page);
    }

    @Override
    public int getDirection() {
        return 0;
    }

    @Override
    public void setDirection(int dir) {

    }

    @Override
    public void setAppMenuCommands(String[] labels, int[] ids) {

    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void runOnUiThread(Runnable r) {

    }
}
