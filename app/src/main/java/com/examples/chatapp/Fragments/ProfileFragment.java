package com.examples.chatapp.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.examples.chatapp.ChangePasswordActivity;
import com.examples.chatapp.InternetCheck.CheckInternetConnection;
import com.examples.chatapp.Model.User;
import com.examples.chatapp.R;
import com.examples.chatapp.StartActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment
{
    private static final String TAG = ProfileFragment.class.getSimpleName();
    private CircleImageView image_profile;
    private TextView email;
    private EditText username,mobile;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    StorageReference storageReference;
    Button Update,ChangePassword,Delete;
    private static final int IMAGE_REQUEST = 1;


    private Uri imageUri;
    private StorageTask uploadTask;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        mobile = view.findViewById(R.id.mobile);
        email = view.findViewById(R.id.email);

        progressDialog = new ProgressDialog(getContext());

        Update = view.findViewById(R.id.btn_profile_edit);
        ChangePassword = view.findViewById(R.id.btn_profile_change);
        Delete = view.findViewById(R.id.btn_profile_delete);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        final String NoInternet = getString(R.string.internetFail);

        final CheckInternetConnection connection = new CheckInternetConnection();

        storageReference = FirebaseStorage.getInstance().getReference("Profile_Pics");
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getUsername());
                mobile.setText(user.getMobile());
                email.setText(user.getEmail());

                if(user.getImageURL().equals("default"))
                {
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                }
                else
                {
                    Glide.with(getContext()).load(user.getImageURL()).into(image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        image_profile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(connection.NetworkState(getContext()))
                {
                    openImage();
                }
                else
                {
                    Toast.makeText(getContext(),""+NoInternet,Toast.LENGTH_LONG).show();
                }
            }
        });


        Update.setOnClickListener(new View.OnClickListener()
        {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            @Override
            public void onClick(View v)
            {
                String UpdateProgress = getString(R.string.update_progress);
                final String UpdateSuccess = getString(R.string.update_success);

                if(connection.NetworkState(getContext()))
                {
                    String Username = username.getText().toString().trim();
                    String Mobile = mobile.getText().toString().trim();
                    progressDialog.setMessage(""+UpdateProgress);
                    progressDialog.show();
                    HashMap <String,Object> hashMap = new HashMap<>();
                    hashMap.put("username",Username);
                    hashMap.put("search",Username.toLowerCase());
                    hashMap.put("mobile",Mobile);
                    databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(),""+UpdateSuccess,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(getContext(),""+NoInternet,Toast.LENGTH_LONG).show();
                }
            }
        });

        ChangePassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getContext(), ChangePasswordActivity.class));
            }
        });

        Delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    DeleteUsers();
                }
                catch (Exception ex)
                {
                    Toast.makeText(getContext(),""+ex,Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }


    private void DeleteUsers()
    {

        final String InternetNot = getString(R.string.internetFail);
        CheckInternetConnection internetConnection = new CheckInternetConnection();
        String dialogTitle = getString(R.string.dial_Title);
        String dialogBody = getString(R.string.dialogBody);
        String dialogDelete = getString(R.string.dialogDelete);
        String dialogCancel = getString(R.string.dialogCancel);
        final String dialogDeleteSuccess = getString(R.string.dialogDeleteSuccess);
        final String dialogDeleteFail = getString(R.string.dialogDeleteFail);

        if(internetConnection.NetworkState(getContext()))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
            builder.setIcon(R.drawable.delete);
            builder.setTitle(""+dialogTitle);
            builder.setMessage(""+dialogBody);
            builder.setPositiveButton(""+dialogDelete, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    if(firebaseUser!=null)
                    {
                        firebaseUser.delete().addOnSuccessListener(new OnSuccessListener<Void>()
                        {
                            @Override
                            public void onSuccess(Void aVoid)
                            {
                                try
                                {
                                    Toast.makeText(getContext(), ""+dialogDeleteSuccess, Toast.LENGTH_LONG).show();
                                    Objects.requireNonNull(getActivity()).finish();
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(getActivity(), StartActivity.class));
                                }
                                catch (Exception ex)
                                {
                                    Toast.makeText(getContext(), "" + ex.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.e(TAG,""+ex.getMessage());
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(getContext(),""+dialogDeleteFail,Toast.LENGTH_LONG).show();
                            }
                        });
                    }


                    }
                })
                    .setNegativeButton(""+dialogCancel, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        }
        else
        {
            Toast.makeText(getContext(),""+InternetNot,Toast.LENGTH_LONG).show();
        }
    }

    private void openImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }
    private void uploadImage()
    {
        String NoImageMessage = getString(R.string.NoImage);
        String Uploading = getString(R.string.uploading);

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(""+Uploading);
        progressDialog.show();
        if(imageUri != null)
        {
            final StorageReference fileReference = storageReference.child(firebaseUser.getUid());

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
                            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("imageURL",ImageURL);
                            reference.updateChildren(map);
                            progressDialog.dismiss();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            });
        }
        else
        {
            Toast.makeText(getContext(),""+NoImageMessage,Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        String Upload = getString(R.string.upload_progress);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
        && data != null && data.getData() != null)
        {
            imageUri = data.getData();
            if(uploadTask != null && uploadTask.isInProgress())
            {
                Toast.makeText(getContext(),""+Upload,Toast.LENGTH_LONG).show();
            }
            else
            {
                uploadImage();
            }
        }
    }
}
