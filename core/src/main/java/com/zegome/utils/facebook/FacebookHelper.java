package com.zegome.utils.facebook;

import android.app.Activity;
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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer.Result;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.util.Arrays;
import java.util.List;

public final class FacebookHelper implements IFacebook {
    private static final String PERMISSION = "publish_actions";

    private CallbackManager mCallbackManager;
    private Activity mActivity;

    public FacebookHelper(final Activity ac) {
        mActivity = ac;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mCallbackManager = CallbackManager.Factory.create();
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
    public List<String> getPublishPermission() {
        return Arrays.asList(PERMISSION);
    }

    @Override
    public boolean canShareLink() {
        return ShareDialog.canShow(ShareLinkContent.class);
    }

    @Override
    public boolean canSharePhoto() {
        return ShareDialog.canShow(SharePhotoContent.class);
    }

    @Override
    public void logIn(FacebookCallback<LoginResult> taskCallback) {
        try {
            createCallbackLogin();
            LoginManager.getInstance().registerCallback(mCallbackManager, taskCallback);
            LoginManager.getInstance()
                    .logInWithPublishPermissions(mActivity, Arrays.asList(PERMISSION));
        } catch (Exception e) {
            e.printStackTrace();
            if (taskCallback != null) {
                taskCallback.onError(new FacebookException(e.getMessage()));
            }
        }
    }

    private void createCallbackLogin() {
        if (mCallbackManager != null) {
            LoginManager.getInstance().unregisterCallback(mCallbackManager);
            mCallbackManager = null;
        }
        mCallbackManager = CallbackManager.Factory.create();
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
        if (canShareLink()) {
            shareDialog.show(linkContent);
        } else {
            Profile profile = Profile.getCurrentProfile();
            if (profile != null && hasPermissions(getPublishPermission())) {
                ShareApi.share(linkContent, taskCallback);
            }
        }
    }

    @Override
    public void logOut() {
        LoginManager.getInstance().logOut();
    }

    @Override
    public void shareLink(final String url, final String quote, final String hashtag, final FacebookCallback<Result> taskCallback) {
        shareLink(ShareDialog.Mode.AUTOMATIC, url, quote, hashtag, taskCallback);
    }

    @Override
    public void shareLink(final ShareDialog.Mode mode, final String url, final String quote, final String hashtag, final FacebookCallback<Result> taskCallback) {
        try {
            if (isLoggedIn()) {
                buildShareLink(url, quote, hashtag, taskCallback);
            } else {
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

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
                LoginManager.getInstance().logInWithReadPermissions(mActivity, getPublishPermission());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (taskCallback != null) {
                taskCallback.onError(new FacebookException(e.getMessage()));
            }
        }
    }

    private void buildSharePhoto(final boolean isUserGenerated, final String caption, final Bitmap bitmap, final FacebookCallback<Result> taskCallback) {
        final SharePhoto sharePhoto = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .setCaption(TextUtils.isEmpty(caption) ? "Share Photo" : caption)
                .setUserGenerated(isUserGenerated)
                .build();

        final SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder()
                .addPhoto(sharePhoto)
                .build();
        final ShareDialog shareDialog = new ShareDialog(mActivity);
        shareDialog.registerCallback(mCallbackManager, taskCallback);

        if (canSharePhoto()) {
            shareDialog.show(sharePhotoContent);
        } else if (hasPermissions(getPublishPermission())) {
            ShareApi.share(sharePhotoContent, taskCallback);
        } else {
            Toast.makeText(mActivity, "Can not share your photo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void sharePhoto(final String caption, final Bitmap bitmap, final FacebookCallback<Result> taskCallback) {
        sharePhoto(true, caption, bitmap, taskCallback);
    }

    @Override
    public void sharePhoto(final boolean isUserGenerated, final String caption, final Bitmap bitmap, final FacebookCallback<Result> taskCallback) {
        try {
            if (isLoggedIn()) {
                buildSharePhoto(isUserGenerated, caption, bitmap, taskCallback);
            } else {
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

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
                LoginManager.getInstance()
                        .logInWithPublishPermissions(mActivity, getPublishPermission());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (taskCallback != null) {
                taskCallback.onError(new FacebookException(e.getMessage()));
            }
        }
    }

}
