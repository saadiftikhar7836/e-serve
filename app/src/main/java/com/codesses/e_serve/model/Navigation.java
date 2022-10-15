package com.codesses.e_serve.model;

public class Navigation {
    Double user_lat, user_lng, service_lat, service_lng;
    String request_id, sent_by, sent_to, service_role, servicer_name, customer_name, customer_fcm_token;

    public Navigation() {
    }

    public String getRequest_id() {
        return request_id;
    }

    public String getServicer_name() {
        return servicer_name;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public Double getUser_lat() {
        return user_lat;
    }

    public void setUser_lat(Double user_lat) {
        this.user_lat = user_lat;
    }

    public Double getUser_lng() {
        return user_lng;
    }

    public void setUser_lng(Double user_long) {
        this.user_lng = user_long;
    }

    public Double getService_lat() {
        return service_lat;
    }

    public void setService_lat(Double service_lat) {
        this.service_lat = service_lat;
    }

    public Double getService_lng() {
        return service_lng;
    }

    public void setService_long(Double service_long) {
        this.service_lng = service_long;
    }

    public String getSent_by() {
        return sent_by;
    }

    public void setSent_by(String sent_by) {
        this.sent_by = sent_by;
    }

    public String getSent_to() {
        return sent_to;
    }

    public void setSent_to(String sent_to) {
        this.sent_to = sent_to;
    }

    public String getService_role() {
        return service_role;
    }

    public String getCustomer_fcm_token() {
        return customer_fcm_token;
    }
}