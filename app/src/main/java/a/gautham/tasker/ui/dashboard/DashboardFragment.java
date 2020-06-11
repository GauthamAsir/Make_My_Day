package a.gautham.tasker.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;
import java.util.Objects;

import a.gautham.tasker.Common;
import a.gautham.tasker.LoginActivity;
import a.gautham.tasker.R;
import a.gautham.tasker.models.NotesModel;
import a.gautham.tasker.models.ReminderList;
import a.gautham.tasker.ui.notes.NotesViewHolder;

public class DashboardFragment extends Fragment {

    private FirebaseRecyclerAdapter<ReminderList, ReminderViewHolder> adapter;
    private ProgressBar progressBar;
    private TextView error;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        progressBar = root.findViewById(R.id.progressbar);
        error = root.findViewById(R.id.error);

        recyclerView = root.findViewById(R.id.recyclerViewDashboard);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

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

        final DatabaseReference reminderRef = FirebaseDatabase.getInstance()
                .getReference("Reminders")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

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

                checkCount();

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

                    }
                });

                reminderViewHolder.type.setText(reminderList.getType());

            }

        };

        checkCount();

        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    private void checkCount(){
        if (adapter.getItemCount()==0){
            error.setVisibility(View.VISIBLE);
            error.setText(R.string.no_reminders);
            progressBar.setVisibility(View.GONE);
        }else {
            progressBar.setVisibility(View.VISIBLE);
            error.setVisibility(View.GONE);
        }
    }

}