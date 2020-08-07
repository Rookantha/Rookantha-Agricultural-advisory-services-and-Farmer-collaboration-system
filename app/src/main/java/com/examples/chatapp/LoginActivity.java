package com.examples.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.examples.chatapp.InternetCheck.CheckInternetConnection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity
{
    private FirebaseAuth auth;
    private EditText email,password;
    private Button Login,Register;
    String EmailAddress,Password;
    private ProgressDialog progressDialog;
    private TextView forgot_password;
    private ImageView outlook_icon,gmail_icon,yahoo_icon;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initiate();

        String GoBack = getString(R.string.back);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(""+GoBack);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String noInternet = getString(R.string.internetFail);
        final String loginFail = getString(R.string.login_fail_message);

        final CheckInternetConnection connection = new CheckInternetConnection();

            outlook_icon.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(connection.NetworkState(getApplicationContext()))
                    {
                        Intent outlook_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://signup.live.com/signup?lcid=1033&wa=wsignin1.0&rpsnv=13&ct=1556864009&rver=7.0.6737.0&wp=MBI_SSL&wreply=https%3a%2f%2foutlook.live.com%2fowa%2f%3fnlp%3d1%26signup%3d1%26RpsCsrfState%3d7fae5417-4276-c076-2561-d50f6e67272c&id=292841&CBCXT=out&lw=1&fl=dob%2cflname%2cwld&cobrandid=90015&lic=1&uaid=a15c5153aaae47a2ba2daac4c9435a0a"));
                        startActivity(outlook_intent);
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,""+noInternet,Toast.LENGTH_LONG).show();
                    }

                }
            });

            gmail_icon.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(connection.NetworkState(getApplicationContext()))
                    {
                        Intent gmail_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/signup/v2/webcreateaccount?flowName=GlifWebSignIn&flowEntry=SignUp"));
                        startActivity(gmail_intent);
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,""+noInternet,Toast.LENGTH_LONG).show();
                    }
                }
            });

            yahoo_icon.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(connection.NetworkState(getApplicationContext()))
                    {
                        Intent yahoo_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://login.yahoo.com/account/create?specId=yidReg"));
                        startActivity(yahoo_intent);
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,""+noInternet,Toast.LENGTH_LONG).show();
                    }
                }
            });

            forgot_password.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(connection.NetworkState(getApplicationContext()))
                    {
                        startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,""+noInternet,Toast.LENGTH_LONG).show();
                    }
                }
            });

            Register.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                    finish();
                    startActivity(intent);
                }
            });

            Login.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(connection.NetworkState(getApplicationContext()))
                    {
                        progressDialog.show();
                        if(validate())
                        {
                            auth.signInWithEmailAndPassword(EmailAddress,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        progressDialog.dismiss();
                                        checkEmailVerification();
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this,""+loginFail,Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,""+noInternet,Toast.LENGTH_LONG).show();
                    }
                }

            });
    }

    private void checkEmailVerification()
    {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        assert firebaseUser != null;
        Boolean mailflag = firebaseUser.isEmailVerified();
        if(mailflag)
        {
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(LoginActivity.this,"Verify your email to continue",Toast.LENGTH_LONG).show();
            auth.signOut();
        }
    }

    private void initiate()
    {
        String dialogLogMessage = getString(R.string.dialog_log_message);
        email = (EditText) findViewById(R.id.Login_Email);
        password = (EditText) findViewById(R.id.Login_Password);
        Register = (Button) findViewById(R.id.btnLogRegister);
        Login = (Button) findViewById(R.id.btnLogLogin);
        forgot_password = (TextView) findViewById(R.id.forgot_password);
        outlook_icon = (ImageView) findViewById(R.id.outlook_icon);
        gmail_icon = (ImageView) findViewById(R.id.gmail_icon);
        yahoo_icon = (ImageView) findViewById(R.id.yahoo_icon);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(""+dialogLogMessage);
    }


    private  boolean validate()
    {
        Resources resources = getResources();
        String [] message = resources.getStringArray(R.array.register_validation_message);

        Boolean result = false;

        EmailAddress = email.getText().toString();
        Password = password.getText().toString();

        if(EmailAddress.isEmpty())
        {
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this,""+message[1],Toast.LENGTH_LONG).show();
        }

        else if(Password.isEmpty())
        {
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this,""+message[3],Toast.LENGTH_LONG).show();
        }

        else
        {
            result = true;
        }

        return result;
    }
}
