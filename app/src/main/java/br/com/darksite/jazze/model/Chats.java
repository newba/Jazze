package br.com.darksite.jazze.model;



public class Chats {

    private String userStatus;

    public Chats() {
    }

    public Chats(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}
