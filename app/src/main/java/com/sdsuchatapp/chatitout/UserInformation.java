package com.sdsuchatapp.chatitout;

/**
 * Created by priyankadeshmukh on 4/19/18.
 */

public class UserInformation {

    String displayName;
    String profilePicture;
    String profileThumbnail;
    String phoneNumber;


    public UserInformation() {

    }

    public UserInformation(String displayName, String profilePicture, String profileThumbnail, String phoneNumber) {
        this.displayName = displayName;
        this.profilePicture = profilePicture;
        this.profileThumbnail = profileThumbnail;
        this.phoneNumber = phoneNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfile_thumbnail() {
        return profileThumbnail;
    }

    public void setProfile_thumbnail(String profileThumbnail) {
        this.profileThumbnail = profileThumbnail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
