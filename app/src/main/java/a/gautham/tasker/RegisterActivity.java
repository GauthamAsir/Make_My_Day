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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import a.gautham.tasker.models.User;

public class RegisterActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private RelativeLayout loadingLayout;
    private TextInputLayout registerName, registerEmail, registerPass;
    private Button register_bt;
    private TextView loginHere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressBar = findViewById(R.id.progressbar);
        loadingLayout = findViewById(R.id.loading);
        registerName = findViewById(R.id.registerName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPass = findViewById(R.id.registerPass);

        register_bt = findViewById(R.id.register_bt);
        loginHere = findViewById(R.id.login_here);

        register_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

    }

    private void register() {

        getRegisterName();
        if (getRegisterName().isEmpty()){
            Common.toastShort(getApplicationContext(),getString(R.string.name_field_empty), Gravity.CENTER);
            registerName.setError(getString(R.string.name_field_empty));
            return;
        }

        registerName.setError(null);

        getRegisterEmail();
        if (getRegisterEmail().isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(getRegisterEmail()).matches()){
            Common.toastShort(getApplicationContext(),getString(R.string.enter_valid_mail), Gravity.CENTER);
            registerEmail.setError(getString(R.string.enter_valid_mail));
            return;
        }

        registerEmail.setError(null);

        getRegisterPass();
        if (getRegisterPass().isEmpty()){
            Common.toastShort(getApplicationContext(),getString(R.string.empty_pass_error), Gravity.CENTER);
            registerPass.setError(getString(R.string.empty_pass_error));
            return;
        }

        registerPass.setError(null);

        if (getRegisterPass().length() <= 5) {
            Common.toastShort(getApplicationContext(),getString(R.string.pass_less_length_error));
            registerPass.setError(getString(R.string.pass_less_length_error));
            return;
        }

        registerPass.setError(null);

        loading(true);

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(getRegisterEmail(), getRegisterPass())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            User user = new User(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),
                                    getRegisterEmail(), getRegisterName(),
                                    new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);

                            if (getIntent()!=null){
                                String title = getIntent().getStringExtra("title");
                                String content = getIntent().getStringExtra("content");
                                String type = getIntent().getStringExtra("type");

                                if (title==null || title.isEmpty() || content==null || content.isEmpty()){

                                    loading(false);
                                    Common.toastLong(getApplicationContext(), getString(R.string.register_successfull));
                                    startActivity(new Intent(getApplicationContext(), Dashboard.class));
                                    finish();
                                    return;

                                }

                                loading(false);
                                Common.toastLong(getApplicationContext(), getString(R.string.register_successfull));
                                startActivity(new Intent(getApplicationContext(),
                                        type != null && !type.isEmpty() && type.equals("Tasks") ?
                                                AddTasks.class : AddNotes.class)
                                        .putExtra("title", title)
                                        .putExtra("content", content));

                            }else {
                                loading(false);
                                Common.toastLong(getApplicationContext(),getString(R.string.register_successfull));
                                startActivity(new Intent(getApplicationContext(),Dashboard.class));
                            }
                            finish();

                        }else {
                            loading(false);
                        }
                    }
                });

    }

    public void login_here(View view) {
        onBackPressed();
    }

    public String getRegisterName() {
        return Objects.requireNonNull(registerName.getEditText()).getText().toString();
    }

    public String getRegisterEmail() {
        return Objects.requireNonNull(registerEmail.getEditText()).getText().toString();
    }

    public String getRegisterPass() {
        return Objects.requireNonNull(registerPass.getEditText()).getText().toString();
    }

    private void loading(boolean value){
        if (value){
            progressBar.setVisibility(View.VISIBLE);
            loadingLayout.setVisibility(View.VISIBLE);
            register_bt.setEnabled(false);
            registerEmail.setEnabled(false);
            registerPass.setEnabled(false);
            registerName.setEnabled(false);
            loginHere.setClickable(false);
        }else {
            progressBar.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.GONE);
            register_bt.setEnabled(true);
            registerEmail.setEnabled(true);
            registerPass.setEnabled(true);
            registerName.setEnabled(true);
            loginHere.setClickable(true);
        }
    }

}