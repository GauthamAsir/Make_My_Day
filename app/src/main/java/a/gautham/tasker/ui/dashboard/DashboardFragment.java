package a.gautham.tasker.ui.dashboard;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import a.gautham.tasker.LoginActivity;
import a.gautham.tasker.R;
import a.gautham.tasker.models.ReminderList;

public class DashboardFragment extends Fragment {

    private FirebaseRecyclerAdapter<ReminderList, ReminderViewHolder> adapter;
    private ProgressBar progressBar;
    private TextView error;
    private RecyclerView recyclerView;
    private DatabaseReference reminderRef;
    private SharedPreferences pref;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        progressBar = root.findViewById(R.id.progressbar);
        error = root.findViewById(R.id.error);

        recyclerView = root.findViewById(R.id.recyclerViewDashboard);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        pref = requireActivity().getSharedPreferences("Reminders", 0);

        reminderRef = FirebaseDatabase.getInstance()
                .getReference("Reminders")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        new SetReminder().execute();
        checkCount();
        UpdateUI();

        return root;
    }

    private void UpdateUI(){

        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.login_to_view_tasks);
            progressBar.setVisibility(View.GONE);
            error.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            });
            return;
        }

        final DatabaseReference notesRef = FirebaseDatabase.getInstance()
                .getReference("Notes")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        final DatabaseReference tasksRef = FirebaseDatabase.getInstance()
                .getReference("Tasks")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        FirebaseRecyclerOptions<ReminderList> options =
                new FirebaseRecyclerOptions.Builder<ReminderList>()
                        .setQuery(reminderRef, ReminderList.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<ReminderList, ReminderViewHolder>(options) {
            @NonNull
            @Override
            public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_dashboard_layout, parent, false);
                return new ReminderViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ReminderViewHolder reminderViewHolder,
                                            final int position, @NonNull final ReminderList reminderList) {

                reminderViewHolder.title.setText(reminderList.getTitle());

                ColorGenerator generator = ColorGenerator.MATERIAL;

                int color2 = generator.getColor(reminderList.getUid());

                TextDrawable drawable = TextDrawable.builder()
                        .beginConfig()
                        .textColor(Color.BLACK)
                        .useFont(Typeface.DEFAULT)
                        .bold()
                        .toUpperCase()
                        .endConfig()
                        .buildRound(String.valueOf(reminderList.getTitle().charAt(0)), color2);
                reminderViewHolder.letterAvatar.setImageDrawable(drawable);

                reminderViewHolder.reminderDateTime.setText(String.format(Locale.getDefault(),"%s | %s",
                        reminderList.getReminderDate(), reminderList.getReminderTime()));

                progressBar.setVisibility(View.GONE);

                reminderViewHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (reminderList.getType().equals("Notes"))
                            notesRef.child(Objects.requireNonNull(adapter.getRef(position).getKey())).removeValue();

                        if (reminderList.getType().equals("Tasks"))
                            tasksRef.child(Objects.requireNonNull(adapter.getRef(position).getKey())).removeValue();

                        reminderRef.child(Objects.requireNonNull(adapter.getRef(position).getKey())).removeValue();

                        notifyDataSetChanged();
                        checkCount();

                    }
                });

                reminderViewHolder.type.setText(reminderList.getType());

            }

        };

        checkCount();

        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    private void checkCount() {

        reminderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() <= 0) {
                    error.setVisibility(View.VISIBLE);
                    error.setText(R.string.no_reminders);
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    error.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    class SetReminder extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {

            reminderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("CommitPrefEdits")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        pref = requireActivity().getSharedPreferences("Reminders", 0);

                        if (!pref.contains(snapshot.getKey())) {

                            ReminderList reminderList = snapshot.getValue(ReminderList.class);

                            String time = Objects.requireNonNull(reminderList).getReminderTime().substring(0, reminderList.getReminderTime().length() - 2);
                            time = time.replace(" ", "");
                            int hour = Integer.parseInt(time.split(":")[0]);

                            int min = Integer.parseInt(time.split(":")[1]);

                            String amPM = reminderList.getReminderTime()
                                    .substring(reminderList.getReminderTime().length() - 2);

                            int day = Integer.parseInt(reminderList.getReminderDate().split("-")[2]);
                            int month = Integer.parseInt(reminderList.getReminderDate().split("-")[1]);
                            int year = Integer.parseInt(reminderList.getReminderDate().split("-")[0]);

                            Intent myIntent = new Intent(getActivity(), NotifyService.class);
                            AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
                            PendingIntent pendingIntent = PendingIntent.getService(getActivity(), 0, myIntent, 0);
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MINUTE, min);
                            calendar.set(Calendar.HOUR, hour);
                            calendar.set(Calendar.AM_PM, amPM
                                    .equalsIgnoreCase("AM") ? Calendar.AM : Calendar.PM);
                            calendar.set(Calendar.DAY_OF_MONTH, day);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.YEAR, year);
                            if (alarmManager != null) {
                                Log.d("HEY", "YES");
                                //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                                pref.edit().putString(snapshot.getKey(), snapshot.getKey()).apply();
                                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 60 * 24, pendingIntent);
                            }
                        } else {
                            Log.d("HEY", "NO");
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return null;
        }
    }

}