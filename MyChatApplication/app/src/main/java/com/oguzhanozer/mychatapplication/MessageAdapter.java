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
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChats;
    private String imageUrl;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext,List<Chat> mChats,String imageUrl){
        this.mChats = mChats;
        this.mContext=mContext;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        if(i == MSG_TYPE_RIGHT){

            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,viewGroup,false);
            return new MessageAdapter.ViewHolder(view);
        }else{

            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,viewGroup,false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder viewHolder, int i) {

       Chat chat = mChats.get(i);


        if(chat.getType().equals("imageMessage")){

            viewHolder.message_show.setVisibility(View.INVISIBLE);
            viewHolder.imagemessage.setVisibility(View.VISIBLE);
            //viewHolder.imagemessage.setImageResource(R.mipmap.ic_launcher);
         //   Glide.with(mContext).load(chat.getMessage()).into(viewHolder.imagemessage);


            Picasso.get().load(chat.getMessage()).into(viewHolder.imagemessage);

            final String url = chat.getMessage();


            viewHolder.imagemessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("amigo");

                    Intent intent = new Intent(mContext,PopUpDisplayImage.class);
                    intent.putExtra("url", url);
                    mContext.startActivity(intent);


                }
            });


        }else{
            viewHolder.message_show.setText(chat.getMessage());
        }


        if(imageUrl.equals("default")){
            viewHolder.profile_image.setImageResource(R.mipmap.ic_launcher);

        }else{
            Glide.with(mContext).load(imageUrl).into(viewHolder.profile_image);
        }

        if(i==mChats.size()-1){
            if(chat.getIsseen()){
                viewHolder.textSeen.setText("Seen");
            }else{
                viewHolder.textSeen.setText("Delivered");
            }
        }else{
            viewHolder.textSeen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView message_show;
        public ImageView profile_image;
        public ImageView imagemessage;
        public TextView textSeen;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemessage = itemView.findViewById(R.id.imagemessage);
            message_show = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image_chat);
            textSeen = itemView.findViewById(R.id.txt_seen);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(mChats.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return  MSG_TYPE_LEFT;
        }

    }
}


