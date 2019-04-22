package com.oguzhanozer.mychatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import  android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.support.v4.app.ActivityCompat;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oguzhanozer.mychatapplication.Notification.Client;
import com.oguzhanozer.mychatapplication.Notification.Data;
import com.oguzhanozer.mychatapplication.Notification.MyResponse;
import com.oguzhanozer.mychatapplication.Notification.Sender;
import com.oguzhanozer.mychatapplication.Notification.Token;
import com.oguzhanozer.mychatapplication.fragment.APIService;

import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    StorageReference storageReference;

    ImageButton buttonSender;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mChats;

    RecyclerView recyclerView;
    Uri selectedUri;
    String userId;

    APIService apıService;

    boolean notify = false;

    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apıService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image_message);
        username = findViewById(R.id.username_message);
        buttonSender = findViewById(R.id.btn_send);
        text_send = (EditText) findViewById(R.id.text_send);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userid");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        buttonSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = text_send.getText().toString();
                if(!message.equals("")){

                    SendMessage(firebaseUser.getUid(),userId,message,"text");
                    text_send.setText("");

                }else
                    Toast.makeText(MessageActivity.this, "You can!t send ", Toast.LENGTH_SHORT).show();
                }



        });

        storageReference = FirebaseStorage.getInstance().getReference();

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String,String> hashMap  = (HashMap<String, String>) dataSnapshot.getValue();
                String email = hashMap.get("username");
                String img = hashMap.get("imageURL");
                String id = hashMap.get("id");
                String status = hashMap.get("status");

                User user = new User(id,email,img,status,email);
                username.setText(user.getUsername());

                if(user.getImageurl().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(MessageActivity.this).load(user.getImageurl()).into(profile_image);
                }

                readMessage(firebaseUser.getUid(),userId,user.getImageurl());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userId);

    }


    public void selectImage(View view){

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,2);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,2);

            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

         if(requestCode==2 && resultCode == RESULT_OK && data!=null){

             selectedUri = data.getData();

             try {

                 Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedUri);
                 Upload();

             } catch (IOException e) {
                 e.printStackTrace();
             }

         }

    }

    public void Upload(){

        final  UUID uuidImage = UUID.randomUUID();
        final  String imageName = "images/" + uuidImage + ".jpg";

        StorageReference newReference = storageReference.child(imageName);
        
        newReference.putFile(selectedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("images/" + uuidImage + ".jpg");
                profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        String downloadUrl = uri.toString();
                    //    System.out.println("downloadurl " + downloadUrl);

                        SendMessage(firebaseUser.getUid(),userId,downloadUrl,"imageMessage");


                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MessageActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }


    private  void seenMessage(final String userId){

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren() ){

                    HashMap<String,String> hashMap  = (HashMap<String, String>) ds.getValue();
                    String message = hashMap.get("message");
                    String receiver = hashMap.get("receiver");
                    String sender = hashMap.get("sender");
                    String type = hashMap.get("type");
                    Object isseen = hashMap.get("isseen");

                    Chat chat = new Chat(sender,receiver,message,type,(boolean)isseen);

                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)){

                         HashMap<String,Object> hashMap1 =  new HashMap<String,Object>();
                         hashMap1.put("isseen",true);
                         ds.getRef().updateChildren(hashMap1);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void SendMessage(String sender, final String receiver, String message, String type){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> hashMap = new HashMap<String,Object>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);
        hashMap.put("type",type);

        reference.child("Chats").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid()).child(userId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userId);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String,String> hashMap  = (HashMap<String, String>) dataSnapshot.getValue();
                String email = hashMap.get("username");
                String img = hashMap.get("imageURL");
                String id = hashMap.get("id");
                String status = hashMap.get("status");

                User user = new User(id,email,img,status,email);

                if(notify){
                    sendNotification(receiver,user.getUsername(),msg);
                }

                notify=false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void sendNotification(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    HashMap<String,String> hashMap  = (HashMap<String, String>)ds.getValue();
                    String tokens = hashMap.get("token");

                    Token token = new Token(tokens);
                    Data data = new Data(firebaseUser.getUid(),R.mipmap.ic_launcher,username+":"+message,"New Message",userId);

                    Sender sender = new Sender(data,token.getToken());

                    apıService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            if(response.code()==200){

                                if(response.body().success == 1){
                                    Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void readMessage(final String myid,final String userId,final String imageurl){
        mChats = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChats.clear();

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    HashMap<String,String> hashMap  = (HashMap<String, String>) ds.getValue();
                    String message = hashMap.get("message");
                    String receiver = hashMap.get("receiver");
                    String sender = hashMap.get("sender");
                    String type  = hashMap.get("type");
                    Object isseen = hashMap.get("isseen");

                    Chat chat = new Chat(sender,receiver,message,type,(boolean)isseen);

                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(myid)){
                        mChats.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this,mChats,imageurl);
                    recyclerView.setAdapter(messageAdapter);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void currentUser(String userId){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser",userId);
        editor.apply();

    }


    private void status(String status){

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();

        status("online");

        currentUser(userId);

    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");

        currentUser("none");

    }



}
