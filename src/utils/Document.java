package utils;

import java.io.Serializable;

public class Document implements Serializable {

    private int id;
    private static int nextId=0;
    private User creator;
    private String title;
    private String content;

    public Document(User creator, String title){
        this.id=nextId;
        nextId++;
        this.creator=creator;
        this.title=title;
    }

    public Document(int id, User creator, String title, String content){
        this.id = id;
        if(nextId <= id){
            nextId  = id + 1;
        }
        this.creator = creator;
        this.title = title;
        this.content = content;

    }

    public int getId() {
        return id;
    }

    public User getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content){
        this.content=content;
    }

}
