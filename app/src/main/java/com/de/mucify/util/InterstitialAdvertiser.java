package com.de.mucify.util;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;


public class InterstitialAdvertiser {
    protected static InterstitialAd mInterstitialAd;

    // Time until the next ad is allowed to display in seconds
    private static long mDownTime = 5;

    // UTC time when the next ad is allowed to play
    private static long mNextAdTime = 0;

    private final Activity mActivity;

    public InterstitialAdvertiser(Activity activity, int downtime) {
        this(activity);
        mDownTime = downtime;
    }

    public InterstitialAdvertiser(Activity activity) {
        mActivity = activity;
        MobileAds.initialize(mActivity, initializationStatus -> {
            MucifyApplication.AdsInitialized = true;
            loadAd();
        });
    }

    public boolean allowedToAdvertise() {
        return System.currentTimeMillis() / 1000 >= mNextAdTime;
    }

    protected void showAd(FullScreenContentCallback callback) {
        mInterstitialAd.setFullScreenContentCallback(callback);
        mInterstitialAd.show(mActivity);

        mNextAdTime = System.currentTimeMillis() / 1000 + mDownTime;
    }

    protected void loadAd() {
        InterstitialAd.load(mActivity, mActivity.getString(R.string.admob_interstitial_select_to_play_id), new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
//                        Log.e("InterstitialAdvertiser", error.getMessage());
                        Utils.startErrorActivity("InterstitialAdvertiser: " + error.getMessage());
                    }
                }
        );
    }
}
