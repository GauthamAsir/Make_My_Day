package a.gautham.tasker.models;

public class ReminderList {

    private String title, uid, reminderDate, reminderTime, type;

    public ReminderList() {
    }

    public ReminderList(String title, String uid, String reminderDate, String reminderTime, String type) {
        this.title = title;
        this.uid = uid;
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
