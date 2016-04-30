package com.baiylin.songjia.ln5in12tool.bean;

import java.util.List;

/**
 * Created by songjia on 16-4-29.
 */
public class ResponseBean {
    //返回信息
    private String message;

    //返回状态码
    private int status;

    private List<Ln5In12Bean> ln5In12List;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Ln5In12Bean> getLn5In12List() {
        return ln5In12List;
    }

    public void setLn5In12List(List<Ln5In12Bean> ln5In12List) {
        this.ln5In12List = ln5In12List;
    }
}
