package a.gautham.tasker;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.Fade;
import androidx.transition.Transition;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Splash extends AppCompatActivity {

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final FloatingActionButton fab = findViewById(R.id.next_bt);

        SharedPreferences preferences = getSharedPreferences(Common.FlagsPref, Context.MODE_PRIVATE);

        if (!preferences.contains(Common.NewUser)) {
            preferences.edit().putString(Common.NewUser, "0").apply();
        }

        String newUser = preferences.getString(Common.NewUser, "");

        if (!newUser.isEmpty() && newUser.equals("1")) {
            fab.setVisibility(View.INVISIBLE);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), Dashboard.class));
                    finish();
                }
            }, 700);

            return;
        }

        fab.setVisibility(View.VISIBLE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Dashboard.class);

                String trans = getString(R.string.change_bounds);

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(Splash.this, fab, trans);
                startActivity(intent, activityOptions.toBundle());
                getWindow().setAllowEnterTransitionOverlap(false);
                getWindow().setAllowReturnTransitionOverlap(false);
                
            }
        });

    }

    public static Transition makeEnterTransition() {
        Transition fade = new Fade();
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        return fade;
    }

}