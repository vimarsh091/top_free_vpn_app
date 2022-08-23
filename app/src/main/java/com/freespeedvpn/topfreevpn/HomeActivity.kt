package com.freespeedvpn.topfreevpn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freespeedvpn.topfreevpn.databinding.ActivityHomeBinding
import com.freespeedvpn.topfreevpn.model.CountryServerList
import com.freespeedvpn.topfreevpn.sstpservice.ACTION_VPN_CONNECT
import com.freespeedvpn.topfreevpn.sstpservice.ACTION_VPN_DISCONNECT
import com.freespeedvpn.topfreevpn.sstpservice.SstpVpnService
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kittoku.osc.preference.OscPreference
import kittoku.osc.preference.accessor.getBooleanPrefValue
import kittoku.osc.preference.accessor.getStringPrefValue
import kittoku.osc.preference.accessor.setIntPrefValue
import kittoku.osc.preference.accessor.setStringPrefValue
import java.io.IOException


class HomeActivity : AppCompatActivity(), OnUserEarnedRewardListener {
    private lateinit var serverList: CountryServerList
    lateinit var binding: ActivityHomeBinding
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private final var TAG = "MainActivity"
    var isVpnConnected = false
    private var totalMinutes: Long = 10
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
        counter = object : CountDownTimer(minutes * 60000, 60000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.txtFreeMinutes.text = "${(millisUntilFinished / 60000)} Minutes Left"
                updatedMinutes = millisUntilFinished / 60000

            }

            override fun onFinish() {
                startVpnService(ACTION_VPN_DISCONNECT)
                binding.txtFreeMinutes.text = totalMinutes.toString()

            }
        }.start()
    }


    fun pingg(domain: String): Long {
        var timeofping = 0L
        val runtime = Runtime.getRuntime();

        try {

            val a = (System.currentTimeMillis() % 100000);

            val ipProcess = runtime.exec("/system/bin/ping - c 1 " + domain);

            ipProcess.waitFor();

            val b = (System.currentTimeMillis() % 100000);

            if (b <= a) {

                timeofping = ((100000 - a) + b);

            } else {

                timeofping = (b - a);
            }

        } catch (e: Exception) {


        }

        return timeofping;

    }


    private fun init() {
        binding.apply {

            serverList = getServerList(this@HomeActivity)
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
                        binding.txtFreeMinutes.text = totalMinutes.toString()
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
                binding.checkBox.isChecked = false
            }

            connectVpnClick()


            // Clicks

            imgExit.setOnClickListener {
                finishAffinity()
            }

            imgRestart.setOnClickListener {

//                (this@HomeActivity.getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
//                binding.txtServerName.text = getStringPrefValue(OscPreference.HOME_SELECTED_COUNTRY, prefs)

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

            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    //ping all servers
                    progressBar.visibility = View.VISIBLE
                    serverList.countryServerList.forEachIndexed { index, countryServer ->
                        serverList.countryServerList[index].latency = pingg(countryServer.host)
                    }

                    //check max latency
                    getMaxLatency(serverList)
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressBar.visibility = View.GONE
                        binding.txtServerName.text = getStringPrefValue(OscPreference.HOME_SELECTED_COUNTRY, prefs)
                    }, 1000)

                } else {
                    binding.txtServerName.text = getStringPrefValue(OscPreference.HOME_SELECTED_COUNTRY, prefs)
                }
            }


        }
    }

    fun getMaxLatency(serverList: CountryServerList) {
        var maxLatency = 0L
        serverList.countryServerList.forEach {
            Log.e("serverName And latency", "${it.countryName} =-= ${it.latency}")
            if (it.latency > maxLatency) {
                maxLatency = it.latency
            }
        }

        serverList.countryServerList.forEach {
            if (it.latency == maxLatency) {
                setStringPrefValue(it.host, OscPreference.HOME_HOSTNAME, prefs!!)
                setStringPrefValue(it.pass, OscPreference.HOME_PASSWORD, prefs!!)
                setStringPrefValue(it.user, OscPreference.HOME_USERNAME, prefs!!)
                setStringPrefValue(it.countryName, OscPreference.HOME_SELECTED_COUNTRY, prefs!!)
                setIntPrefValue(it.port.toInt(), OscPreference.SSL_PORT, prefs!!)
            }
        }
        Log.e("maxLatencyNuber", maxLatency.toString())

    }

    override fun onResume() {
        super.onResume()
        binding.txtServerName.text = getStringPrefValue(OscPreference.HOME_SELECTED_COUNTRY, prefs)

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


        val newMinutes = updatedMinutes + 60
        counter?.cancel()

        Log.e("newMinutes", newMinutes.toString())

        timer(newMinutes)

    }

    override fun onDestroy() {
        super.onDestroy()
        startVpnService(ACTION_VPN_DISCONNECT)
    }

    fun getServerList(context: Context): CountryServerList {

        lateinit var jsonString: String
        try {
            jsonString = context.assets.open("ServerList/country_vpn_list.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {

        }

        val listCountryType = object : TypeToken<CountryServerList>() {}.type
        return Gson().fromJson(jsonString, listCountryType)
    }


}
