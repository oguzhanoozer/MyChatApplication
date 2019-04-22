package com.oguzhanozer.mychatapplication.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.oguzhanozer.mychatapplication.Chat;
import com.oguzhanozer.mychatapplication.ChatList;
import com.oguzhanozer.mychatapplication.Notification.MyFirebaseIdService;
import com.oguzhanozer.mychatapplication.Notification.Token;
import com.oguzhanozer.mychatapplication.R;
import com.oguzhanozer.mychatapplication.User;
import com.oguzhanozer.mychatapplication.UserAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;

    private  UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser fUser;
    DatabaseReference reference;

    private  List<ChatList> usersList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_chats);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                usersList.clear();

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    HashMap<String,String> hashMap = (HashMap<String, String>)ds.getValue();
                    String idChat = hashMap.get("id");

                    ChatList chatList = new ChatList(idChat);
                    usersList.add(chatList);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

            updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;

    }

    public void updateToken(String token){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token tokenI = new Token(token);
        reference.child(fUser.getUid()).setValue(tokenI);

    }



    public void chatList(){

        mUsers = new ArrayList<>();
        reference =  FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUsers.clear();

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    HashMap<String,String> hashMap  = (HashMap<String, String>) ds.getValue();
                    String email = hashMap.get("username");
                    String img = hashMap.get("imageURL");
                    String ids = hashMap.get("id");
                    String status = hashMap.get("status");

                    User user = new User(ids,email,img,status,email);

                    for(ChatList chatList : usersList){

                        if(user.getId().equals(chatList.getId())){
                            mUsers.add(user);
                        }

                    }
                }

                userAdapter = new UserAdapter(getContext(),mUsers,true);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




}
