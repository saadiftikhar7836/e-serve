package com.codesses.e_serve.model;


import java.io.Serializable;

public class MessageRequest implements Serializable {
    private String r_id, address, sent_by, sent_to;
    private int status;
    private double lat, lng;

    public MessageRequest() {
    }

    public String getR_id() {
        return r_id;
    }

    public String getAddress() {
        return address;
    }

    public String getSent_by() {
        return sent_by;
    }

    public String getSent_to() {
        return sent_to;
    }

    public int getStatus() {
        return status;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
