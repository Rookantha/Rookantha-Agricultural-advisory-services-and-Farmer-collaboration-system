package com.examples.chatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SnackbarContentLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.examples.chatapp.InternetCheck.CheckInternetConnection;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class StartActivity extends AppCompatActivity
{

    private Button Register,Login,Exit,Language;
    private FirebaseUser firebaseUser;

    @Override
    protected void onStart()
    {
        String InternetMessage = getString(R.string.no_internet_big_message);
        super.onStart();
        loadLocale();
        CheckInternetConnection checkInternetConnection = new CheckInternetConnection();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if(checkInternetConnection.NetworkState(getApplicationContext()))
            {
                if(firebaseUser!=null)
                {
                    Intent intent = new Intent(StartActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            else
            {
                Toast.makeText(StartActivity.this,""+InternetMessage,Toast.LENGTH_LONG).show();
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_start);
        Initiate();

        Login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(StartActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        Register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        Exit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
                moveTaskToBack(true);
                System.exit(0);
            }
        });

        Language.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showChangeLanguageDialog();
            }
        });

    }

    private void showChangeLanguageDialog()
    {
        String [] listItems = {"English","සිංහල","தமிழ்"};

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(StartActivity.this);
        mBuilder.setTitle("Choose Language....");
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(which==0)
                {
                    setLocale("en");
                    recreate();
                }
                if(which==1)
                {
                    setLocale("si");
                    recreate();
                }
                if(which==2)
                {
                    setLocale("ta");
                    recreate();
                }
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void setLocale(String lang)
    {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("My_Lang",lang);
        editor.apply();
    }

    public void loadLocale()
    {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang","");
        setLocale(language);
    }

    private void Initiate()
    {
        Register = (Button) findViewById(R.id.Start_Register);
        Login = (Button) findViewById(R.id.Start_Login);
        Exit = (Button) findViewById(R.id.Start_Exit);
        Language = (Button) findViewById(R.id.Start_Language);
    }
}
