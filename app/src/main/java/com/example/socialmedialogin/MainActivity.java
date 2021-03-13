package com.example.socialmedialogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private String name, userName, userId, picURL, profileLink, userBirthday, gender, email, location;
    private ImageView userPic, backBtn;
    private TextView tvFullName, tvUserName, tvGender, tvDOB, tvEmail, tvLink, tvUserHome;
    private String api;
    private LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        userPic = findViewById(R.id.userPic);
        backBtn = findViewById(R.id.btn_back);
        tvFullName = findViewById(R.id.fullName);
        tvUserName = findViewById(R.id.userName);
        tvGender = findViewById(R.id.userGen);
        tvDOB = findViewById(R.id.userdob);
        tvEmail = findViewById(R.id.userEmail);
        tvLink = findViewById(R.id.userLink);
        tvUserHome = findViewById(R.id.userHome);
        ll = (LinearLayout) findViewById(R.id.mainLayout);
        final LinearLayout child = (LinearLayout) ll.findViewById(R.id.ltProfileLink);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        email = intent.getStringExtra("email");
        userId = intent.getStringExtra("id");
        picURL = intent.getStringExtra("picstring");
        api = intent.getStringExtra("social");
        location = intent.getStringExtra("location");
        //userBirthday = intent.getStringExtra("birthday");
        //profileLink = intent.getStringExtra("profilelink");
        //gender = intent.getStringExtra("gender");

        String[] splitEmail = email.split("@");
        userName = "@" + splitEmail[0];

        if(api.equals("0") || api.equals("3")) {


            ll.removeView(child);
            child.removeAllViews();

        }

        if(api.equals("2")) {

            if(location != null && !location.equals("") && !location.equals(" ")) {

                tvUserHome.setText(location);

            }

            userName = "@" + intent.getStringExtra("username");
            profileLink = "https://twitter.com/" + userName;
            tvLink.setText(profileLink);

        }

        if(picURL.equals("No Picture found")) {

            userPic.setImageResource(R.drawable.ic_user);

        } else {

            Picasso.with(MainActivity.this).load(picURL).into(userPic);

        }

        Log.i("Data", email + userPic + name);
        tvFullName.setText(name);
        tvUserName.setText(userName);
        tvEmail.setText(email);
        //tvGender.setText(gender);
        //tvDOB.setText(userBirthday);
        //tvLink.setText(profileLink);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                disconnectFromFacebook();
                Toast.makeText(MainActivity.this,"User Logged out" , Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));

            }
        });

    }

    public void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null,
                HttpMethod.DELETE,
                new GraphRequest
                        .Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        LoginManager.getInstance().logOut();
                    }
                })
                .executeAsync();
    }

}