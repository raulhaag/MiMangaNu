package ar.rulosoft.mimanganu.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class RevealAnimationSetting implements Parcelable {
    public static final Creator<RevealAnimationSetting> CREATOR
            = new Creator<RevealAnimationSetting>() {
        public RevealAnimationSetting createFromParcel(Parcel in) {
            return new RevealAnimationSetting(in);
        }

        public RevealAnimationSetting[] newArray(int size) {
            return new RevealAnimationSetting[size];
        }
    };
    private int centerX;
    private int centerY;
    private int width ;
    private int height;
    private int initialRadius;

    public RevealAnimationSetting(int centerX, int centerY, int width, int height, int initialRadius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.initialRadius = initialRadius;
    }

    private RevealAnimationSetting(Parcel in) {
        centerX = in.readInt();
        centerY = in.readInt();
        width = in.readInt();
        height = in.readInt();
        initialRadius = in.readInt();
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getInitialRadius() {
        return initialRadius;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(centerX);
        dest.writeInt(centerY);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(initialRadius);
    }
}
