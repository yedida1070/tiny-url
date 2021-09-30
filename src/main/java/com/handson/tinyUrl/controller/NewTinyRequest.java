package com.handson.tinyUrl.controller;

import java.util.Objects;

public class NewTinyRequest {
    private  String longUrl;

    public String getUserId() {
        return userId;
    }

    private  String userId;

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    @Override
    public String toString() {
        return "NewTinyRequest{" +
                "longUrl='" + longUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewTinyRequest that = (NewTinyRequest) o;
        return Objects.equals(longUrl, that.longUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(longUrl);
    }
}
