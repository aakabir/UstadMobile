package com.ustadmobile.port.android.view;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.contentformats.epub.nav.EpubNavItem;
import com.ustadmobile.core.controller.ContentEditorPageListPresenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
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
public class ContentEditorPageListFragment extends UstadDialogFragment
        implements UmOnStartDragListener, ContentEditorPageListView {

    private List<EpubNavItem> pageList;

    private ItemTouchHelper mItemTouchHelper;

    private UmPageActionListener pageActionListener;

    private  PageListAdapter mPageListAdapter;

    private ContentEditorPageListPresenter presenter;

    private boolean isScrollDirectionUp = false;

    private String documentTitle = null;

    private String currentHref;

    private TextView titleView;

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
            final EpubNavItem pageItem = pageList.get(holder.getAdapterPosition());
            holder.pageTitle.setText(pageItem.getTitle());
            holder.pageReorderHandle.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onDragStarted(holder);
                }
                return false;
            });

            holder.pageTitle.setTextColor(getColor(holder.itemView.getContext(),
                    pageItem.getHref().equals(currentHref) ? R.color.text_primary:
                            R.color.text_secondary));
            holder.pageOptionHandle.setColorFilter(getColor(holder.itemView.getContext(),
                    pageItem.getHref().equals(currentHref) ? R.color.text_primary:
                            R.color.text_secondary));
            holder.pageReorderHandle.setColorFilter(getColor(holder.itemView.getContext(),
                    pageItem.getHref().equals(currentHref) ? R.color.text_primary:
                            R.color.text_secondary));
            holder.itemHolder.setBackgroundColor(getColor(holder.itemView.getContext(),
                    pageItem.getHref().equals(currentHref) ? R.color.secondary_text_light:
                            R.color.icons));

            holder.pageOptionHandle.setOnClickListener(v ->
                    showPopUpMenu(holder.itemView.getContext(),holder.pageOptionHandle,pageItem));
            holder.itemView.setOnClickListener(v ->
                    presenter.handlePageSelected(pageItem));
        }

        private int getColor(Context content, int resource){
            return ContextCompat.getColor(content,resource);
        }

        private void showPopUpMenu(Context context, View anchorView,EpubNavItem pageItem){
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
            EpubNavItem oldItem = pageList.get(fromPosition);
            pageList.remove(fromPosition);
            pageList.add(toPosition,oldItem);
            notifyItemMoved(fromPosition, toPosition);
            presenter.handleReOrderPages(pageList);
        }

        class PageViewHolder extends RecyclerView.ViewHolder{
            ImageView pageReorderHandle;
            TextView pageTitle;
            ImageView pageOptionHandle;
            FrameLayout itemHolder;
            PageViewHolder(View itemView) {
                super(itemView);
                pageReorderHandle = itemView.findViewById(R.id.page_handle);
                pageOptionHandle = itemView.findViewById(R.id.page_option);
                pageTitle = itemView.findViewById(R.id.page_title);
                itemHolder = itemView.findViewById(R.id.page_item);
            }
        }
    }


    public void setPageActionListener(Activity activity){
        pageActionListener = (UmPageActionListener) activity;
    }

    public void setDocumentTitle(String title){
        this.documentTitle = title;
        if(titleView != null){
           presenter.handleDocumentTitle(title);
        }
    }

    public void setPageList(List<EpubNavItem> pageList, String currentHref){
        this.pageList = pageList;
        this.currentHref = currentHref;
        if(mPageListAdapter != null){
            mPageListAdapter.notifyItemRangeChanged(0,pageList.size());
        }
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
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        titleView = rootView.findViewById(R.id.document_title);
        titleView.setText(documentTitle);

        RecyclerView pageListView = rootView.findViewById(R.id.page_list);
        FloatingTextButton btnAddPage = rootView.findViewById(R.id.btn_add_page);
        mPageListAdapter = new PageListAdapter(this);
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
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
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
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                isScrollDirectionUp = dy > 0;
                super.onScrolled(recyclerView, dx, dy);
            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                btnAddPage.setVisibility(newState != RecyclerView.SCROLL_STATE_IDLE
                        && !isScrollDirectionUp ? View.VISIBLE:View.GONE);
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        btnAddPage.setOnClickListener(v -> presenter.handleAddPage());

        titleView.setOnClickListener(v -> presenter.handleDocumentTitleUpdate());

        toolbar.setNavigationOnClickListener(view1 -> presenter.handleClosingPageManager());

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

    private void showDocAndPageUpdateDialog(EpubNavItem pageItem, boolean isDocument){

        final boolean isNewPage = pageItem == null;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        String dialogTitle = isDocument ? impl.getString(MessageID.content_update_document_title,
                getActivity()) : (isNewPage ? impl.getString(MessageID.content_add_page,
                getActivity()) : impl.getString(MessageID.content_update_page_title, getActivity()));

        String positiveBtnLabel = isNewPage && !isDocument ? impl.getString(
                MessageID.content_page_dialog_add, getActivity()) :
                impl.getString(MessageID.content_page_dialog_update, getActivity());

        String titleToUpdateFrom = isDocument ? documentTitle:
                (isNewPage ? impl.getString(MessageID.content_untitled_page,
                getActivity()):pageItem.getTitle());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.umcontent_dialog_option_actionview,
                null,false);

        TextInputLayout titleWrapper = dialogView.findViewById(R.id.titleWrapper);
        titleWrapper.setHint(impl.getString(MessageID.content_editor_page_view_hint,
                getActivity()));

        EditText titleView = dialogView.findViewById(R.id.title);
        titleView.setText(titleToUpdateFrom);
        builder.setView(dialogView);
        builder.setTitle(dialogTitle);

        builder.setPositiveButton(positiveBtnLabel, (dialog, which) -> {

            if(!isDocument){
                if(isNewPage){
                    pageActionListener.onPageCreate(titleView.getText().toString());
                }else{
                    pageItem.setTitle(titleView.getText().toString());
                    pageActionListener.onPageUpdate(pageItem);
                }
            }else{
                pageActionListener.onDocumentTitleUpdate(titleView.getText().toString());
            }

            dialog.dismiss();
        });
        builder.setNegativeButton(impl.getString(MessageID.content_page_dialog_cancel,
                getActivity()), (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    @Override
    public void onDragStarted(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void updatePageList(List<EpubNavItem> newPageList) {
        pageActionListener.onOrderChanged(newPageList);
    }

    @Override
    public void addNewPage() {
        showDocAndPageUpdateDialog(null,false);
    }

    @Override
    public void removePage(EpubNavItem page) {
        if(pageList.size() == 1){
            UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
            String message = impl.getString(MessageID.content_editor_page_delete_failure_message,
                    getActivity());
            pageActionListener.onDeleteFailure(message);
        }else{
            int index = pageList.indexOf(page);
            if(pageList.remove(page)){
                mPageListAdapter.notifyItemRemoved(index);
                pageActionListener.onPageRemove(page.getHref());
            }
        }
    }

    @Override
    public void loadPage(EpubNavItem page) {
        pageActionListener.onPageSelected(page.getHref());
        presenter.handleClosingPageManager();
    }

    @Override
    public void updatePage(EpubNavItem page) {
        showDocAndPageUpdateDialog(page,false);
    }

    @Override
    public void updateDocumentTitle() {
        showDocAndPageUpdateDialog(null,true);
    }

    @Override
    public void closePageManager() {
        pageActionListener.onPageManagerClosed();
        dismiss();
    }

    @Override
    public void setTitle(String title) {
        titleView.setText(title);
    }

}
