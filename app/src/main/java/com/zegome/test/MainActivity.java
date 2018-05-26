package com.zegome.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.zegome.utils.facebook.FacebookHelper;

import static java.lang.System.gc;

public class MainActivity extends Activity {

    private FacebookHelper mFacebook = null;

    private Button mBtFacebookLogin;
    private Bitmap mSharePhotoBm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFacebook = new FacebookHelper(this);
        mFacebook.onCreate(savedInstanceState);

        mBtFacebookLogin = findViewById(R.id.main_bt_facebook_login);
        mBtFacebookLogin.setText(mFacebook.isLoggedIn() ? "Logout" : "Login");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mFacebook.onActivityResult(requestCode, resultCode, intent);

    }

    @Override
    protected void onDestroy() {
        releaseShareBitmap();

        super.onDestroy();
    }

    private void releaseShareBitmap() {
        if (mSharePhotoBm != null) {
            do {
                mSharePhotoBm.recycle();
            } while (!mSharePhotoBm.isRecycled());
            mSharePhotoBm = null;
        }
    }

    public static Bitmap getImage(@NonNull final Context context, @NonNull final View root) {
        final DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        return getImage(root, width, height);
    }

    public static Bitmap getImage(@NonNull final View root, final int width, final int height) {
        Bitmap bitmap;
        View v1 = root;
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Bitmap bitmap2 = Bitmap.createBitmap(v1.getDrawingCache());
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(bitmap2, 0, 0, paint);

        v1.setDrawingCacheEnabled(false);

        do {
            bitmap2.recycle();
        } while (!bitmap2.isRecycled());

        gc();

        return bitmap;

    }

    public void onFacebookLogin(View v) {
        if (mFacebook.isLoggedIn()) {
            mFacebook.logOut();
        } else {
            mFacebook.logIn(new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    mBtFacebookLogin.setEnabled(false);
                    showToast("Login success");
                }

                @Override
                public void onCancel() {
                    showToast("Cancel login");
                }

                @Override
                public void onError(FacebookException error) {
                    showToast("Login error: " + error.getMessage());
                }
            });
        }
    }

    public void onFacebookSharePhoto(View v) {
        releaseShareBitmap();
        mSharePhotoBm = getImage(this, findViewById(R.id.main_root));

        mFacebook.sharePhoto("SimpleFacebook post photo test", mSharePhotoBm, new FacebookCallback<Sharer.Result>() {

            @Override
            public void onSuccess(Sharer.Result result) {
                releaseShareBitmap();
                showToast("Posted on your timeline");
            }

            @Override
            public void onCancel() {
                releaseShareBitmap();
                showToast("Canceld posting photo");
            }

            @Override
            public void onError(FacebookException ex) {
                releaseShareBitmap();
                showToastLong("Posts photo error " + (ex == null ? "" : ex.getMessage()));
            }
        });
    }

    public void onFacebookShareLink(View v) {
        if (mFacebook.canShareLink()) {
            mFacebook.shareLink("https://google.com", "SimpleFacebook test", "#simple_facebook", new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    showToastLong("ShareLink success");
                }

                @Override
                public void onCancel() {
                    showToast("ShareLink cancel");
                }

                @Override
                public void onError(FacebookException error) {
                    showToastLong("ShareLink error: " + error.getMessage());
                }
            });
        } else {
            showToast("Can not share link");
        }
    }

    private void showToast(@NonNull final String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }


    private void showToastLong(@NonNull final String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
