package a.gautham.tasker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class Dashboard extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton add_fab;
    private LinearLayout addNotes, addTasks;
    private boolean isFABOpen = false;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private long back_pressed;
    private ConstraintLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        add_fab = findViewById(R.id.add_fab);
        addNotes = findViewById(R.id.addNotes_container);
        addTasks = findViewById(R.id.addTask_container);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        root =findViewById(R.id.container);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_tasks, R.id.navigation_dashboard, R.id.navigation_notes)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        SharedPreferences preferences = getSharedPreferences(Common.FlagsPref, Context.MODE_PRIVATE);
        preferences.edit().putString(Common.NewUser,"1").apply();

        add_fab.setOnClickListener(this);
        addNotes.setOnClickListener(this);
        addTasks.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        animateFAB();

        switch (v.getId()){
            case R.id.addNotes_container:
                startActivity(new Intent(getApplicationContext(),AddNotes.class));
                break;
            case R.id.addTask_container:
                startActivity(new Intent(getApplicationContext(),AddTasks.class));
                break;
        }

    }

    public void animateFAB() {

        if (isFABOpen) {
            add_fab.startAnimation(rotate_backward);
            addNotes.startAnimation(fab_close);
            addTasks.startAnimation(fab_close);
            isFABOpen = false;
        } else {
            add_fab.startAnimation(rotate_forward);
            addNotes.startAnimation(fab_open);
            addTasks.startAnimation(fab_open);
            isFABOpen = true;
        }
    }

    @Override
    public void onBackPressed() {

        if (back_pressed + 2000 > System.currentTimeMillis()){
            finish();
            moveTaskToBack(true);
            System.exit(1);
            android.os.Process.killProcess(android.os.Process.myPid());
        }else {
            Snackbar.make(root, getString(R.string.press_again_to_exit), Snackbar.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }

    }
}