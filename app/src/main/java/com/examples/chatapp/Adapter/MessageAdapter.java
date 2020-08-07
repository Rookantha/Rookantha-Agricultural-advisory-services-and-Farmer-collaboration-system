package com.examples.chatapp.Adapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.examples.chatapp.Model.Chat;
import com.examples.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>
{
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;
    private FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl)
    {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        if(viewType == MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,viewGroup,false);
            return new MessageAdapter.ViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,viewGroup,false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position)
    {
        Chat chat = mChat.get(position);


        if(chat.getMessage().equals("This message was deleted"))
        {
            viewHolder.Show_Message.setTextColor(mContext.getColor(R.color.gray));
        }

        if(chat.getPicture_URL().equals("default"))
        {
            viewHolder.Show_Message.setText(chat.getMessage());
            viewHolder.Show_Picture_Message.setVisibility(View.GONE);
        }
        else
        {
            viewHolder.Show_Message.setVisibility(View.GONE);
            Glide.with(mContext).load(chat.getPicture_URL()).into(viewHolder.Show_Picture_Message);
        }


        if(imageurl.equals("default"))
        {
            viewHolder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else
        {
            Glide.with(mContext).load(imageurl).into(viewHolder.profile_image);
        }



        final String message = "This message was deleted";
        String check_is_Deleted = mChat.get(position).getMessage();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = databaseReference.orderByChild("message").equalTo(check_is_Deleted);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if (!ds.child("message").getValue().equals(message))
                    {
                        viewHolder.Show_Message.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setIcon(R.drawable.delete);
                                builder.setTitle("" + viewHolder.dial_Title);
                                builder.setMessage("" + viewHolder.dial_Message);
                                builder.setPositiveButton("" + viewHolder.dial_Yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteMessage(position, viewHolder.dial_not_user);
                                    }
                                });
                                builder.setNegativeButton("" + viewHolder.dial_No, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.create().show();
                                return false;
                            }
                        });

                        viewHolder.Show_Picture_Message.setOnLongClickListener(new View.OnLongClickListener()
                        {
                            @Override
                            public boolean onLongClick(View v)
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setIcon(R.drawable.delete);
                                builder.setTitle("" + viewHolder.dial_Title);
                                builder.setMessage("" + viewHolder.dial_Message);
                                builder.setPositiveButton("" + viewHolder.dial_Yes, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        deletePictureMessage(position, viewHolder.dial_not_user);
                                    }
                                });
                                builder.setNegativeButton("" + viewHolder.dial_No, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                    }
                                });
                                builder.create().show();
                                return false;
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        if(position == mChat.size()-1)
        {
            if(chat.isIsseen())
            {
                viewHolder.txt_seen.setImageResource(R.drawable.seen);
            }
            else
            {
                viewHolder.txt_seen.setImageResource(R.drawable.delivery);
            }
        }
        else
        {
            viewHolder.txt_seen.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(final int position, final String toast)
    {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String message = mChat.get(position).getMessage();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        Query query_message = databaseReference.orderByChild("message").equalTo(message);
        query_message.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if (ds.child("sender").getValue().equals(myUID))
                    {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted");
                        ds.getRef().updateChildren(hashMap);
                    }
                    else
                    {
                        Toast.makeText(mContext, "" + toast, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deletePictureMessage(final int position, final String toast)
    {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String image_URL = mChat.get(position).getPicture_URL();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        Query query_picture = databaseReference.orderByChild("picture_URL").equalTo(image_URL);
        query_picture.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if (ds.child("sender").getValue().equals(myUID))
                    {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted");
                        hashMap.put("picture_URL", "default");
                        ds.getRef().updateChildren(hashMap);
                    }
                    else
                    {
                        Toast.makeText(mContext, "" + toast, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView Show_Message;
        public ImageView profile_image;
        public ImageView txt_seen;
        public ImageView Show_Picture_Message;
        String dial_Title,dial_Message,dial_Yes,dial_No,dial_not_user;

        public ViewHolder(View itemView)
        {
            super(itemView);
            Show_Message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            Show_Picture_Message = itemView.findViewById(R.id.pic_Message);
            dial_Title = itemView.getContext().getString(R.string.dial_Title);
            dial_Yes = itemView.getContext().getString(R.string.dialogDelete);
            dial_No = itemView.getContext().getString(R.string.negative);
            dial_Message = itemView.getContext().getString(R.string.delete_message);
            dial_not_user = itemView.getContext().getString(R.string.not_user);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(firebaseUser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }
    }


}