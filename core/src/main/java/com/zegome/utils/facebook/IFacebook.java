package com.zegome.utils.facebook;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.facebook.FacebookCallback;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.widget.ShareDialog;

import java.util.List;

public interface IFacebook {
	void onCreate(Bundle savedInstanceState);
	void onActivityResult(int requestCode, int resultCode, Intent data);

	boolean canShareLink();
	boolean canSharePhoto();

    boolean isLoggedIn();
    void logIn(final FacebookCallback<LoginResult> taskCallback);
    boolean hasPermissions(List<String> permissions);
	void shareLink(final ShareDialog.Mode mode, final String url, final String quote, final String hashtag, final FacebookCallback<Sharer.Result> taskCallback);
	void shareLink(final String url, final String quote, final String hashtag, final FacebookCallback<Sharer.Result> taskCallback);
	void sharePhoto(final String  caption, final Bitmap bitmap, final FacebookCallback<Sharer.Result> taskCallback);
	void sharePhoto(final boolean isUserGenerated, final String  caption, final Bitmap bitmap, final FacebookCallback<Sharer.Result> taskCallback);

    void logOut();
}
