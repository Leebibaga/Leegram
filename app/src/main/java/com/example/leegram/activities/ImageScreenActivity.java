package com.example.leegram.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.widget.ImageView;
import com.example.leegram.others.CommunicateWithRealm;
import com.example.leegram.R;

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
            Intent intent = new Intent(this, MainActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this,
                            imageView,
                            getString(R.string.image_transition));
            startActivity(intent, options.toBundle());
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation);
        });
    }
}
