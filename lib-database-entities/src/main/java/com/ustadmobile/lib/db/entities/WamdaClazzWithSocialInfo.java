package com.ustadmobile.lib.db.entities;

import com.ustadmobile.lib.database.annotation.UmEmbedded;

public class WamdaClazzWithSocialInfo extends Clazz {

    private boolean liked;

    private int numLikes;

    private int numStudents;

    private int numShares;

    @UmEmbedded
    private WamdaClazz wamdaClazz;

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public int getNumStudents() {
        return numStudents;
    }

    public void setNumStudents(int numStudents) {
        this.numStudents = numStudents;
    }

    public int getNumShares() {
        return numShares;
    }

    public void setNumShares(int numShares) {
        this.numShares = numShares;
    }

    public WamdaClazz getWamdaClazz() {
        return wamdaClazz;
    }

    public void setWamdaClazz(WamdaClazz wamdaClazz) {
        this.wamdaClazz = wamdaClazz;
    }
}