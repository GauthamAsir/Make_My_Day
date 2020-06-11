package a.gautham.tasker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout signInEmail, signInPass;
    private TextView forgot_pwd, register_here;
    private Button login_bt;
    private ProgressBar progressBar;
    private RelativeLayout loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInEmail = findViewById(R.id.signInEmail);
        signInPass = findViewById(R.id.signInPass);

        forgot_pwd = findViewById(R.id.forgot_pwd);
        register_here = findViewById(R.id.register_here);
        login_bt = findViewById(R.id.login_bt);
        progressBar = findViewById(R.id.progressbar);
        loadingLayout = findViewById(R.id.loading);

        forgot_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgot_pwd();
            }
        });
        login_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        register_here.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);

                String title = getIntent().getStringExtra("title");
                String content = getIntent().getStringExtra("content");
                String type = getIntent().getStringExtra("type");

                if (title!=null && !title.isEmpty() && content!=null && !content.isEmpty()) {
                    intent.putExtra("title", title)
                            .putExtra("content", content)
                            .putExtra("type", type);
                }

                startActivity(intent);

            }
        });

    }

    private void login(){
        getSignInEmail();
        if (getSignInEmail().isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(getSignInEmail()).matches()){
            Common.toastShort(getApplicationContext(),getString(R.string.enter_valid_mail), Gravity.CENTER);
            signInEmail.setError(getString(R.string.enter_valid_mail));
            return;
        }

        signInEmail.setError(null);

        getSignInPass();
        if (getSignInPass().isEmpty()){
            Common.toastShort(getApplicationContext(),getString(R.string.empty_pass_error), Gravity.CENTER);
            signInPass.setError(getString(R.string.empty_pass_error));
            return;
        }

        signInPass.setError(null);

        if (getSignInPass().length() <= 5) {
            Common.toastShort(getApplicationContext(),getString(R.string.pass_less_length_error));
            signInPass.setError(getString(R.string.pass_less_length_error));
            return;
        }

        signInPass.setError(null);

        loading(true);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(getSignInEmail(), getSignInPass())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            if (getIntent()!=null){
                                String title = getIntent().getStringExtra("title");
                                String content = getIntent().getStringExtra("content");
                                String type = getIntent().getStringExtra("type");

                                if (title==null || title.isEmpty() || content==null || content.isEmpty()){

                                    loading(false);
                                    Common.toastLong(getApplicationContext(), getString(R.string.login_success));
                                    startActivity(new Intent(getApplicationContext(), Dashboard.class));
                                    finish();
                                    return;

                                }

                                loading(false);
                                Common.toastLong(getApplicationContext(), getString(R.string.login_success));
                                startActivity(new Intent(getApplicationContext(),
                                        type != null && !type.isEmpty() && type.equals("Tasks") ?
                                                AddTasks.class : AddNotes.class).putExtra("title", title)
                                        .putExtra("content", content));

                            }else {
                                loading(false);
                                Common.toastLong(getApplicationContext(),getString(R.string.login_success));
                                startActivity(new Intent(getApplicationContext(),Dashboard.class));
                            }
                            finish();

                        }else {
                            loading(false);
                            Common.toastLong(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage());
                        }

                    }
                });

    }

    private void forgot_pwd(){
        getSignInEmail();
        if (getSignInEmail().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(getSignInEmail()).matches()){
            Common.toastShort(getApplicationContext(),getString(R.string.enter_valid_mail), Gravity.CENTER);
            return;
        }

        loading(true);

        FirebaseAuth.getInstance().sendPasswordResetEmail(getSignInEmail())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        loading(false);

                        if (task.isSuccessful())
                            Common.toastLong(getApplicationContext(),getString(R.string.forgot_pass_mail_sent), Gravity.CENTER);
                        else {
                            Common.toastLong(getApplicationContext(),Objects.requireNonNull(task.getException()).getMessage(), Gravity.CENTER);
                        }
                    }
                });
    }

    private String getSignInEmail() {
        return Objects.requireNonNull(signInEmail.getEditText()).getText().toString();
    }

    private String getSignInPass() {
        return Objects.requireNonNull(signInPass.getEditText()).getText().toString();
    }

    private void loading(boolean value){
        if (value){
            progressBar.setVisibility(View.VISIBLE);
            loadingLayout.setVisibility(View.VISIBLE);
            login_bt.setEnabled(false);
            signInEmail.setEnabled(false);
            signInPass.setEnabled(false);
            register_here.setClickable(false);
            forgot_pwd.setClickable(false);
        }else {
            progressBar.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.GONE);
            login_bt.setEnabled(true);
            signInEmail.setEnabled(true);
            signInPass.setEnabled(true);
            register_here.setClickable(true);
            forgot_pwd.setClickable(true);
        }
    }
}