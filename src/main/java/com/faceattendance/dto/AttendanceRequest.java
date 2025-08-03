package com.faceattendance.dto;

import jakarta.validation.constraints.NotBlank;

public class AttendanceRequest {

    @NotBlank(message = "Face image is required")
    private String faceImage; // Base64 encoded image

    private String imagePath;
    private String notes;
    private boolean checkOut = false;

    // Constructors
    public AttendanceRequest() {}

    public AttendanceRequest(String faceImage, String imagePath, String notes, boolean checkOut) {
        this.faceImage = faceImage;
        this.imagePath = imagePath;
        this.notes = notes;
        this.checkOut = checkOut;
    }

    // Getters and Setters
    public String getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCheckOut() {
        return checkOut;
    }

    public void setCheckOut(boolean checkOut) {
        this.checkOut = checkOut;
    }
}
