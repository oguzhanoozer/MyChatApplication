package com.oguzhanozer.mychatapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

public class PopUpDisplayImage extends Activity {

    public ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_display_image);

        imageView = findViewById(R.id.popupimageView);

        Intent intent = getIntent();
        String url =  intent.getStringExtra("url");

        Picasso.get().load(url).into(imageView);
        //Glide.with(getApplicationContext()).load(url).into(iImageView);



}

}
