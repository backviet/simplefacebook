package com.zegome.utils.facebook;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer.Result;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FacebookHelper implements IFacebook {
    private static final String PERMISSION_PUBLIC_PROFILE = "public_profile";
    private static final String PERMISSION_EMAIL = "email";

    private CallbackManager mCallbackManager;

    private LoginManager mLoginManager;
    private Activity mActivity;

    private boolean canShareLink = false;
    private boolean canSharePhoto = false;

    public static void activateApp(final Application application) {
        AppEventsLogger.activateApp(application);
    }

    public static void activateApp(final Activity activity) {
        AppEventsLogger.activateApp(activity.getApplication());
    }

    public FacebookHelper(final Activity ac) {
        mActivity = ac;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //code is inside method

        mCallbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();

        // Can we present the share dialog for regular links?
        canShareLink = ShareDialog.canShow(ShareLinkContent.class);

        // Can we present the share dialog for photos?
        canSharePhoto = ShareDialog.canShow(SharePhotoContent.class);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean isLoggedIn() {
        final AccessToken accesstoken = AccessToken.getCurrentAccessToken();
        return !(accesstoken == null || accesstoken.getPermissions().isEmpty());
    }

    @Override
    public boolean hasPermissions(List<String> permissions) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null)
            return false;
        for (String permission : permissions) {
            if (!accessToken.getPermissions().contains(permission))
                return false;
        }
        return true;
    }

    //================================================================//
    // Getter & Setter
    //================================================================//
    @NonNull
    public List<String> getDefaultPermission() {
        return Arrays.asList(PERMISSION_PUBLIC_PROFILE, PERMISSION_EMAIL);
    }

    @Override
    public boolean canShareLink() {
        return canShareLink;
    }

    @Override
    public boolean canSharePhoto() {
        return canSharePhoto;
    }

    @Override
    public void logIn(FacebookCallback<LoginResult> taskCallback) {
        mLoginManager.registerCallback(mCallbackManager, taskCallback);
        mLoginManager.logInWithReadPermissions(mActivity, getDefaultPermission());
    }

    private void buildShareLink(final String url, final String quote, final String hashtag, final FacebookCallback<Result> taskCallback) {
        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(url))
                .setQuote(quote)
                .setShareHashtag(new ShareHashtag.Builder()
                        .setHashtag(hashtag.trim().startsWith("#") ? hashtag.trim() : ("#" + hashtag.trim()))
                        .build())
                .build();

        final ShareDialog shareDialog = new ShareDialog(mActivity);
        shareDialog.registerCallback(mCallbackManager, taskCallback);
        if (canShareLink) {
            shareDialog.show(linkContent);
        } else {
            Profile profile = Profile.getCurrentProfile();
            if (profile != null && hasPermissions(getDefaultPermission())) {
                ShareApi.share(linkContent, taskCallback);
            }
        }
    }

    @Override
    public void logOut() {
        mLoginManager.logOut();
    }

    @Override
    public void newFeed(final String url, final String quote, final String hashtag, final FacebookCallback<Result> taskCallback) {
        newFeed(ShareDialog.Mode.AUTOMATIC, url, quote, hashtag, taskCallback);
    }
        @Override
    public void newFeed(final ShareDialog.Mode mode, final String url, final String quote, final String hashtag, final FacebookCallback<Result> taskCallback) {
        if (isLoggedIn()) {
            buildShareLink(url, quote, hashtag, taskCallback);
        } else {
            mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

                @Override
                public void onSuccess(LoginResult result) {
                    buildShareLink(url, quote, hashtag, taskCallback);
                }

                @Override
                public void onCancel() {
                    taskCallback.onCancel();
                }

                @Override
                public void onError(FacebookException error) {
                    taskCallback.onError(error);
                }
            });
            mLoginManager.logInWithPublishPermissions(mActivity, getDefaultPermission());
        }
    }

    private void buildSharePhoto(final boolean isUserGenerated, final String caption, final Bitmap bitmap, final FacebookCallback<Result> taskCallback) {
        final SharePhoto sharePhoto = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .setCaption(TextUtils.isEmpty(caption) ? "Share Photo" : caption)
                .setUserGenerated(isUserGenerated)
                .build();
        final ArrayList<SharePhoto> photos = new ArrayList<SharePhoto>();
        photos.add(sharePhoto);

        final SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder()
                .setPhotos(photos)
                .build();
        final ShareDialog shareDialog = new ShareDialog(mActivity);
        shareDialog.registerCallback(mCallbackManager, taskCallback);
        if (canSharePhoto) {
            shareDialog.show(sharePhotoContent);
        } else if (hasPermissions(getDefaultPermission())) {
            ShareApi.share(sharePhotoContent, taskCallback);
        } else {
            Toast.makeText(mActivity, "Không thể chia sẻ ảnh!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void postPhoto(final String caption, final Bitmap bitmap, final FacebookCallback<Result> taskCallback) {
        postPhoto(true, caption, bitmap, taskCallback);
    }
        @Override
    public void postPhoto(final boolean isUserGenerated, final String caption, final Bitmap bitmap, final FacebookCallback<Result> taskCallback) {
        if (isLoggedIn()) {
            buildSharePhoto(isUserGenerated, caption, bitmap, taskCallback);
        } else {
            mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

                @Override
                public void onSuccess(LoginResult result) {
                    buildSharePhoto(isUserGenerated, caption, bitmap, taskCallback);
                }

                @Override
                public void onCancel() {
                    taskCallback.onCancel();
                }

                @Override
                public void onError(FacebookException error) {
                    taskCallback.onError(error);
                }
            });
            mLoginManager.logInWithPublishPermissions(mActivity, getDefaultPermission());
        }
    }

}
