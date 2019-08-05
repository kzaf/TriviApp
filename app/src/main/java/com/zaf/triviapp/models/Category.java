package com.zaf.triviapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable {
    private String name;
    private int id;

    public Category(String name, int id) {
        this.name = name;
        this.id = id;
    }

    protected Category(Parcel in) {
        name = in.readString();
        id = in.readInt();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(id);
    }
}
