package com.examples.chatapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.examples.chatapp.Fragments.ChatsFragment;
import com.examples.chatapp.Fragments.ProfileFragment;
import com.examples.chatapp.Fragments.UsersFragment;
import com.examples.chatapp.InternetCheck.CheckInternetConnection;
import com.examples.chatapp.Model.Chat;
import com.examples.chatapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{

    private CircleImageView profile_image;
    private TextView username;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final String chat = getString(R.string.chating);
        final String user = getString(R.string.Users);
        final String profile = getString(R.string.Profile);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        Initiate();
        Display();
        loadLocale();


        mAuth = FirebaseAuth.getInstance();

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);


        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unread = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Chat chat1 = snapshot.getValue(Chat.class);
                    if(chat1.getReceiver().equals(firebaseUser.getUid())&& !chat1.isIsseen())
                    {
                        unread++;
                    }
                }
                if(unread == 0)
                {
                    viewPagerAdapter.addFragment(new ChatsFragment(),""+chat);
                }
                else
                {
                    viewPagerAdapter.addFragment(new ChatsFragment(),""+chat+" ("+unread+")");
                }

                viewPagerAdapter.addFragment(new UsersFragment(),""+user);
                viewPagerAdapter.addFragment(new ProfileFragment(),""+profile);
                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });



    }

    private void showChangeLanguageDialog()
    {
        String [] listItems = {"English","සිංහල","தமிழ்"};

        android.app.AlertDialog.Builder mBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
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
        android.app.AlertDialog mDialog = mBuilder.create();
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
        profile_image = (CircleImageView) findViewById(R.id.profile_image);
        username = (TextView) findViewById(R.id.username);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
    }

    private void Display()
    {
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getImageURL().equals("default"))
                {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }
                else
                {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        CheckInternetConnection connection = new CheckInternetConnection();
        String dialogTitle = getString(R.string.title_signout_dialog);
        String dialogBody = getString(R.string.body_signout_dialog);
        String positive = getString(R.string.positive);
        String negative = getString(R.string.negative);
        String noInternet = getString(R.string.internetFail);

        switch (item.getItemId())
        {
            case R.id.logout:
            {
                if(connection.NetworkState(getApplicationContext()))
                {
                    try
                    {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setCancelable(false);
                        dialog.setIcon(R.drawable.warning);
                        dialog.setTitle(""+dialogTitle);
                        dialog.setMessage(""+dialogBody );
                        dialog.setNegativeButton(""+negative, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //Cancel
                            }
                        }).setPositiveButton(""+positive, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                try
                                {
                                    Intent intent = new Intent(MainActivity.this, StartActivity.class);
                                    intent.putExtra("finish", true); // if you are checking for this in your other Activities
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                    FirebaseAuth.getInstance().signOut();
                                }
                                catch (Exception e)
                                {
                                    Toast.makeText(MainActivity.this,""+e,Toast.LENGTH_LONG).show();
                                }

                            }
                        });
                        final AlertDialog alert = dialog.create();
                        alert.show();
                    }
                    catch(Exception ex)
                    {
                        Log.e("message",ex.getMessage());
                    }
                    return true;
                }
                else
                {
                    Toast.makeText(MainActivity.this,""+noInternet,Toast.LENGTH_LONG).show();
                }
                break;
            }

            case R.id.refresh:
            {
                break;
            }
            case R.id.language:
            {
                try
                {
                    showChangeLanguageDialog();
                }
                catch (Exception e)
                {
                    Log.d("",e.toString());
                }
                break;
            }
        }
        return false;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm)
        {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position)
        {
            return fragments.get(position);
        }

        @Override
        public int getCount()
        {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title)
        {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position)
        {
            return titles.get(position);
        }
    }

    private void status(String status)
    {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        status("offline");
    }
}
