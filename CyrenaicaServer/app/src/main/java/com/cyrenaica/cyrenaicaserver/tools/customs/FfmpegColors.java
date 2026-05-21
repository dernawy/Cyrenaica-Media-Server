package com.cyrenaica.cyrenaicaserver.tools.customs;

import java.io.Serializable;

public class FfmpegColors implements Serializable {
    private String name;
    private int color;
    private String code;

    public FfmpegColors() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
