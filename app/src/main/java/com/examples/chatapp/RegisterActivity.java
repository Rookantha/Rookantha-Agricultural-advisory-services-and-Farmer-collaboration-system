package com.examples.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.examples.chatapp.InternetCheck.CheckInternetConnection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity
{
    private EditText Email,Name,Mobile,Password,Confirm_Password;
    private Button Register,Login;
    private ProgressDialog message;
    private FirebaseAuth mAuth;
    String EmailAddress,FullName,Contact,Password1,Password2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initiate();
        final CheckInternetConnection connection = new CheckInternetConnection();
        String GoBack = getString(R.string.back);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(""+GoBack);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                    finish();
                    startActivity(intent);
                }
                catch (Exception ex)
                {
                    //Log.e("",ex.getMessage())
                }
            }
        });

        Register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String dialogMessage = getString(R.string.register_dialog_message);
                final String failRegister = getString(R.string.register_fail);
                String internetFail = getString(R.string.internetFail);

                if(connection.NetworkState(getApplicationContext()))
                {
                    if(validate())
                    {


                        String email = Email.getText().toString().trim();
                        String pass = Password.getText().toString().trim();
                        message.setMessage(""+dialogMessage);
                        message.show();
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task)
                            {
                                if(!task.isSuccessful())
                                {
                                    message.dismiss();
                                    Toast.makeText(RegisterActivity.this, ""+failRegister, Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    sendEmailVerification();
                                }
                            }
                        });
                    }
                }
                else
                {
                    Toast.makeText(RegisterActivity.this,""+internetFail,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initiate()
    {
        Email = (EditText) findViewById(R.id.Register_Email);
        Name = (EditText) findViewById(R.id.Register_Name);
        Mobile = (EditText) findViewById(R.id.Register_Mobile);
        Password = (EditText) findViewById(R.id.Register_Password);
        Confirm_Password = (EditText) findViewById(R.id.Register_Confirm_Password);
        Register = (Button) findViewById(R.id.btnRegister);
        Login = (Button) findViewById(R.id.btnLogin);
        mAuth = FirebaseAuth.getInstance();
        message = new ProgressDialog(this);
    }


    private  boolean validate()
    {
        Boolean result = false;

        Resources resources = getResources();
        String [] message = resources.getStringArray(R.array.register_validation_message);

        FullName = Name.getText().toString();
        EmailAddress = Email.getText().toString();
        Contact = Mobile.getText().toString();
        Password1 = Password.getText().toString();
        Password2 = Confirm_Password.getText().toString();

        if(FullName.isEmpty())
        {
            Toast.makeText(RegisterActivity.this,""+message[0],Toast.LENGTH_LONG).show();
        }

        else if(EmailAddress.isEmpty())
        {
            Toast.makeText(RegisterActivity.this,""+message[1],Toast.LENGTH_LONG).show();
        }

        else if(Contact.isEmpty())
        {
            Toast.makeText(RegisterActivity.this,""+message[2],Toast.LENGTH_LONG).show();
        }

        else if(Password1.isEmpty() || Password2.isEmpty())
        {
            Toast.makeText(RegisterActivity.this,""+message[3],Toast.LENGTH_LONG).show();
        }

        else if(!Password1.equals(Password2))
        {
            Toast.makeText(RegisterActivity.this,""+message[4],Toast.LENGTH_LONG).show();
        }
        else if(Password1.length()<6)
        {
            Toast.makeText(RegisterActivity.this,""+message[5],Toast.LENGTH_LONG).show();
        }
        else
        {
            result = true;
        }

        return result;
    }

    private void sendEmailVerification()
    {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if(fUser!=null)
        {
            fUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>()
            {
                String registerSuccess = getString(R.string.registerSuccess);
                String registerFailed = getString(R.string.registerFailed);
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(RegisterActivity.this, ""+registerSuccess, Toast.LENGTH_SHORT).show();
                        sendUserData();
                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        mAuth.signOut();
                        finish();
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this, ""+registerFailed, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUserData()
    {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference("Users").child(Objects.requireNonNull(mAuth.getUid()));
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("id",mAuth.getUid());
        hashMap.put("mobile",Contact);
        hashMap.put("username",FullName);
        hashMap.put("email",EmailAddress);
        hashMap.put("imageURL","default");
        hashMap.put("status","offline");
        hashMap.put("search",FullName.toLowerCase());
        reference.setValue(hashMap);
    }



}