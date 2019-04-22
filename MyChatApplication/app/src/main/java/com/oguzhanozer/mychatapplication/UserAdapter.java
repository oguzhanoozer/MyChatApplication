package com.oguzhanozer.mychatapplication;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;

    String theLastMsg;

    private boolean isChat;

    public UserAdapter(Context mContext,List<User> mUsers,boolean isChat){
        this.mUsers = mUsers;
        this.mContext=mContext;
        this.isChat=isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,viewGroup,false);
        return new UserAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final User user = mUsers.get(i);
        viewHolder.username.setText(user.getUsername());

        if(user.getImageurl().equals("default")){
            viewHolder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(mContext).load(user.getImageurl()).into(viewHolder.profile_image);
        }


        if(isChat){
            lastMessage(user.getId(),viewHolder.last_message);

        }else{
            viewHolder.last_message.setVisibility(View.GONE);
        }


        if(isChat){
            if(user.getStatus().equals("online")){
                viewHolder.img_on.setVisibility(View.VISIBLE);
                viewHolder.img_off.setVisibility(View.GONE);

            }else{
                viewHolder.img_off.setVisibility(View.VISIBLE);
                viewHolder.img_on.setVisibility(View.GONE);
            }
        }else{
            viewHolder.img_on.setVisibility(View.GONE);
            viewHolder.img_off.setVisibility(View.GONE);

        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        public ImageView img_on,img_off;
        public TextView last_message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_off = itemView.findViewById(R.id.img_off);
            img_on = itemView.findViewById(R.id.img_on);
            last_message = itemView.findViewById(R.id.last_msg);

        }
    }

    private void lastMessage(final String userid, final TextView last_msg){

        theLastMsg = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    HashMap<String,String> hashMap  = (HashMap<String, String>) ds.getValue();
                    String message = hashMap.get("message");
                    String receiver = hashMap.get("receiver");
                    String sender = hashMap.get("sender");
                    String type = hashMap.get("type");
                    Object isseen = hashMap.get("isseen");

                    Chat chat = new Chat(sender,receiver,message,type,(boolean)isseen);

                    if (firebaseUser != null && chat != null) {

                        if(chat.getReceiver().equals(firebaseUser.getUid()) &&  chat.getSender().equals(userid)
                                ||  chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid()))
                        {

                            if(chat.getType().equals("text")){
                                theLastMsg  = chat.getMessage();
                            }else{
                                theLastMsg="an image message";
                            }
                        }
                    }
                }

                switch (theLastMsg){
                    case "default":
                        last_msg.setText("No Message");
                        break;
                    default:
                        last_msg.setText(theLastMsg);
                        break;
                }
                theLastMsg = "default";
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



}
