package com.example.adtiming;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.openmediation.sdk.InitCallback;
import com.openmediation.sdk.InitConfiguration;
import com.openmediation.sdk.OmAds;
import com.openmediation.sdk.banner.AdSize;
import com.openmediation.sdk.banner.BannerAd;
import com.openmediation.sdk.banner.BannerAdListener;
import com.openmediation.sdk.interstitial.InterstitialAd;
import com.openmediation.sdk.interstitial.InterstitialAdListener;
import com.openmediation.sdk.utils.error.Error;
import com.openmediation.sdk.utils.model.Scene;
import com.openmediation.sdk.video.RewardedVideoAd;
import com.openmediation.sdk.video.RewardedVideoListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private BannerAd bannerAd;
    private LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button1 = findViewById(R.id.banner);
        Button button2 = findViewById(R.id.interstitial);
        Button button3 = findViewById(R.id.rewarded);
        linearLayout = findViewById(R.id.linear);

        InitConfiguration configuration = new InitConfiguration.Builder()
                .appKey("OtnCjcU7ERE0D21GRoquiQBY6YXR3YLl")
                .preloadAdTypes(OmAds.AD_TYPE.INTERSTITIAL, OmAds.AD_TYPE.REWARDED_VIDEO)
                .logEnable(true)
                .build();

        OmAds.init(configuration, new InitCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "init success");
                initInterstitial();
                initRewarded();
            }

            @Override
            public void onError(Error error) {
                Log.d(TAG, "error: " + error.getErrorMessage());
            }
        });


        button1.setOnClickListener(v ->
                initBanner()
        );

        button2.setOnClickListener(v -> {
            if (InterstitialAd.isReady()) {
                InterstitialAd.showAd();
            }
        });

        button3.setOnClickListener(v -> {
            if (RewardedVideoAd.isReady()) {
                RewardedVideoAd.showAd();
            }
        });

    }


    @Override
    protected void onDestroy() {
        if (bannerAd != null) {
            bannerAd.destroy();
        }
        super.onDestroy();
    }

    //
    private void initBanner() {
        bannerAd = new BannerAd("4900", new BannerAdListener() {
            @Override
            public void onBannerAdLoaded(String s, View view) {
                Log.d(TAG, "onBannerAdLoaded: " + s);
                if (view.getParent() != null) {
                    ((ViewGroup) view.getParent()).removeView(view);
                }
                linearLayout.removeAllViews();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                linearLayout.addView(view, layoutParams);
            }

            @Override
            public void onBannerAdLoadFailed(String s, Error error) {
                Log.d(TAG, "onBannerAdLoadFailed: " + s);
                Log.d(TAG, "onBannerAdLoadFailed: " + error);
            }

            @Override
            public void onBannerAdClicked(String s) {
                Log.d(TAG, "onBannerAdClicked: " + s);
            }
        });

        bannerAd.setAdSize(AdSize.BANNER);

        bannerAd.loadAd();
    }

    private void initInterstitial() {
        InterstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialAdAvailabilityChanged(boolean b) {
                Log.d(TAG, "onInterstitialAdAvailabilityChanged: " + b);

            }

            @Override
            public void onInterstitialAdShowed(Scene scene) {
                Log.d(TAG, "onInterstitialAdShowed: " + scene.getN());
            }

            @Override
            public void onInterstitialAdShowFailed(Scene scene, Error error) {
                if (scene != null) {
                    Log.d(TAG, "onInterstitialAdShowFailed: " + scene.getN());
                    Log.d(TAG, "onInterstitialAdShowFailed: " + error.getErrorMessage());
                }
            }

            @Override
            public void onInterstitialAdClosed(Scene scene) {
                Log.d(TAG, "onInterstitialAdClosed: " + scene.getN());
            }

            @Override
            public void onInterstitialAdClicked(Scene scene) {
                Log.d(TAG, "onInterstitialAdClicked: " + scene.getN());

            }
        });
        InterstitialAd.loadAd();

    }

    private void initRewarded() {
        RewardedVideoAd.setAdListener(new RewardedVideoListener() {
            @Override
            public void onRewardedVideoAvailabilityChanged(boolean b) {

            }

            @Override
            public void onRewardedVideoAdShowed(Scene scene) {
                Log.d(TAG, "onRewardedVideoAdShowed: " + scene.getN());
            }

            @Override
            public void onRewardedVideoAdShowFailed(Scene scene, Error error) {
                Log.d(TAG, "onRewardedVideoAdShowFailed: " + scene.getN());
                Log.d(TAG, "onRewardedVideoAdShowFailed: " + error.getErrorMessage());
            }

            @Override
            public void onRewardedVideoAdClicked(Scene scene) {
                Log.d(TAG, "onRewardedVideoAdClicked: " + scene.getN());
            }

            @Override
            public void onRewardedVideoAdClosed(Scene scene) {
                Log.d(TAG, "onRewardedVideoAdClosed: " + scene.getN());
            }

            @Override
            public void onRewardedVideoAdStarted(Scene scene) {
                Log.d(TAG, "onRewardedVideoAdStarted: " + scene.getN());
            }

            @Override
            public void onRewardedVideoAdEnded(Scene scene) {
                Log.d(TAG, "onRewardedVideoAdEnded: " + scene.getN());
            }

            @Override
            public void onRewardedVideoAdRewarded(Scene scene) {
                Log.d(TAG, "onRewardedVideoAdRewarded: " + scene.getN());
            }
        });
        RewardedVideoAd.loadAd();
    }
}