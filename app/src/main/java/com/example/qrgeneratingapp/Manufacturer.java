package com.example.qrgeneratingapp;

public class Manufacturer {


    private String FullName,Email,ima,CNIC,CompanyId;
    public Manufacturer(){

    }

    public Manufacturer(String fullName, String email, String ima, String cnic, String CompId) {
        FullName = fullName;
        Email = email;
        this.ima = ima;
        CNIC = cnic;
        CompanyId = CompId;
    }

    public String getCNIC() {
        return CNIC;
    }

    public void setCNIC(String CNIC) {
        this.CNIC = CNIC;
    }

    public String getCompanyId() {
        return CompanyId;
    }

    public void setCompanyId(String companyId) {
        CompanyId = companyId;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getIma() {
        return ima;
    }

    public void setIma(String ima) {
        this.ima = ima;
    }
}
