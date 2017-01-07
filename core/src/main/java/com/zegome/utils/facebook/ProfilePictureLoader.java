package com.zegome.utils.facebook;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.LoggingBehavior;
import com.facebook.internal.ImageDownloader;
import com.facebook.internal.ImageRequest;
import com.facebook.internal.ImageResponse;
import com.facebook.internal.Logger;
import com.facebook.internal.Utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfilePictureLoader {

    /**
     * Tag used when logging calls are made by ProfilePictureView
     */
    public static final String TAG = ProfilePictureLoader.class.getSimpleName();
    
    /**
     * Callback interface that will be called when a network or other error is encountered
     * while retrieving profile pictures.
     */
    public interface OnErrorListener {
        /**
         * Called when a network or other error is encountered.
         * @param error     a FacebookException representing the error that was encountered.
         */
        void onError(FacebookException error);
    }
    
    private final Activity mActivity;
    
    private ImageRequest lastRequest;
    private OnErrorListener onErrorListener;
    private LoadCallback mLoadCallback;

    private String profileId;
    private int queryHeight = ImageRequest.UNSPECIFIED_DIMENSION;
    private int queryWidth = ImageRequest.UNSPECIFIED_DIMENSION;
    
    private String mFileName = "";
    
    public ProfilePictureLoader(final Activity ac, final String fileName) {
    	mActivity = ac;
    	mFileName = fileName;
    	
    	//default size
    	queryWidth = 64;
    	queryHeight = 64;
    }
    
    /**
     * Returns the current OnErrorListener for this instance of ProfilePictureView
     *
     * @return The OnErrorListener
     */
    public final OnErrorListener getOnErrorListener() {
        return onErrorListener;
    }

    /**
     * Sets an OnErrorListener for this instance of ProfilePictureView to call into when
     * certain errors occur.
     *
     * @param onErrorListener The Listener object to set
     */
    public final void setOnErrorListener(OnErrorListener onErrorListener) {
      this.onErrorListener = onErrorListener;
    }
    
    /**
     * Returns the profile Id for the current profile photo
     *
     * @return The profile Id
     */
    public final String getProfileId() {
        return profileId;
    }

    /**
     * Sets the profile Id for this profile photo
     *
     * @param profileId The profileId
     *               NULL/Empty String will show the blank profile photo
     */
    public final void setProfileId(String profileId) {
        boolean force = false;
        if (Utility.isNullOrEmpty(this.profileId) || !this.profileId.equalsIgnoreCase(profileId)) {
            // Clear out the old profilePicture before requesting for the new one.
            force = true;
        }

        this.profileId = profileId;
    }

    private void sendImageRequest(boolean allowCachedResponse) {
        final ImageRequest.Builder requestBuilder = new ImageRequest.Builder(mActivity,
                ImageRequest.getProfilePictureUri(profileId, queryWidth, queryHeight));

        final ImageRequest request = requestBuilder.setAllowCachedRedirects(allowCachedResponse)
                .setCallerTag(this)
                .setCallback(
                new ImageRequest.Callback() {
                    @Override
                    public void onCompleted(ImageResponse response) {
                        processResponse(response);
                    }
                })
                .build();

        // Make sure to cancel the old request before sending the new one to prevent
        // accidental cancellation of the new request. This could happen if the URL and
        // caller tag stayed the same.
        if (lastRequest != null) {
            ImageDownloader.cancelRequest(lastRequest);
        }
        lastRequest = request;

        ImageDownloader.downloadAsync(request);
    }

    private boolean processResponse(ImageResponse response) {
        // First check if the response is for the right request. We may have:
        // 1. Sent a new request, thus super-ceding this one.
        // 2. Detached this view, in which case the response should be discarded.
        if (response.getRequest() == lastRequest) {
            lastRequest = null;
            Bitmap responseImage = response.getBitmap();
            Exception error = response.getError();
            if (error != null) {
                OnErrorListener listener = onErrorListener;
                if (listener != null) {
                    listener.onError(new FacebookException(
                            "Error in downloading profile picture for profileId: " +
                                    getProfileId(), error));

                } else {
                    Logger.log(LoggingBehavior.REQUESTS, Log.ERROR, TAG, error.toString());
                    
                }
                if (mLoadCallback != null) {
                	mLoadCallback.onLoadFailure(error);
                }
                return false;
            } else if (responseImage != null) {
                if (saveFile(responseImage)) {
                    if (mLoadCallback != null) {
                    	mLoadCallback.onLoadSuccess(mFileName + ".png");
                    }	
                } else {
                    if (mLoadCallback != null) {
                    	mLoadCallback.onLoadFailure(new Exception("Profile load error"));
                    }
                }

                return true;
            }
        }
        return false;
    }

    public interface LoadCallback {
    	void onLoadSuccess(String fileName);
    	void onLoadFailure(Exception e);
    }
    
    public boolean loadPicture(final String profileId, final LoadCallback callback) {
    	mLoadCallback = callback;
    	
        boolean force = false;
        if (Utility.isNullOrEmpty(this.profileId) || !this.profileId.equalsIgnoreCase(profileId)) {
            // Clear out the old profilePicture before requesting for the new one.
            force = true;
        }

        this.profileId = profileId;
        // Note: do not use Utility.isNullOrEmpty here as this will cause the Eclipse
        // Graphical Layout editor to fail in some cases
        if (profileId == null || profileId.length() == 0 ||
                ((queryWidth == ImageRequest.UNSPECIFIED_DIMENSION) &&
                        (queryHeight == ImageRequest.UNSPECIFIED_DIMENSION))) {
        	return false;
        } else if (force) {
            sendImageRequest(true);
        }
    	return true;
    }
    
    private boolean saveFile(final Bitmap bitmap) {
    	if (bitmap == null) return false;
    	boolean ret = true;
		FileOutputStream fout = null;
		try {
			File file = new File(mFileName + ".png");
			Log.e("saveFile", "save 1: " + file.getAbsolutePath());
			if (!file.exists())
				try {
					file.getParentFile().mkdirs();
					file.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
		    fout = new FileOutputStream(file);	
		    Toast.makeText(mActivity, "check file" + file.isFile(), Toast.LENGTH_SHORT).show();
		    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fout);

		    fout.flush();
		    fout.close();
		    ret = true;
		} catch (FileNotFoundException e) {
		    ret = false;
		    e.printStackTrace();
		} catch (IOException e) {
		    ret = false;
		    e.printStackTrace();
		    
		} finally {
			bitmap.recycle();
		}
		
		return ret;
    }
}
