package com.oguzhanozer.mychatapplication;

public class User {

    private String id,username,imageurl,status,search;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public User(String id, String username, String imageurl,String status,String search) {

        this.id = id;
        this.username = username;
        this.imageurl = imageurl;
        this.status = status;
        this.search=search;
    }

    public User(){

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
