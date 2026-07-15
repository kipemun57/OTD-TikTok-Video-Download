package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    const val APP_ID = "ca-app-pub-8650588116475716~7786660711"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-8650588116475716/5987540063"
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-8650588116475716/5160497378"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8650588116475716/4553346889"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShowTime: Long = 0
    private var downloadCounter = 0

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var lastBackgroundTime: Long = 0

    fun init(context: Context) {
        MobileAds.initialize(context) {}
        loadInterstitial(context)
        loadAppOpenAd(context)
    }

    fun setBackgroundTime() {
        lastBackgroundTime = System.currentTimeMillis()
    }

    fun shouldShowAppOpenOnResume(): Boolean {
        if (lastBackgroundTime == 0L) return false
        val hoursInBg = (System.currentTimeMillis() - lastBackgroundTime) / (1000 * 60 * 60)
        return hoursInBg >= 4
    }

    fun loadAppOpenAd(context: Context) {
        if (isAppOpenLoading || appOpenAd != null) return
        isAppOpenLoading = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context.applicationContext,
            APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAppOpenLoading = false
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        val ad = appOpenAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    loadAppOpenAd(activity)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    appOpenAd = null
                    onAdClosed()
                }
            }
            ad.show(activity)
        } else {
            loadAppOpenAd(activity)
            onAdClosed()
        }
    }

    fun loadInterstitial(context: Context) {
        if (isInterstitialLoading || interstitialAd != null) return
        isInterstitialLoading = true
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            INTERSTITIAL_AD_UNIT_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    fun incrementDownloadAndCheckAd(activity: Activity, onAdFinished: () -> Unit) {
        downloadCounter++
        val now = System.currentTimeMillis()
        val timeSinceLastAd = now - lastInterstitialShowTime

        // Show interstitial:
        // 1. Before starting every third download (i.e. downloadCounter % 3 == 0)
        // 2. Frequency capping: never show more than one interstitial every 2 minutes
        val shouldShow = (downloadCounter % 3 == 0) && (timeSinceLastAd >= 120_000)

        if (shouldShow && interstitialAd != null) {
            val ad = interstitialAd!!
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialShowTime = System.currentTimeMillis()
                    loadInterstitial(activity)
                    onAdFinished()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onAdFinished()
                }
            }
            ad.show(activity)
        } else {
            onAdFinished()
            if (interstitialAd == null) {
                loadInterstitial(activity)
            }
        }
    }

    fun showDownloadsPageInterstitial(activity: Activity, onAdFinished: () -> Unit) {
        val now = System.currentTimeMillis()
        val timeSinceLastAd = now - lastInterstitialShowTime

        // Open Downloads screen frequency capping check
        val shouldShow = (timeSinceLastAd >= 120_000) && (interstitialAd != null)

        if (shouldShow) {
            val ad = interstitialAd!!
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialShowTime = System.currentTimeMillis()
                    loadInterstitial(activity)
                    onAdFinished()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onAdFinished()
                }
            }
            ad.show(activity)
        } else {
            onAdFinished()
        }
    }
}
