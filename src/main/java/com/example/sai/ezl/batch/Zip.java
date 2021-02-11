package com.example.sai.ezl.batch;

public class Zip {

    private String zip;
    private String state;
    private String primary_city;

    public Zip() {
    }

    public Zip(String zip, String state, String primary_city) {
        this.zip = zip;
        this.state=state;
        this.primary_city=primary_city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public String toString() {
        return "Zip : " + zip;
    }

}
