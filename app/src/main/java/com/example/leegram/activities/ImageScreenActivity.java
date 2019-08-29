package com.example.leegram.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import com.example.leegram.others.CommunicateWithRealm;
import com.example.leegram.R;

// TODO: 29/08/2019 remove - its crazy to hold an activity where what should have been added is an iamgeview to the xml
public class ImageScreenActivity extends Activity {
    private ImageView imageView;
    private CommunicateWithRealm communicateWithRealm;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_screen);
        communicateWithRealm = new CommunicateWithRealm();
        byte[] picture = communicateWithRealm.getPhoto(getIntent().getStringExtra("image"));
        imageView = findViewById(R.id.image_container);
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(picture, 0, picture.length));
        imageView.setOnClickListener(v -> {
            finish();
           this.overridePendingTransition(R.anim.exit_animation,R.anim.exit_animation);
        });
    }
}
