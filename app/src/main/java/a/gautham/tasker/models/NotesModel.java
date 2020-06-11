package a.gautham.tasker.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotesModel {

  private String title, content, date, time;
  private String reminderDate, reminderTime;
  private String lastEdited;

  public NotesModel() {
  }

  public NotesModel(String title, String content, String reminderDate, String reminderTime, String lastEdited) {
    this.title = title;
    this.content = content;
    this.reminderDate = reminderDate;
    this.reminderTime = reminderTime;
    this.lastEdited = lastEdited;
    this.date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    this.time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getReminderDate() {
    return reminderDate;
  }

  public void setReminderDate(String reminderDate) {
    this.reminderDate = reminderDate;
  }

  public String getLastEdited() {
    return lastEdited;
  }

  public void setLastEdited(String lastEdited) {
    this.lastEdited = lastEdited;
  }

  public String getReminderTime() {
    return reminderTime;
  }

  public void setReminderTime(String reminderTime) {
    this.reminderTime = reminderTime;
  }
}