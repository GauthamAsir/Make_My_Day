package a.gautham.tasker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import a.gautham.tasker.models.NotesModel;
import a.gautham.tasker.models.ReminderList;

public class AddNotes extends AppCompatActivity{

    private RelativeLayout loading_layout;
    private ProgressBar progressbar;
    private TextInputLayout notesTitle;
    private EditText notesContent;
    private Button reminder;
    private TextView save_notes;
    private boolean loadingVal = false, a = false, timeSelected = false;
    private String reminderDate, id = null, reminderTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView toolbar_title = findViewById(R.id.toolbar_title);

        save_notes = findViewById(R.id.save_notes);
        loading_layout = findViewById(R.id.loading_layout);
        progressbar = findViewById(R.id.progressbar);
        notesTitle = findViewById(R.id.notesTitle);
        notesContent = findViewById(R.id.notesContent);
        reminder = findViewById(R.id.reminder);

        if (getIntent()!=null){
            String t = getIntent().getStringExtra("title");
            String c = getIntent().getStringExtra("content");
            boolean reminder = getIntent().getBooleanExtra("reminder",false);

            String date = getIntent().getStringExtra("date");
            String time = getIntent().getStringExtra("time");

            if (date!=null && !date.isEmpty() && !date.equals("default"))
                reminderDate = date;

            if (time!=null && !time.isEmpty() && !time.equals("default"))
                reminderTime = time;

            a = reminder;
            selectReminder(reminder);

            Objects.requireNonNull(notesTitle.getEditText()).setText(t);
            notesContent.setText(c);

            id = getIntent().getStringExtra("id");

        }

        if (id!=null&&!id.isEmpty()){
            toolbar_title.setText(getString(R.string.update_notes));
        }

        save_notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser()==null){
                    startActivity(new Intent(getApplicationContext(),LoginActivity.class)
                    .putExtra("title", getNotesTitle().isEmpty() ? "default" : getNotesTitle())
                    .putExtra("content", getNotesContent().isEmpty() ? "default" : getNotesContent()));
                    Common.toastShort(getApplicationContext(),getString(R.string.login_to_save_notes));
                    return;
                }

                validateSave(getNotesTitle(), getNotesContent());

            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a = !a;
                selectReminderDateDialog();
            }
        });

    }

    private void selectReminderDateDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        @SuppressLint("InflateParams") View view = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.date_picker,null);
        builder.setView(view);

        final DatePicker datePicker = view.findViewById(R.id.date_picker);

        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());

        datePicker.setMinDate(System.currentTimeMillis());

        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                LocalDate selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth);

                reminderDate = selectedDate.toString();

                System.out.println(reminderDate);

                if (selectedDate.isBefore(currentDate)) {
                    Common.toastLong(getApplicationContext(), getString(R.string.before_date_error));
                }

            }
        });

        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                a = false;
                selectReminder(false);
                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reminderDate==null){
                    Common.toastShort(getApplicationContext(),getString(R.string.select_date));
                    return;
                }

                if (LocalDate.parse(reminderDate).isBefore(currentDate)){
                    Common.toastLong(getApplicationContext(), getString(R.string.selected_valid_date));
                }else {
                    alertDialog.dismiss();
                    selectReminderTimeDialog();
                }
            }
        });

    }

    private void selectReminderTimeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.time_picker, null);
        builder.setView(view);
        builder.setCancelable(false);

        TimePicker timePicker = view.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(false);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

                int selectedTime = hourOfDay + minute;

                final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());

                if (LocalDate.parse(reminderDate).isEqual(currentDate)){

                    Calendar cal = Calendar.getInstance();

                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    int hourofday = cal.get(Calendar.HOUR_OF_DAY);
                    int minuteofday = cal.get(Calendar.MINUTE);
                    int totalCurrenttime = hourofday + minuteofday;

                    cal.set(year, month, day);

                    if (validTime(totalCurrenttime, selectedTime, cal.getTimeInMillis())) {
                        timeSelected = true;
                    } else {
                        Common.toastShort(getApplicationContext(),"Sorry we can't do time travelling, so please select another Date/Time");
                        timeSelected = false;
                    }

                }else {
                    timeSelected = true;
                }

                String am_pm = "";

                Calendar datetime = Calendar.getInstance();
                datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                datetime.set(Calendar.MINUTE, minute);

                if (datetime.get(Calendar.AM_PM) == Calendar.AM)
                    am_pm = "AM";
                else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
                    am_pm = "PM";

                String strHrsToShow = (datetime.get(Calendar.HOUR) == 0) ?"12":datetime.get(Calendar.HOUR)+"";

                reminderTime = strHrsToShow + ":"+datetime.get(Calendar.MINUTE)+" "+am_pm;


            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog alertDialog2 = builder.create();
        alertDialog2.show();
        alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(
                getApplicationContext(),R.color.colorAccent));

        alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println(timeSelected);

                if (reminderTime==null || reminderTime.isEmpty()){
                    Common.toastShort(getApplicationContext(), "Please select a valid time");
                    return;
                }

                if (!timeSelected) {
                    Common.toastShort(getApplicationContext(), "Please select a valid time");
                    return;
                }

                Common.toastShort(getApplicationContext(),"Reminder set for " + reminderDate + " : " + reminderTime);
                selectReminder(true);
                a = true;
                alertDialog2.dismiss();

            }
        });

    }

    private boolean validTime(long current, long selected, long dateSet) {

        if (dateSet > System.currentTimeMillis()) {
            return true;
        } else {
            boolean isValid = false;
            if (selected > current) {
                isValid = true;
            }
            return isValid;
        }
    }

    private void selectReminder(boolean value){
        if (value){
            reminder.setText(R.string.reminder_selected);
            reminder.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.overlayBackground));
            reminder.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_round_done_24, 0, 0, 0);
        }else {
            reminder.setText(R.string.set_reminder);
            reminder.setBackgroundColor(Color.TRANSPARENT);
            reminder.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_round_notifications_24, 0);
        }
    }

    private void validateSave(final String notesTitle, String notesContent) {

        if (notesTitle.isEmpty() && notesContent.isEmpty()){
            Common.toastShort(getApplicationContext(),"Can't save empty note");
            return;
        }

        if (notesTitle.isEmpty()){
            warnDialog("Title");
            return;
        }else if (notesContent.isEmpty()){
            warnDialog("Content");
            return;
        }

        loading(true);

        String lastEdited = new SimpleDateFormat("dd-MM-yyyy | hh:mm a", Locale.getDefault()).format(new Date());

        NotesModel notesModel = new NotesModel(notesTitle, notesContent,
                a ? reminderDate : "default", a ? reminderTime : "default",
                id != null && !id.isEmpty() ? lastEdited : "default");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notes")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        final String uid = String.valueOf(System.currentTimeMillis());

        if (id!=null && !id.isEmpty()){
            reference = reference.child(id);
        }else {
            reference = reference.child(uid);
        }

        reference.setValue(notesModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            if (a){
                                ReminderList reminderList = new ReminderList(notesTitle,
                                        id!=null && !id.isEmpty() ? id : uid, reminderDate, reminderTime, "Notes");

                                DatabaseReference reminderRef = FirebaseDatabase.getInstance().getReference("Reminders")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                                if (id!=null && !id.isEmpty()){
                                    reminderRef = reminderRef.child(id);
                                }else {
                                    reminderRef = reminderRef.child(uid);
                                }

                                reminderRef.setValue(reminderList);
                            }

                            loading(false);
                            if (id!=null&&!id.isEmpty())
                                Common.toastShort(getApplicationContext(),getString(R.string.updated_to_database));
                            else
                                Common.toastShort(getApplicationContext(),getString(R.string.saved_to_database));
                            onBackPressed();
                        }else {
                            loading(false);
                            Common.toastShort(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });

    }

    private void warnDialog(final String msg){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(String.format(Locale.getDefault(),"%s is empty, Do you still want to save?",msg));
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (msg.equalsIgnoreCase("Title")){
                    validateSave("default", getNotesContent());
                }else {
                    validateSave(getNotesContent(), "default");
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));

    }

    public String getNotesTitle() {
        return Objects.requireNonNull(notesTitle.getEditText()).getText().toString();
    }

    public String getNotesContent() {
        return notesContent.getText().toString();
    }

    private void loading(boolean value){

        loadingVal = value;

        if (value){
            loading_layout.setVisibility(View.VISIBLE);
            progressbar.setVisibility(View.VISIBLE);
            notesTitle.setEnabled(false);
            notesContent.setEnabled(false);
            reminder.setEnabled(false);
            save_notes.setEnabled(false);
        }else {
            loading_layout.setVisibility(View.GONE);
            progressbar.setVisibility(View.GONE);
            notesTitle.setEnabled(true);
            notesContent.setEnabled(true);
            reminder.setEnabled(true);
            save_notes.setEnabled(true);
        }

    }

    @Override
    public void onBackPressed() {
        if (loadingVal){
            Common.toastShort(getApplicationContext(),getString(R.string.disable_back_button));
        }else {
            super.onBackPressed();
        }
    }
}