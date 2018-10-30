package com.ustadmobile.port.android.view;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CommonHandlerPresenter;
import com.ustadmobile.lib.db.entities.Person;

import java.io.File;
import java.util.Hashtable;

public class PeopleBlobListRecyclerAdapter extends
        PagedListAdapter<Person, PeopleBlobListRecyclerAdapter.PeopleViewHolder> {

    Context theContext;
    CommonHandlerPresenter mPresenter;
    boolean hideNames = false;

    Hashtable colorMap = new Hashtable();


    protected class PeopleViewHolder extends RecyclerView.ViewHolder {
        protected PeopleViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected PeopleBlobListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<Person> diffCallback,
                                              Context context, CommonHandlerPresenter presenter) {
        super(diffCallback);
        theContext = context;
        mPresenter = presenter;
    }

    protected PeopleBlobListRecyclerAdapter(@NonNull DiffUtil.ItemCallback<Person> diffCallback,
                                            Context context, CommonHandlerPresenter presenter,
                                            boolean namesHidden) {
        super(diffCallback);
        theContext = context;
        mPresenter = presenter;
        hideNames = namesHidden;
    }

    /**
     * This method inflates the card layout (to parent view given) and returns it.
     * @param parent View given.
     * @param viewType View Type not used here.
     * @return New ViewHolder for the ClazzStudent type
     */
    @NonNull
    @Override
    public PeopleBlobListRecyclerAdapter.PeopleViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View clazzStudentListItem =
                LayoutInflater.from(theContext).inflate(
                        R.layout.item_peopleblob, parent, false);
        return new PeopleBlobListRecyclerAdapter.PeopleViewHolder(clazzStudentListItem);
    }

    public class CropSquareTransformation implements Transformation {
        @Override public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override public String key() { return "square()"; }
    }

    public void updateImageOnView(String imagePath, ImageView personImageView){
        Uri profileImage = Uri.fromFile(new File(imagePath));

//        personImageView.setImageURI(profileImage);

        Picasso.with(theContext)
                .load(profileImage)
                .transform(new CropSquareTransformation())
                .resize(90,90)
                .centerCrop()
                .into(personImageView);

        File profilePic = new File(imagePath);
        Picasso.with(theContext)
                .load(profilePic)
                .transform(new CropSquareTransformation())
                .resize(90,90)
                .centerCrop()
                .into(personImageView);
    }

    /**
     * This method sets the elements after it has been obtained for that item'th position.
     * @param holder    The holder
     * @param position  The position in the recycler view.
     */
    @Override
    public void onBindViewHolder(
            @NonNull PeopleBlobListRecyclerAdapter.PeopleViewHolder holder, int position) {


        Person thisPerson = getItem(position);
        String studentName;
        if (thisPerson == null) {
            studentName = "Student";
        } else {
            studentName = thisPerson.getFirstNames() + " " +
                    thisPerson.getLastName();
        }

        ImageView studentImage = holder.itemView
                .findViewById(R.id.item_peopleblob_image);

        //TODO: Add image of student here.
        if(thisPerson.getImagePath() != null && !thisPerson.getImagePath().isEmpty()){
            updateImageOnView(thisPerson.getImagePath(), studentImage);
        }

        TextView studentEntry = (TextView) holder.itemView
                .findViewById(R.id.item_peopleblob_name);

        if (!hideNames) {
            studentEntry.setText(studentName);
        }

        CardView personCard = holder.itemView.findViewById(R.id.item_peoplblob_card);

        personCard.setOnClickListener(v -> {


            if (colorMap.containsKey(position)) {
                if (colorMap.get(position) == "selected") {
                    if (hideNames){
                        studentEntry.setText("");
                    }else {
                        personCard.setBackgroundColor(Color.parseColor("#FFFFFF"));

                    }
                    colorMap.put(position, "unselected");
                } else {
                    if (hideNames){
                        studentEntry.setText(studentName);
                    }else {
                        personCard.setBackgroundColor(Color.parseColor("#FF6666"));

                    }
                    colorMap.put(position, "selected");
                }
            } else {
                colorMap.put(position, "selected");
                if (hideNames){
                    studentEntry.setText(studentName);
                }else {
                    personCard.setBackgroundColor(Color.parseColor("#FF6666"));
                }
            }


            mPresenter.handleCommonPressed(thisPerson.getPersonUid());
        });


    }
}