package com.aceburgundy.criminalintent;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Crime {
    private boolean requiresIntervention;
    private String suspectNumber;
    private String suspect;
    private boolean solved;
    private final UUID id;
    private String title;
    private Date date;
    private Date time;

    public Crime() {
        this(UUID.randomUUID());
    }

    public Crime(UUID id) {
        this.id = id;
        requiresIntervention = false;
        date = new Date();
        time = new Date();
    }

    public String getFormattedTime() {
        DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);
        return timeFormatter.format(time);
    }

    private DateFormat localeDateFormatter() {
        return java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG, Locale.getDefault());
    }

    public String getFormattedDate() {
        java.text.DateFormat dateFormat = localeDateFormatter();
        return dateFormat.format(date);
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }

    public String getSuspectNumber() {
        return suspectNumber;
    }

    public String getTitle() {
        return title;
    }

    public Date getTime() {
        return time;
    }


    public Date getDate() {
        return date;
    }


    public UUID getId() {
        return id;
    }

    public String getSuspectName() {
        return suspect;
    }

    public void setRequiresIntervention(boolean requiresIntervention) {
        this.requiresIntervention = requiresIntervention;
    }

    public void setSuspectNumber(String number) {
        suspectNumber = number;
    }

    public boolean requiresIntervention() {
        return requiresIntervention;
    }

    public void setSuspectName(String suspect) {
        this.suspect = suspect;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isSolved() {
        return solved;
    }

}
