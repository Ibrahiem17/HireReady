package com.example.hireready;

public class JobListing {
    private String title;
    private String company;
    private String location;

    public JobListing(String title, String company, String location) {
        this.title = title;
        this.company = company;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }
}
