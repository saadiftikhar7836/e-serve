package com.eserve.android.model;

public class ServiceLocation {
    private String place_id, address;
    private Double lat, lng;

    public ServiceLocation() {
    }

    public String getPlace_id() {
        return place_id;
    }

    public String getAddress() {
        return address;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}
