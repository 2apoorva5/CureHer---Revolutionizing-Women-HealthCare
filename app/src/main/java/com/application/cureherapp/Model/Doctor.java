package com.application.cureherapp.Model;

import com.google.firebase.Timestamp;

public class Doctor {
    private String doctorId, doctorName, doctorImage, doctorSpeciality, doctorCategory, doctorSearchKeyword;
    private Timestamp doctorTimestamp;
    private double doctorRatings;

    public Doctor() {
    }

    public Doctor(String doctorId, String doctorName, String doctorImage, String doctorSpeciality,
                  String doctorCategory, String doctorSearchKeyword, Timestamp doctorTimestamp, double doctorRatings) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorImage = doctorImage;
        this.doctorSpeciality = doctorSpeciality;
        this.doctorCategory = doctorCategory;
        this.doctorSearchKeyword = doctorSearchKeyword;
        this.doctorTimestamp = doctorTimestamp;
        this.doctorRatings = doctorRatings;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorImage() {
        return doctorImage;
    }

    public void setDoctorImage(String doctorImage) {
        this.doctorImage = doctorImage;
    }

    public String getDoctorSpeciality() {
        return doctorSpeciality;
    }

    public void setDoctorSpeciality(String doctorSpeciality) {
        this.doctorSpeciality = doctorSpeciality;
    }

    public String getDoctorCategory() {
        return doctorCategory;
    }

    public void setDoctorCategory(String doctorCategory) {
        this.doctorCategory = doctorCategory;
    }

    public String getDoctorSearchKeyword() {
        return doctorSearchKeyword;
    }

    public void setDoctorSearchKeyword(String doctorSearchKeyword) {
        this.doctorSearchKeyword = doctorSearchKeyword;
    }

    public Timestamp getDoctorTimestamp() {
        return doctorTimestamp;
    }

    public void setDoctorTimestamp(Timestamp doctorTimestamp) {
        this.doctorTimestamp = doctorTimestamp;
    }

    public double getDoctorRatings() {
        return doctorRatings;
    }

    public void setDoctorRatings(double doctorRatings) {
        this.doctorRatings = doctorRatings;
    }
}
