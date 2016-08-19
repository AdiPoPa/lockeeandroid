package com.adipopa.lockee;

public class Data {

    public String nickname;
    public String lockID;
    public String shareID;
    public String is_open;

    public Data(String nickname, String lockID, String shareID, String is_open) {
        this.nickname = nickname;
        this.lockID = lockID;
        this.shareID = shareID;
        this.is_open = is_open;
    }
}
