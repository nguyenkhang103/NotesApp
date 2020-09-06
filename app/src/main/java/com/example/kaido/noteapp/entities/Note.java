package com.example.kaido.noteapp.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "notes")
public class Note implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "subtitle")
    private String subtitle;
    @ColumnInfo(name = "date_time")
    private String dateTime;
    @ColumnInfo(name = "note_content")
    private String noteContent;
    @ColumnInfo(name = "image_path")
    private String imagePath;
    @ColumnInfo(name = "color")
    private String color;
    @ColumnInfo(name = "web_link")
    private String url;
    @ColumnInfo(name = "voice_record")
    private String voiceRecorder;
    @TypeConverters(DateConverter.class)
    Date timeReminder;
    // title
    private int titleColor;
    private String titleFontFamily;
    private float titleFontSize;
    private String titleAlign;
    private boolean isTitleBold;
    private boolean isTitleItalic;
    private boolean isTitleUnderLined;
    //content
    private int contentColor;
    private String contentFontFamily;
    private float contentFontSize;
    private String contentAlign;
    private boolean isContentBold;
    private boolean isContentItalic;
    private boolean isContentUnderLined;

    public Note() {
    }

    public int getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }

    public String getTitleFontFamily() {
        return titleFontFamily;
    }

    public void setTitleFontFamily(String titleFontFamily) {
        this.titleFontFamily = titleFontFamily;
    }

    public float getTitleFontSize() {
        return titleFontSize;
    }

    public void setTitleFontSize(float titleFontSize) {
        this.titleFontSize = titleFontSize;
    }

    public String getTitleAlign() {
        return titleAlign;
    }

    public void setTitleAlign(String titleAlign) {
        this.titleAlign = titleAlign;
    }

    public boolean isTitleBold() {
        return isTitleBold;
    }

    public void setTitleBold(boolean titleBold) {
        isTitleBold = titleBold;
    }

    public boolean isTitleItalic() {
        return isTitleItalic;
    }

    public void setTitleItalic(boolean titleItalic) {
        isTitleItalic = titleItalic;
    }

    public boolean isTitleUnderLined() {
        return isTitleUnderLined;
    }

    public void setTitleUnderLined(boolean titleUnderLined) {
        isTitleUnderLined = titleUnderLined;
    }

    public int getContentColor() {
        return contentColor;
    }

    public void setContentColor(int contentColor) {
        this.contentColor = contentColor;
    }

    public String getContentFontFamily() {
        return contentFontFamily;
    }

    public void setContentFontFamily(String contentFontFamily) {
        this.contentFontFamily = contentFontFamily;
    }

    public float getContentFontSize() {
        return contentFontSize;
    }

    public void setContentFontSize(float contentFontSize) {
        this.contentFontSize = contentFontSize;
    }

    public String getContentAlign() {
        return contentAlign;
    }

    public void setContentAlign(String contentAlign) {
        this.contentAlign = contentAlign;
    }

    public boolean isContentBold() {
        return isContentBold;
    }

    public void setContentBold(boolean contentBold) {
        isContentBold = contentBold;
    }

    public boolean isContentItalic() {
        return isContentItalic;
    }

    public void setContentItalic(boolean contentItalic) {
        isContentItalic = contentItalic;
    }

    public boolean isContentUnderLined() {
        return isContentUnderLined;
    }

    public void setContentUnderLined(boolean contentUnderLined) {
        isContentUnderLined = contentUnderLined;
    }

    public Date getTimeReminder() {
        return timeReminder;
    }

    public void setTimeReminder(Date timeReminder) {
        this.timeReminder = timeReminder;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVoiceRecorder() {
        return voiceRecorder;
    }

    public void setVoiceRecorder(String voiceRecorder) {
        this.voiceRecorder = voiceRecorder;
    }

    @NonNull
    @Override
    public String toString() {
        return title + " : " + dateTime;
    }
}

