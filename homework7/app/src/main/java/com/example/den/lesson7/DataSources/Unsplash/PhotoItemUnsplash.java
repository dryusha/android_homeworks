package com.example.den.lesson7.DataSources.Unsplash;

import com.example.den.lesson7.Interfaces.PhotoItem;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.dsl.Table;

import java.io.Serializable;
import java.util.List;

@Table
public class PhotoItemUnsplash implements PhotoItem {

    // that the main pain
    // because DB need to have its own ID, it conflict with ID that we receive from server
    // In order to overcome that, we need to manually resolve them
    @SerializedName("db_id") // ID that use local DB
    private transient Long id;

    @SerializedName("id") // ID that came from server
    private String imgID;

    URLs urls;
    User user;

    private boolean isDeleted = false;

    private String URLsFromORM;
    private String UserFromORM;

    public PhotoItemUnsplash() {}


    public String getID () {return this.imgID;}

    @Override
    public boolean getIsDeleted() {return this.isDeleted;}

    public String getImgUrl() {
        if (urls == null) { // Because this DB does not know how to fully recreate object, we need to re-create its nested properties back manually
            this.urls = new Gson().fromJson(this.URLsFromORM, URLs.class);
        }

        return this.urls.regular;
    }

    public String getAuthorName() {
        if (user == null) { // Because this DB does not know how to fully recreate object, we need to re-create its nested properties back manually
            this.user = new Gson().fromJson(this.UserFromORM, User.class);
        }

        return this.user.name;
    }

    @Override
    public void saveToDatabase() {
        if(isSavedRecordExist()) { // if exist -> update
            update(false);
        } else {
            // before save - convert inner objects to string
            this.URLsFromORM = urls.toString();
            this.UserFromORM = user.toString();

            SugarRecord.save(this);
        }
    }

    @Override
    public void deleteFromDatabase() {
        // A small query is needed because of conflicted id properties
        SugarRecord.deleteAll(PhotoItemUnsplash.class,"img_ID = ?", this.imgID);
    }

    @Override
    public void deleteFromDatabaseSafe() {
        update(true);
    }

    @Override
    public boolean isSavedToDatabase() {
        PhotoItemUnsplash item = getSavedRecord();
        if (item == null) {
            return false; // if no record exist -> not saved
        }
        return !item.isDeleted; // if record exist -> return its value
    }

    private void update(boolean isDeleted) {
        this.isDeleted = isDeleted;
        SugarRecord.executeQuery("UPDATE PHOTO_ITEM_UNSPLASH SET IS_DELETED = ? WHERE img_ID = ?", this.isDeleted ? "1" : "0", this.imgID);
    }

    private boolean isSavedRecordExist() {
        return getSavedRecord() != null;
    }

    private PhotoItemUnsplash getSavedRecord() {
        List<PhotoItemUnsplash> items = SugarRecord.find(PhotoItemUnsplash.class,"img_ID = ?", this.imgID);
        if(items.isEmpty()) { return null; } // If empty -> nothing to return
        return items.get(0);
    }

    public class User implements Serializable {
        String name;

        public String toString() {
            return new Gson().toJson(this);
        }
    }

    public class URLs implements Serializable {
        String regular;

        public String toString() {
            return new Gson().toJson(this);
        }

    }
}
