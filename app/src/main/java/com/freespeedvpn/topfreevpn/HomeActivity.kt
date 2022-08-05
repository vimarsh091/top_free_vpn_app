package com.freespeedvpn.topfreevpn

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freespeedvpn.topfreevpn.databinding.ActivityHomeBinding
import com.freespeedvpn.topfreevpn.sstpservice.ACTION_VPN_CONNECT
import com.freespeedvpn.topfreevpn.sstpservice.ACTION_VPN_DISCONNECT
import com.freespeedvpn.topfreevpn.sstpservice.SstpVpnService
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kittoku.osc.preference.OscPreference
import kittoku.osc.preference.accessor.getBooleanPrefValue

class HomeActivity : AppCompatActivity(), OnUserEarnedRewardListener {
    lateinit var binding: ActivityHomeBinding
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private final var TAG = "MainActivity"
    var isVpnConnected = false
    private var totalMinutes: Long = 3600000L
    var updatedMinutes: Long = 0
    var counter: CountDownTimer? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        init()
        MobileAds.initialize(this) {
            loadAd()
        }

    }

    private fun loadAd() {
        RewardedInterstitialAd.load(this, getString(R.string.google_ad_id),
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

                    binding.txtRewardButton.setOnClickListener {
                        rewardedInterstitialAd?.show(this@HomeActivity, this@HomeActivity)
                        loadAd()
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError?.toString()?.let { Log.d(TAG, it) }
                    rewardedInterstitialAd = null
                }
            })
    }

    private fun timer(minutes: Long) {
        counter = object : CountDownTimer(minutes, 60000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.txtFreeMinutes.text = "${(millisUntilFinished / 60000)} Minutes Left"
                updatedMinutes = millisUntilFinished / 60000

            }

            override fun onFinish() {
                startVpnService(ACTION_VPN_DISCONNECT)
                binding.txtFreeMinutes.text = "50"

            }
        }.start()
    }


    private fun init() {
        binding.apply {
            listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == OscPreference.ROOT_STATE.name) {
                    val newState = getBooleanPrefValue(OscPreference.ROOT_STATE, prefs)
                    if (newState == true) {
                        binding.btnConnect.setText(R.string.connected)
                        binding.btnConnect.backgroundTintList = ContextCompat.getColorStateList(this@HomeActivity, R.color.green_connected);
                        binding.btnConnect.strokeColor = ColorStateList.valueOf(getColor(R.color.grenn_stroke))
                        timer(totalMinutes)

                    } else {
                        counter?.cancel()
                        binding.txtFreeMinutes.text = "50"
                        binding.btnConnect.setText(R.string.connect)
                        binding.btnConnect.backgroundTintList = ContextCompat.getColorStateList(this@HomeActivity, R.color.brown_connect);
                        binding.btnConnect.strokeColor = ColorStateList.valueOf(getColor(R.color.black))
                    }
                }
            }

            prefs.registerOnSharedPreferenceChangeListener(listener)

            txtServerName.setOnClickListener {
                val intent = Intent(this@HomeActivity, ServerListActivity::class.java)
                startActivity(intent)
            }

            connectVpnClick()


            // Clicks

            imgExit.setOnClickListener {
                finishAffinity()
            }

            imgShare.setOnClickListener {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                )
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            }

            imgLike.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")))
            }
        }
    }


    private fun connectVpnClick() {
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

    private fun startVpnService(action: String) {
        startService(Intent(this@HomeActivity, SstpVpnService::class.java).setAction(action))
    }

    override fun onUserEarnedReward(p0: RewardItem) {
        Log.e(TAG, "RewardEarned")


        val newMinutes =(updatedMinutes * 60000) + 1800000
        counter?.cancel()

        Log.e("newMinutes",newMinutes.toString())

        timer(newMinutes)

    }

    override fun onDestroy() {
        super.onDestroy()
        startVpnService(ACTION_VPN_DISCONNECT)
    }


}
