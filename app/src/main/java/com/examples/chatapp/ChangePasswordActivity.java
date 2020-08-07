package com.examples.chatapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.examples.chatapp.Fragments.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static java.security.AccessController.getContext;

public class ChangePasswordActivity extends AppCompatActivity
{
    private Button ChangePassword;
    private EditText NewPassword,NewPasswordConfirm;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        String GoBack = getString(R.string.back);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(""+GoBack);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    startActivity(new Intent(ChangePasswordActivity.this,MainActivity.class));
                }
                catch (Exception e)
                {
                    Toast.makeText(ChangePasswordActivity.this, ""+e, Toast.LENGTH_LONG).show();
                }
            }
        });


        NewPassword = (EditText) findViewById(R.id.txtNew_Password);
        NewPasswordConfirm = (EditText) findViewById(R.id.txtNew_Confirm);
        ChangePassword = (Button) findViewById(R.id.btnChange_Password);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();

        ChangePassword.setOnClickListener(new View.OnClickListener()
        {
            String FailChange = getString(R.string.fail_change);
            String SuccessChange = getString(R.string.success_change);

            @Override
            public void onClick(View v)
            {
                String newPass = NewPassword.getText().toString();

                if(Validate())
                {
                    firebaseUser.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(!task.isSuccessful())
                                {
                                    Toast.makeText(ChangePasswordActivity.this, ""+FailChange, Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    Toast.makeText(ChangePasswordActivity.this, ""+SuccessChange, Toast.LENGTH_LONG).show();
                                    firebaseAuth.signOut();
                                    finish();
                                    startActivity(new Intent(ChangePasswordActivity.this,LoginActivity.class));
                                }
                            }
                        });
                    }
            }
        });

    }

    private boolean Validate()
    {
        Resources resources = getResources();
        String [] messages = resources.getStringArray(R.array.register_validation_message);

        Boolean result = false;
        String Pass1 = NewPassword.getText().toString();
        String Pass2 = NewPasswordConfirm.getText().toString();

        if(Pass1.isEmpty() || Pass2.isEmpty())
        {
            Toast.makeText(ChangePasswordActivity.this,""+messages[3],Toast.LENGTH_LONG).show();
        }
        else if(!Pass1.equals(Pass2))
        {
            Toast.makeText(ChangePasswordActivity.this,""+messages[4],Toast.LENGTH_LONG).show();
        }
        else if(Pass1.length()<6)
        {
            Toast.makeText(ChangePasswordActivity.this,""+messages[5],Toast.LENGTH_LONG).show();
        }
        else
        {
            result = true;
        }
        return result;
    }
}
