package a.gautham.tasker.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TasksModel {

    private String title, createdDateTime, reminderDate, reminderTime, lastEdited;

    public TasksModel() {
    }

    public TasksModel(String title, String reminderDate, String reminderTime, String lastEdited) {
        this.title = title;
        this.createdDateTime = new SimpleDateFormat("dd-MM-yyyy | hh:mm a", Locale.getDefault()).format(new Date());
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
        this.lastEdited = lastEdited;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(String lastEdited) {
        this.lastEdited = lastEdited;
    }
}
