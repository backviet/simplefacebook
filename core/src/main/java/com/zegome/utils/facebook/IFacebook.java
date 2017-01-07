package com.zegome.utils.facebook;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.facebook.FacebookCallback;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;

import java.util.List;

public interface IFacebook {
	void onCreate(Bundle savedInstanceState);
	void onPause();
	void onResume();
	void onDestroy();
	void onActivityResult(int requestCode, int resultCode, Intent data);
	
    boolean isLoggedIn();
    void logIn(final FacebookCallback<LoginResult> taskCallback);
    boolean hasPermissions(List<String> permissions);
	void newFeed(final Bundle params, final FacebookCallback<Sharer.Result> taskCallback);
	void postPhoto(final Bundle params, final Bitmap bitmap, final FacebookCallback<Sharer.Result> taskCallback);
	void shareMessenger(final String content);
    void logOut();
}
