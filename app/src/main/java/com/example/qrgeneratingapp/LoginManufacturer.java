package com.example.qrgeneratingapp;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrgeneratingapp.Extra.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class LoginManufacturer extends AppCompatActivity {

    private LoadingDialog loadingDialog;
    private TextView txtNewAccount;
    private Button btnLogin;
    EditText txtEmail,txtPassword;


    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;

    TextToSpeech ttobj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_manufacturer);
        loadingDialog = new LoadingDialog(this, R.style.DialogLoadingTheme);

        getSupportActionBar().setTitle("Login Manufacturer"); // for set actionbar title
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for add back arrow in action bar

        btnLogin = findViewById(R.id.btnLogin);
        txtNewAccount = findViewById(R.id.txtNewAccount);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);

        databaseReference = FirebaseDatabase.getInstance().getReference("Manufacturer");
        firebaseAuth = FirebaseAuth.getInstance();

        //Initializing Text to Speech
        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    ttobj.setLanguage(Locale.UK);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void RegisterActivity(View v){
        Intent intent = new Intent(this,RegisterManufacturer.class);
        startActivity(intent);
    }


    public void LoginManufacturer(View v){
        final String Email = txtEmail.getText().toString();
        String Password = txtPassword.getText().toString();
        Boolean flag = true;

        if(TextUtils.isEmpty(Email) && TextUtils.isEmpty(Password)){
            Toast.makeText(this, "Please Enter Email and Password!",
                    Toast.LENGTH_LONG).show();
            //Playing audio
            ttobj.speak("Please Enter Email and Password!", TextToSpeech.QUEUE_FLUSH, null);
            flag = false;
        }
        else{
            if(TextUtils.isEmpty(Email)){
                Toast.makeText(this, "Please Enter Email!",
                        Toast.LENGTH_LONG).show();
                //Playing audio
                ttobj.speak("Please Enter Email!", TextToSpeech.QUEUE_FLUSH, null);
                flag = false;
            }
            if(TextUtils.isEmpty(Password)){
                Toast.makeText(this, "Please Enter Password!",
                        Toast.LENGTH_LONG).show();
                //Playing audio
                ttobj.speak("Please Enter Password!", TextToSpeech.QUEUE_FLUSH, null);
                flag = false;
            }
        }

        if(flag == true){
            loadingDialog.show();
            firebaseAuth.signInWithEmailAndPassword(Email, Password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                loadingDialog.dismiss();
                                Intent intent = new Intent(LoginManufacturer.this,ManufacturerProfile.class);
                                startActivity(intent);

                            } else {
                                // If sign in fails, display a message to the user.
                                loadingDialog.dismiss();
                                ttobj.speak("Invalid Login!", TextToSpeech.QUEUE_FLUSH, null);

                                Toast.makeText(LoginManufacturer.this, "Invalid Login",
                                        Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });
        }
    }

}
