package com.freespeedvpn.topfreevpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.freespeedvpn.topfreevpn.databinding.ActivityHomeBinding
import com.freespeedvpn.topfreevpn.sstpservice.ACTION_VPN_CONNECT
import com.freespeedvpn.topfreevpn.sstpservice.ACTION_VPN_DISCONNECT
import com.freespeedvpn.topfreevpn.sstpservice.SstpVpnService
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

class HomeActivity : AppCompatActivity(), OnUserEarnedRewardListener {
    lateinit var binding: ActivityHomeBinding
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private final var TAG = "MainActivity"
    var isVpnConnected = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        init()
        MobileAds.initialize(this) {
            binding.txtRewardButton.setOnClickListener {
                loadAd()
            }
        }

    }

    private fun loadAd() {
        RewardedInterstitialAd.load(this, "ca-app-pub-3940256099942544/5354046379",
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedInterstitialAd = ad
                    rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d(TAG, "Ad was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Log.d(TAG, "Ad dismissed fullscreen content.")
                            rewardedInterstitialAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            // Called when ad fails to show.
                            Log.e(TAG, "Ad failed to show fullscreen content.")
                            rewardedInterstitialAd = null
                        }

                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d(TAG, "Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d(TAG, "Ad showed fullscreen content.")
                        }

                    }
                    rewardedInterstitialAd?.show(/* Activity */ this@HomeActivity, /*
    OnUserEarnedRewardListener */ this@HomeActivity
                    )
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError?.toString()?.let { Log.d(TAG, it) }
                    rewardedInterstitialAd = null
                }
            })
    }


    private fun init() {
        binding.apply {
            txtServerName.setOnClickListener {
                val intent = Intent(this@HomeActivity, ServerListActivity::class.java)
                startActivity(intent)
            }
        }
        connectVpnClick()

    }


    fun connectVpnClick() {
        binding.btnConnect.setOnClickListener {
            if (isVpnConnected) {
                startVpnService(ACTION_VPN_DISCONNECT)
            } else {
                VpnService.prepare(this)?.also { intent ->
                    startActivityForResult(intent, 0)
                } ?: onActivityResult(0, Activity.RESULT_OK, null)
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            startVpnService(ACTION_VPN_CONNECT)
        }
    }

    /*private fun startVpnService(action: String) {
        startService(Intent(this, SstpVpnService::class.java).setAction(action))
    }*/

    private fun startVpnService(action: String) {
        startService(Intent(this@HomeActivity, SstpVpnService::class.java).setAction(action))
    }

    override fun onUserEarnedReward(p0: RewardItem) {

        Log.e(TAG, "RewardEarned")

    }


}
