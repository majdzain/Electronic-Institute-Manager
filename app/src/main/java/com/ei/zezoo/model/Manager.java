package com.ei.zezoo.model;

public class Manager {
    private String user,pass,name,userId;
    private boolean isSigned;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Manager() {

    }

    public Manager(String user, String pass, String name, String userId, boolean isSigned) {
        this.user = user;
        this.pass = pass;
        this.name = name;
        this.userId = userId;
        this.isSigned = isSigned;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public void setSigned(boolean signed) {
        isSigned = signed;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
