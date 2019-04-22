package com.oguzhanozer.mychatapplication.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.oguzhanozer.mychatapplication.MainActivity;
import com.oguzhanozer.mychatapplication.R;
import com.oguzhanozer.mychatapplication.User;
import com.oguzhanozer.mychatapplication.UserAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserFragment extends Fragment {

    private RecyclerView recyclerView;

    private EditText search_user;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user,container,false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();

        readUser();

        search_user = view.findViewById(R.id.search_users);
        search_user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                searchUser(s.toString().toLowerCase());

            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;

    }



    private void searchUser(String s) {

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search").startAt(s).endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
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

                    assert  user!=null;
                    assert fuser!=null;

                    if(!user.getId().equals(fuser.getUid())){

                        mUsers.add(user);

                    }
                }

                userAdapter = new UserAdapter(getContext(),mUsers,false);
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void readUser(){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(search_user.getText().toString().equals("")){

                    mUsers.clear();

                    for(DataSnapshot ds : dataSnapshot.getChildren()){

                        HashMap<String,String> hashMap  = (HashMap<String, String>) ds.getValue();
                        String email = hashMap.get("username");
                        String img = hashMap.get("imageURL");
                        String id = hashMap.get("id");
                        String status = hashMap.get("status");

                        User user = new User(id,email,img,status,email);

                        if(!user.getId().equals(firebaseUser.getUid())){
                            mUsers.add(user);
                        }
                    }

                    userAdapter = new UserAdapter(getContext(),mUsers,false);
                    recyclerView.setAdapter(userAdapter);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
