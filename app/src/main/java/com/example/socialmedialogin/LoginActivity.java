package com.example.socialmedialogin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.shantanudeshmukh.linkedinsdk.LinkedInBuilder;
import com.shantanudeshmukh.linkedinsdk.helpers.LinkedInUser;
import com.shantanudeshmukh.linkedinsdk.helpers.OnBasicProfileListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private Button btnFbLogin, btnGoogleLogin, btnTwitterLogin, btnLinkedinLogin;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private ProgressDialog progressDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 7;
    private static final int LI_SDK_AUTH_REQUEST_CODE = 3672, FB_REQUEST_CODE = 64206;
    private TwitterAuthClient mTwitterAuthClient;
    public static final int LINKEDIN_REQUEST = 99;
    private static String clientID = "78udh55a79z0t3";
    private static String clientSecret = "brGMwFxlr3UDBUkL";
    private static String redirectUrl = "https://www.linkedin.com/oauth/v2/authorization/";
    private long accessTokenExpiry;
    private String accessToken;

    private String api; //0 for google, 1 for facebook, 2 for twitter, 3 for linkedin;

    //twitter api key = rfPNsM8vGTEip2DdVzeLKiIqt
    //api secret key = AdKrN5fN4kSz3JcdBpkPL8W9IkM1AcoVKAnBZL5B5aJ5587NgV
    //bearer token = AAAAAAAAAAAAAAAAAAAAAKZONgEAAAAAPXvWIOARuk9r2swnP5lfaxGvQqQ%3DpbD9K3zzJdJOC4uhzYXEEMAbliUwDGdTnhL4Hi21kVJEJfQ9ut

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnFbLogin = findViewById(R.id.btn_fb);
        btnGoogleLogin = findViewById(R.id.btn_google);
        btnTwitterLogin = findViewById(R.id.btn_tw);
        btnLinkedinLogin = findViewById(R.id.btn_ldn);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Loading...");

        loginManager = LoginManager.getInstance();
        callbackManager = CallbackManager.Factory.create();

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.consumerKey), getString(R.string.consumerSecret)))
                .debug(true)
                .build();
        Twitter.initialize(config);

        btnLinkedinLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                getCredentials();
                api = "3";
                LinkedInBuilder.getInstance(LoginActivity.this)
                        .setClientID(clientID)
                        .setClientSecret(clientSecret)
                        .setRedirectURI(redirectUrl)
                        .authenticate(LINKEDIN_REQUEST);

            }
        });

        btnFbLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                api = "1";
                progressDialog.show();
                loginManager.logInWithReadPermissions(
                        LoginActivity.this,
                        Arrays.asList(
                                "email",
                                "public_profile")); //,"user_birthday"

                checkLoginStatus();

            }
        });

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                api = "0";
                progressDialog.show();
                switch (v.getId()) {

                    case R.id.btn_google:
                        googleSignIn();
                        break;

                }
            }
        });

        btnTwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                api = "2";
                mTwitterAuthClient= new TwitterAuthClient();
                mTwitterAuthClient.authorize(LoginActivity.this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> result) {

                        TwitterSession twitterSession = result.data;
                        //fetchTwitterEmail(twitterSession);
                        fetchTwitterImage();

                    }

                    @Override
                    public void failure(TwitterException exception) {

                        Toast.makeText(LoginActivity.this, "Failed to authenticate. Please try again.", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loginManager.registerCallback(
                callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        if(progressDialog.isShowing()) {

                            progressDialog.dismiss();

                        }

                    }

                    @Override
                    public void onCancel() {

                        if(progressDialog.isShowing()) {

                            progressDialog.dismiss();

                        }

                    }

                    @Override
                    public void onError(FacebookException error) {

                        if(progressDialog.isShowing()) {

                            progressDialog.dismiss();

                        }
                        Log.i("Error", error.toString());

                    }
                });

    }

    public void fetchLiUserProfile() {

        LinkedInBuilder.retrieveBasicProfile(accessToken, accessTokenExpiry, new OnBasicProfileListener() {
            @Override
            public void onDataRetrievalStart() {

            }

            @Override
            public void onDataSuccess(LinkedInUser user) {

                setDataToMain(user);

            }

            @Override
            public void onDataFailed(int errCode, String errMessage) {

                Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCredentials() {
        try {
            InputStream is = getAssets().open("linkedin-credentials.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONObject linkedinCred = new JSONObject(json);
            clientID = linkedinCred.getString("client_id");
            clientSecret = linkedinCred.getString("client_secret");
            redirectUrl = linkedinCred.getString("redirect_url");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private void setDataToMain(LinkedInUser user) {

        String name = user.getFirstName() + " " + user.getLastName();
        String email = user.getEmail();
        String link = user.getProfileUrl();
        String id = user.getId();

        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("id", id);
        intent.putExtra("picstring", link);
        intent.putExtra("social", api);
        startActivity(intent);

    }

    public void fetchTwitterImage() {
        //check if user is already authenticated or not
        if (getTwitterSession() != null) {

            progressDialog.show();

            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();

            //pass includeEmail : true if you want to fetch Email as well
            Call<User> call = twitterApiClient.getAccountService().verifyCredentials(true, false, true);
            call.enqueue(new Callback<User>() {

                @Override
                public void success(Result<User> result) {

                    User user = result.data;
                    //userDetailsLabel.setText("User Id : " + user.id + "\nUser Name : " + user.name + "\nEmail Id : " + user.email + "\nScreen Name : " + user.screenName);

                    String name = user.name;
                    String username = user.screenName;
                    String location = user.location;
                    String email = user.email;
                    String id = String.valueOf(user.id);
                    String link = user.profileImageUrl;
                    link = link.replace("_normal", "");
                    link = link.replace("http://", "https://");
                    Log.e("TAG", "Data : " + email + " " + link);
                    api = "2";
                    //NOTE : User profile provided by twitter is very small in size i.e 48*48
                    //Link : https://developer.twitter.com/en/docs/accounts-and-users/user-profile-images-and-banners
                    //so if you want to get bigger size image then do the following:

                    if (progressDialog.isShowing()) {

                        progressDialog.dismiss();

                    }

                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("email", email);
                    intent.putExtra("id", id);
                    intent.putExtra("picstring", link);
                    intent.putExtra("username", username);
                    intent.putExtra("location", location);
                    intent.putExtra("social", api);
                    startActivity(intent);

                }

                @Override
                public void failure(TwitterException exception) {
                    Toast.makeText(LoginActivity.this, "Failed to authenticate. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //if user is not authenticated first ask user to do authentication
            Toast.makeText(this, "First to Twitter auth to Verify Credentials.", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * get authenticates user session
     *
     * @return twitter session
     */
    private TwitterSession getTwitterSession() {
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();

        //NOTE : if you want to get token and secret too use uncomment the below code
        /*TwitterAuthToken authToken = session.getAuthToken();
        String token = authToken.token;
        String secret = authToken.secret;*/

        return session;
    }

    private void googleSignIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            String url = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "No Picture found";
            api = "0";

            Log.i("GLOGIN", url);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("name", account.getDisplayName());
            intent.putExtra("email", account.getEmail());
            intent.putExtra("picstring", url);
            intent.putExtra("social", api);

            if(progressDialog.isShowing()) {

                progressDialog.dismiss();

            }

            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
            startActivity(intent);

        } catch (ApiException e) {

            if(progressDialog.isShowing()) {

                progressDialog.dismiss();

            }

            Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == FB_REQUEST_CODE)
            callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        if (requestCode == LINKEDIN_REQUEST && data != null) {
            if (resultCode == RESULT_OK) {

                //Successfully signed in and retrieved data
                LinkedInUser user = data.getParcelableExtra("social_login");
                setDataToMain(user);

            } else {

                if (data.getIntExtra("err_code", 0) == LinkedInBuilder.ERROR_USER_DENIED) {
                    //user denied access to account
                    Toast.makeText(this, "User Denied Access", Toast.LENGTH_SHORT).show();
                } else if (data.getIntExtra("err_code", 0) == LinkedInBuilder.ERROR_USER_DENIED) {
                    //some error occured
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }


            }
        }

        if(requestCode == TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE)
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);

    }

    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            if(currentAccessToken==null) {

                Toast.makeText(LoginActivity.this,"User Logged Out",Toast.LENGTH_SHORT).show();

            }
            else
                fbLogin(currentAccessToken);

        }
    };

    public void fbLogin(AccessToken accessToken) {

        progressDialog.show();
        Bundle parameters = new Bundle();
        parameters.putBoolean("redirect", false);
        parameters.putString(
                "fields",
                "first_name, last_name, email, id, picture.type(normal)"); /*, birthday, link, gender*/

        new GraphRequest(
                    accessToken,
                    "me",
                    parameters,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {

                        @Override
                        public void onCompleted(GraphResponse response) {

                            if(response != null) {

                                try {

                                    JSONObject object = response.getJSONObject();
                                    String name = object.getString("first_name") + " " + object.getString("last_name");
                                    String email = object.getString("email");
                                    String id = object.getString("id");
                                    //String birthday = object.getString("birthday");
                                    //String profileLink = object.getString("link");
                                    //String gender = object.getString("gender");

                                    String link = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    api = "1";
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("name", name);
                                    intent.putExtra("email", email);
                                    intent.putExtra("id", id);
                                    intent.putExtra("picstring", link);
                                    intent.putExtra("social", api);
                                    //intent.putExtra("birthday", birthday);
                                    //intent.putExtra("gender", gender);
                                    //intent.putExtra("profilelink", profileLink);

                                    if(progressDialog.isShowing()) {

                                        progressDialog.dismiss();

                                    }

                                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);

                                    // do action after Facebook login success
                                    // or call your API
                                }
                                catch (Exception e) {

                                    Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    Log.i("Error", "SOME ERROR" + e.toString());
                                    e.printStackTrace();

                                }


                            }

                        }
                    }

            ).executeAsync();

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

    private void checkLoginStatus() {
        if (AccessToken.getCurrentAccessToken() != null) {
            fbLogin(AccessToken.getCurrentAccessToken());
        }
    }

}