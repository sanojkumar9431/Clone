package com.example.clone.models;


import java.util.List;

public class Comment {

    private String comment;
    private String user_id;
    private List<Like> likes;
    private String data_created;

    public Comment(){

    }

    public Comment(String comment, String user_id, List<Like> likes, String data_created) {
        this.comment = comment;
        this.user_id = user_id;
        this.likes = likes;
        this.data_created = data_created;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public String getData_created() {
        return data_created;
    }

    public void setData_created(String data_created) {
        this.data_created = data_created;
    }


    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", likes=" + likes +
                ", data_created='" + data_created + '\'' +
                '}';
    }
}
