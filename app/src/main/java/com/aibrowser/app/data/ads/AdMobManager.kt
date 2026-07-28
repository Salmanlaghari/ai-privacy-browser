package com.aibrowser.app.data.ads

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.aibrowser.app.data.billing.PremiumBillingManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import android.widget.Toast
import timber.log.Timber

/**
 * Orchestrates AdMob banner, interstitial, and rewarded ad integration.
 * Respects premium subscription, hiding ads for premium users.
 */
object AdMobManager {

    private const val BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    /**
     * Checks whether ads should be displayed (false if user is premium).
     */
    fun shouldShowAds(context: Context): Boolean {
        val billing = PremiumBillingManager(context)
        return !billing.isPremiumUser()
    }

    /**
     * Loads and attaches a banner ad to the bottom of the container.
     */
    fun loadBannerAd(context: Context, container: ViewGroup) {
        container.removeAllViews()
        if (!shouldShowAds(context)) {
            container.visibility = View.GONE
            return
        }

        val adView = AdView(context).apply {
            adUnitId = BANNER_ID
            setAdSize(AdSize.BANNER)
        }
        container.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        container.visibility = View.VISIBLE
        Timber.d("AdMob: Banner ad request initiated.")
    }

    /**
     * Pre-loads an interstitial ad.
     */
    fun loadInterstitialAd(context: Context) {
        if (!shouldShowAds(context)) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Timber.d("AdMob: Interstitial ad loaded successfully.")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                Timber.w("AdMob: Interstitial ad failed to load: %s", error.message)
            }
        })
    }

    /**
     * Shows the preloaded interstitial ad if available.
     */
    fun showInterstitialAd(activity: Activity) {
        if (!shouldShowAds(activity)) return

        interstitialAd?.let { ad ->
            ad.show(activity)
            interstitialAd = null // Reset after showing
            // Preload next
            loadInterstitialAd(activity)
        } ?: run {
            Timber.i("AdMob: Interstitial ad was not loaded yet.")
            loadInterstitialAd(activity)
        }
    }

    /**
     * Pre-loads a rewarded ad.
     */
    fun loadRewardedAd(context: Context) {
        if (!shouldShowAds(context)) return

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                Timber.d("AdMob: Rewarded ad loaded successfully.")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                Timber.w("AdMob: Rewarded ad failed to load: %s", error.message)
            }
        })
    }

    /**
     * Shows the preloaded rewarded ad and calls [onRewardEarned] upon successful completion.
     */
    fun showRewardedAd(activity: Activity, onRewardEarned: (Int) -> Unit) {
        if (!shouldShowAds(activity)) {
            // Premium users get the reward instantly!
            onRewardEarned(10)
            return
        }

        rewardedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                val amount = rewardItem.amount
                Timber.d("AdMob: Reward earned! Amount: %d", amount)
                onRewardEarned(amount)
            }
            rewardedAd = null // Reset after showing
            loadRewardedAd(activity) // Preload next
        } ?: run {
            Timber.w("AdMob: Rewarded ad not ready.")
            Toast.makeText(activity, "Rewarded ad is not ready. Try again in a moment.", Toast.LENGTH_SHORT).show()
            loadRewardedAd(activity)
        }
    }
}
