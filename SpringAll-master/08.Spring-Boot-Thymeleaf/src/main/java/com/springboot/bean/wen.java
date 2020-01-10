package com.springboot.bean;

/**
 * Created by craywen on 2019/9/30 15:24
 *
 * @description
 */
public class wen {

    public  String url ;

    public  String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        wen wen = (wen) o;

        if (!url.equals(wen.url)) return false;
        return url.equals(wen.url);
    }

    @Override
    public int hashCode() {
        System.out.println(this.url.hashCode());
        return this.url.hashCode();
    }

    public wen(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
