package com.examples.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity
{

    private EditText Email;
    private Button Reset;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        String GoBack = getString(R.string.back);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(""+GoBack);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Email = (EditText) findViewById(R.id.send_email);
        Reset = (Button) findViewById(R.id.btn_reset);
        firebaseAuth = FirebaseAuth.getInstance();

        Reset.setOnClickListener(new View.OnClickListener()
        {
            String nullEmail = getString(R.string.null_email);
            String checkEmail = getString(R.string.check_email);
            @Override
            public void onClick(View v)
            {
                String email = Email.getText().toString();
                if(email.equals(""))
                {
                    Toast.makeText(ResetPasswordActivity.this,""+nullEmail,Toast.LENGTH_LONG).show();
                }
                else
                {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(ResetPasswordActivity.this,""+checkEmail,Toast.LENGTH_LONG).show();
                                startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                            }
                            else
                            {
                                String error = task.getException().getMessage();
                                Toast.makeText(ResetPasswordActivity.this,error,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
