package a.gautham.tasker.ui.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import a.gautham.tasker.AddTasks;
import a.gautham.tasker.Common;
import a.gautham.tasker.LoginActivity;
import a.gautham.tasker.R;
import a.gautham.tasker.models.TasksModel;
import a.gautham.tasker.ui.notes.NotesViewHolder;

public class TasksFragment extends Fragment {

    private FirebaseRecyclerAdapter<TasksModel, NotesViewHolder> adapter;
    private ProgressBar progressBar;
    private TextView error;
    private RecyclerView recyclerView;
    private DatabaseReference reference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tasks, container, false);

        progressBar = root.findViewById(R.id.progressbar);
        error = root.findViewById(R.id.error);

        recyclerView = root.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            reference = FirebaseDatabase.getInstance()
                    .getReference("Tasks")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

            checkCount();
        }
        updateUI();

        return root;
    }

    private void updateUI() {

        SharedPreferences preferences = requireActivity()
                .getSharedPreferences(Common.FlagsPref, Context.MODE_PRIVATE);

        String newUser = preferences.getString(Common.NewUser, "");

        if (!newUser.isEmpty() && newUser.equals("0")) {

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                preferences.edit().putString(Common.NewUser, "1").apply();
                updateUI();
                return;
            }

            error.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            error.setText(R.string.welcome_new_user);
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
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

        FirebaseRecyclerOptions<TasksModel> options =
                new FirebaseRecyclerOptions.Builder<TasksModel>()
                        .setQuery(reference, TasksModel.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<TasksModel, NotesViewHolder>(options) {
            @NonNull
            @Override
            public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_tasks_layout, parent, false);
                return new NotesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NotesViewHolder notesViewHolder, final int position,
                                            @NonNull final TasksModel tasksModel) {
                notesViewHolder.notes_title.setText(tasksModel.getTitle());

                if (tasksModel.getReminderDate().equals("default")) {
                    notesViewHolder.reminder_icon.setVisibility(View.INVISIBLE);
                } else {
                    notesViewHolder.reminder_icon.setVisibility(View.VISIBLE);
                }

                if (tasksModel.getLastEdited().equals("default")){
                    notesViewHolder.editedTag.setText(getString(R.string.created));
                    notesViewHolder.createdDate.setText(tasksModel.getCreatedDateTime());
                }else {
                    notesViewHolder.editedTag.setText(getString(R.string.last_edited));
                    notesViewHolder.createdDate.setText(tasksModel.getLastEdited());
                }

                if (progressBar.getVisibility()==View.VISIBLE)
                    progressBar.setVisibility(View.GONE);

                notesViewHolder.delete_note.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reference.child(Objects.requireNonNull(adapter.getRef(position).getKey())).removeValue();
                        reminderRef.child(Objects.requireNonNull(adapter.getRef(position).getKey())).removeValue();
                        notifyDataSetChanged();
                        checkCount();
                    }
                });

                notesViewHolder.edit_note.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AddTasks.class)
                                .putExtra("title",tasksModel.getTitle())
                                .putExtra("id", adapter.getRef(position).getKey())
                                .putExtra("reminder", !tasksModel.getReminderDate().equals("default"))
                                .putExtra("date",tasksModel.getReminderDate())
                                .putExtra("time",tasksModel.getReminderTime()));
                    }
                });
            }

        };

        adapter.startListening();

        recyclerView.setAdapter(adapter);

    }

    private void checkCount() {

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() == 0) {
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

}