package com.examples.chatapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.examples.chatapp.Adapter.MessageAdapter;
import com.examples.chatapp.Fragments.APIService;
import com.examples.chatapp.InternetCheck.CheckInternetConnection;
import com.examples.chatapp.Model.Chat;
import com.examples.chatapp.Model.User;
import com.examples.chatapp.Notifications.Client;
import com.examples.chatapp.Notifications.Data;
import com.examples.chatapp.Notifications.MyResponse;
import com.examples.chatapp.Notifications.Sender;
import com.examples.chatapp.Notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MessageActivity extends AppCompatActivity
{
    Intent intent;
    ImageButton Send, Attach;
    TextView username, Audio_Call, Video_Call;
    CircleImageView Image;
    StorageTask uploadTask;

    private CircleImageView profile_image;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private EditText Message;
    private MessageAdapter messageAdapter;
    private List<Chat> mChat;
    private RecyclerView recyclerView;
    private ValueEventListener seenListner;
    private boolean notify = false;
    private String userid;
    private Dialog dialog;
    private APIService apiService;
    private String Contact_Number;
    private static final int IMAGE_REQUEST = 1;
    private static final int PERMISSION_CODE = 1001;
    private StorageReference storageReference;
    private ImageView Selected_Image,Send_Image,Cancel_Image;
    private View buttonPanel;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        String GoBack = getString(R.string.back);

        final CheckInternetConnection connection = new CheckInternetConnection();


            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(""+GoBack);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        dialog = new Dialog(this);
        profile_image = findViewById(R.id.profile_image);
        Audio_Call = (TextView) findViewById(R.id.btn_audio_call);
        Video_Call = (TextView) findViewById(R.id.btn_video_call);
        username = findViewById(R.id.username);
        Send = (ImageButton) findViewById(R.id.btn_send);
        Attach = (ImageButton) findViewById(R.id.btn_attach);
        Message = (EditText) findViewById(R.id.text_send);
        intent = getIntent();
        userid = intent.getStringExtra("userid");


        storageReference = FirebaseStorage.getInstance().getReference("Pictures_Message");


        Attach.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ShowPopupDialog();
            }
        });

        Send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(connection.NetworkState(getApplicationContext()))
                {
                    notify = true;
                    String msg = Message.getText().toString();
                    String empty_Message = getString(R.string.empty_send);
                    if (!msg.equals(""))
                    {
                        SendMessage(firebaseUser.getUid(), userid, msg);
                    }
                    else
                    {
                        Toast.makeText(MessageActivity.this, ""+empty_Message, Toast.LENGTH_LONG).show();
                    }
                    Message.setText("");
                }
            }
        });

        Audio_Call.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED)
                {
                    String [] permisions = {Manifest.permission.CALL_PHONE};
                    requestPermissions(permisions,PERMISSION_CODE);
                }
                else
                {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:"+Contact_Number));
                    startActivity(intent);
                }
            }
        });

        Video_Call.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED)
                {
                    String [] permisions = {Manifest.permission.CALL_PHONE};
                    requestPermissions(permisions,PERMISSION_CODE);
                }
                else
                {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:"+Contact_Number));
                    startActivity(intent);
                }
            }
        });
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
            reference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    Contact_Number = user.getMobile();
                    if(user.getImageURL().equals("default"))
                    {
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    }
                    else
                    {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                    }

                    ReadMessage(firebaseUser.getUid(),userid,user.getImageURL());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
            seenMessage(userid);
    }

    public void ShowPopupDialog()
    {
        dialog.setContentView(R.layout.custompopup);
        Image = (CircleImageView)dialog.findViewById(R.id.btn_image);

        Selected_Image = dialog.findViewById(R.id.pic_Selected);
        Send_Image = dialog.findViewById(R.id.send_Image);
        Cancel_Image = dialog.findViewById(R.id.cancel_Image);
        buttonPanel = dialog.findViewById(R.id.buttonPanel);

        Image.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Selected_Image.setVisibility(View.VISIBLE);
                Send_Image.setVisibility(View.VISIBLE);
                Cancel_Image.setVisibility(View.VISIBLE);
                buttonPanel.setVisibility(View.GONE);

                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                {
                    String [] permisions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permisions,PERMISSION_CODE);
                }
                else
                {
                    openGalleryImage();
                }
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void openGalleryImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
           imageUri = data.getData();
            try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                Selected_Image.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            Send_Image.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        uploadImage();
                    }
                });

                Cancel_Image.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Selected_Image.setImageBitmap(null);
                        buttonPanel.setVisibility(View.VISIBLE);
                        Selected_Image.setVisibility(View.GONE);
                        Send_Image.setVisibility(View.GONE);
                        Cancel_Image.setVisibility(View.GONE);
                    }
                });
        }
    }

    private void uploadImage()
    {
        String NoImageMessage = getString(R.string.NoImage);
        String Uploading = getString(R.string.image_uploading);

        final ProgressDialog progressDialog = new ProgressDialog(MessageActivity.this);
        progressDialog.setMessage(""+Uploading);
        progressDialog.show();
        if(imageUri != null)
        {
            final StorageReference fileReference = storageReference.child(firebaseUser.getUid()+"_"+ UUID.randomUUID());

            uploadTask = fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                            {
                                @Override
                                public void onSuccess(Uri uri)
                                {
                                    Uri down = uri;
                                    String ImageURL = down.toString().trim();
                                    SendImageMessage(firebaseUser.getUid(),userid,ImageURL);
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(MessageActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });
        }
        else
        {
            Toast.makeText(MessageActivity.this,""+NoImageMessage,Toast.LENGTH_LONG).show();
        }
    }

    private void SendImageMessage(String sender, final String receiver, String URL)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> messageMap = new HashMap<>();
        messageMap.put("sender",sender);
        messageMap.put("receiver",receiver);
        messageMap.put("message","picture");
        messageMap.put("picture_URL",URL);
        messageMap.put("isseen",false);

        reference.child("Chats").push().setValue(messageMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(receiver);

        final DatabaseReference reserverRef = FirebaseDatabase.getInstance()
                .getReference("Chatlist")
                .child(receiver)
                .child(firebaseUser.getUid());

        reserverRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.exists())
                {
                    reserverRef.child("id").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


        chatRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.exists())
                {
                    chatRef.child("id").setValue(receiver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                User user = dataSnapshot.getValue(User.class);
                if(notify)
                {
                    sendNotification(receiver,user.getUsername(),"Image");
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void SendMessage(String sender, final String receiver, String message)
    {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> messageMap = new HashMap<>();
        messageMap.put("sender",sender);
        messageMap.put("receiver",receiver);
        messageMap.put("message",message);
        messageMap.put("picture_URL","default");
        messageMap.put("isseen",false);

        reference.child("Chats").push().setValue(messageMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(receiver);

        final DatabaseReference reserverRef = FirebaseDatabase.getInstance()
                .getReference("Chatlist")
                .child(receiver)
                .child(firebaseUser.getUid());

        reserverRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.exists())
                {
                    reserverRef.child("id").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


        chatRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.exists())
                {
                    chatRef.child("id").setValue(receiver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                User user = dataSnapshot.getValue(User.class);
                if(notify)
                {
                    sendNotification(receiver,user.getUsername(),msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void ReadMessage(final String myid, final String userid, final String imageurl)
    {
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                mChat.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid))
                    {
                        mChat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this,mChat,imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void seenMessage(final String userid)
    {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListner = reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid))
                    {
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void sendNotification(final String receiver, final String username, final String message)
    {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(),R.mipmap.app_launcher,
                            username+": "+message,"New Message",receiver);

                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>()
                            {
                                @Override
                                public void onResponse(Response<MyResponse> response, Retrofit retrofit)
                                {
                                    if(response.code() == 200)
                                    {
                                        if(response.body().success != 1)
                                        {
                                            Toast.makeText(MessageActivity.this,"Failed",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t)
                                {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void currentUser(String ID_USER)
    {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser", ID_USER).apply();
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
        currentUser(userid);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        reference.removeEventListener(seenListner);
        status("offline");
        currentUser("");
    }

}
