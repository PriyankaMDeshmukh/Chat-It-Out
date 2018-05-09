package com.sdsuchatapp.chatitout;

import java.sql.Timestamp;

/**
 * Created by priyankadeshmukh on 5/8/18.
 */

public class Chat {
    String userId;
    String message;
    Timestamp timeStamp;
    String profilePicture;
    String displayName;

    public Chat(String userId, String message, Timestamp timeStamp, String profilePicture, String displayName) {
        this.userId = userId;
        this.message = message;
        this.timeStamp = timeStamp;
        this.profilePicture = profilePicture;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


}
