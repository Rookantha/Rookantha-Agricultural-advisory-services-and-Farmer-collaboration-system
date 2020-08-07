package com.examples.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.examples.chatapp.MessageActivity;
import com.examples.chatapp.Model.Chat;
import com.examples.chatapp.Model.User;
import com.examples.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>
{
    private Context mContext;
    private List<User> mUser;
    private boolean isChat;
    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUser,boolean isChat)
    {
        this.mContext = mContext;
        this.mUser = mUser;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,viewGroup,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position)
    {
        final User user = mUser.get(position);
        viewHolder.username.setText(user.getUsername());
        if(user.getImageURL().equals("default"))
        {
            viewHolder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else
        {
            Glide.with(mContext).load(user.getImageURL()).into(viewHolder.profile_image);
        }

        if(isChat)
        {
            lastMessage(user.getId(),viewHolder.Last_MSG);
            chatcounter(user.getId(),viewHolder.Count);
        }
        else
        {
            viewHolder.Last_MSG.setVisibility(View.GONE);
            viewHolder.Count.setVisibility(View.GONE);
        }


        if(isChat)
        {
            if(user.getStatus().equals("online"))
            {
                viewHolder.img_on.setVisibility(View.VISIBLE);
                viewHolder.img_off.setVisibility(View.GONE);
            }
            else
            {
                viewHolder.img_on.setVisibility(View.GONE);
                viewHolder.img_off.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            viewHolder.img_on.setVisibility(View.GONE);
            viewHolder.img_off.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView username;
        public ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView Last_MSG;
        private TextView Count;


        public ViewHolder(View itemView)
        {
            super(itemView);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            Last_MSG = itemView.findViewById(R.id.last_msg);
            Count = itemView.findViewById(R.id.count_chats);
        }
    }

    private void chatcounter(final String userid, final TextView ChatDisplay)
    {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                int unread = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) && !chat.isIsseen())
                    {
                        unread++;
                    }
                }
                if(unread == 0)
                {
                    ChatDisplay.setVisibility(View.GONE);
                }
                else
                {
                    ChatDisplay.setVisibility(View.VISIBLE);
                    ChatDisplay.setText(""+unread);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void lastMessage(final String userid, final TextView Last_MSG)
    {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid())
                    && chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())
                    )
                    {
                        theLastMessage = chat.getMessage();
                    }
                }
                switch (theLastMessage)
                {
                    case "default":
                        Last_MSG.setText("No Message");
                        break;
                    default:
                        Last_MSG.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
