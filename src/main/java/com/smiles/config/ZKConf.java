package com.smiles.config;

public class ZKConf {

    /**
     * zk地址
     */
    private String address;

    /**
     * session超时时间
     */
    private Integer sessionTime;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getSessionTime() {
        return sessionTime;
    }

    public void setSessionTime(Integer sessionTime) {
        this.sessionTime = sessionTime;
    }

}
