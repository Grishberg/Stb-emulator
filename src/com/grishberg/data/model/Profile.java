package com.grishberg.data.model;

/**
 * Created by g on 13.08.15.
 */
public class Profile {
    private int id;
    private String name;

    public Profile(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
