package a.gautham.tasker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import a.gautham.tasker.models.ReminderList;
import a.gautham.tasker.models.TasksModel;

public class AddTasks extends AppCompatActivity {

    private RelativeLayout loadingLayout;
    private ProgressBar progressBar;
    private EditText date, time, taskTitle;
    private ImageView datePicker, timePicker;
    private Switch aSwitch;
    private FloatingActionButton addTask;
    private boolean reminder = false;
    private String id;
    private TextView reminder_text;
    private boolean timeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tasks);

        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        datePicker = findViewById(R.id.date_picker);
        timePicker = findViewById(R.id.time_picker);
        aSwitch = findViewById(R.id.switch_);
        addTask = findViewById(R.id.addTask);
        taskTitle = findViewById(R.id.taskTitle);
        reminder_text = findViewById(R.id.reminder_text);

        progressBar = findViewById(R.id.progressbar);
        loadingLayout = findViewById(R.id.loadingLayout);

        date.setKeyListener(null);
        time.setKeyListener(null);

        if (aSwitch.isChecked()){
            datePicker.setEnabled(true);
            timePicker.setEnabled(true);
        }else {
            datePicker.setEnabled(false);
            timePicker.setEnabled(false);
        }

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                reminder = isChecked;

                if (isChecked){
                    datePicker.setEnabled(true);
                    timePicker.setEnabled(true);
                }else {
                    datePicker.setEnabled(false);
                    timePicker.setEnabled(false);
                }
            }
        });

        if (getIntent()!=null){

            String t = getIntent().getStringExtra("title");
            String rDate = getIntent().getStringExtra("date");
            String rTime = getIntent().getStringExtra("time");
            id = getIntent().getStringExtra("id");

            if (t!=null&&!t.isEmpty())
                taskTitle.setText(t);

            if (rDate!=null&&!rDate.isEmpty())
                date.setText(rDate);

            if (rTime!=null&&!rTime.isEmpty())
                time.setText(rTime);

        }

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog();
            }
        });

        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getReminderDate().isEmpty()){
                    Common.toastShort(getApplicationContext(),"Please select date first");
                }else {
                    timeDialog();
                }
            }
        });

        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser()==null){
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class)
                    .putExtra("title",getTaskTitle().isEmpty() ? "default" : getTaskTitle())
                    .putExtra("content","default"));
                }else {
                    saveTask();
                }
            }
        });

    }

    private void saveTask() {

        getTaskTitle();
        if (getTaskTitle().isEmpty()){
            Toast.makeText(AddTasks.this, "Title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        loading(true);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tasks")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        String lastEdited = new SimpleDateFormat("dd-MM-yyyy | hh:mm a", Locale.getDefault()).format(new Date());

        TasksModel tasksModel = new TasksModel(getTaskTitle(), reminder ? getReminderDate() : "default",
                reminder ? getReminderTime() : "default", id!=null && !id.isEmpty() ? lastEdited : "default");

        final String uid = String.valueOf(System.currentTimeMillis());

        if (id!=null && !id.isEmpty()){
            reference = reference.child(id);
        }else {
            reference = reference.child(uid);
        }

        reference.setValue(tasksModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            if (reminder){
                                ReminderList reminderList = new ReminderList(getTaskTitle(),
                                        uid, getReminderDate(), getReminderTime(), "Tasks");

                                DatabaseReference reminderRef = FirebaseDatabase.getInstance().getReference("Reminders")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(uid);

                                reminderRef.setValue(reminderList);
                            }

                            loading(false);

                            if (id!=null && id.isEmpty()){
                                Common.toastShort(getApplicationContext(),"Updated Task");
                            }else {
                                Common.toastShort(getApplicationContext(),"Task saved");
                            }
                            onBackPressed();

                        }
                    }
                });

    }

    private void loading(boolean value){
        if (value){
            loadingLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            aSwitch.setEnabled(false);
            taskTitle.setEnabled(false);
            timePicker.setEnabled(false);
            datePicker.setEnabled(false);
            addTask.setEnabled(false);
        }else {
            loadingLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            aSwitch.setEnabled(true);
            taskTitle.setEnabled(true);
            timePicker.setEnabled(true);
            datePicker.setEnabled(true);
            addTask.setEnabled(true);
        }
    }

    private String getTaskTitle() {
        return taskTitle.getText().toString();
    }

    private String getReminderDate(){
        return date.getText().toString();
    }

    private String getReminderTime(){
        return time.getText().toString();
    }

    private void timeDialog() {

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

                if (LocalDate.parse(getReminderDate()).isEqual(currentDate)){

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

                String timeToShow = strHrsToShow + ":"+datetime.get(Calendar.MINUTE)+" "+am_pm;

                time.setText(timeToShow);
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

                if (getReminderTime().isEmpty()){
                    Common.toastShort(getApplicationContext(), "Please select a valid time");
                    return;
                }

                if (!timeSelected) {
                    Common.toastShort(getApplicationContext(), "Please select a valid time");
                    return;
                }

                setReminder_text();
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

    private void dateDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.date_picker, null);
        builder.setView(view);

        DatePicker datePicker = view.findViewById(R.id.date_picker);
        datePicker.setMinDate(System.currentTimeMillis());

        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());

        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                LocalDate selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth);

                if (selectedDate.isBefore(currentDate)) {
                    Common.toastLong(getApplicationContext(), getString(R.string.before_date_error));
                }

                date.setText(selectedDate.toString());

            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(
                getApplicationContext(),R.color.colorAccent));

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getReminderDate().isEmpty()){
                    Common.toastShort(getApplicationContext(),getString(R.string.select_date));
                    return;
                }

                if (LocalDate.parse(getReminderDate()).isBefore(currentDate)){
                    Common.toastLong(getApplicationContext(), getString(R.string.selected_valid_date));
                    return;
                }

                setReminder_text();
                alertDialog.dismiss();

            }
        });

    }

    private void setReminder_text(){

        if (!aSwitch.isChecked())
            return;

        if (getReminderDate().isEmpty()) {
            reminder_text.setVisibility(View.GONE);
            return;
        }

        if (getReminderTime().isEmpty()) {
            reminder_text.setVisibility(View.GONE);
            return;
        }

        reminder_text.setVisibility(View.VISIBLE);

        reminder_text.setText(String.format(Locale.getDefault(),"Reminder set for %s, %s", getReminderDate(), getReminderTime()));

    }

}