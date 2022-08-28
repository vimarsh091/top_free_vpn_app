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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*


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


/*    fun pingg(domain: String): Long {
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

    }*/


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
                    btnConnect.isEnabled = false

                    GlobalScope.launch {
                        val fetchLatency = fetchLatency()

                        if (fetchLatency) {
                            runOnUiThread {
                                progressBar.visibility = View.GONE
                                btnConnect.isEnabled = true
                            }
                        }
                    }

                } else {
                    binding.txtServerName.text = getStringPrefValue(OscPreference.HOME_SELECTED_COUNTRY, prefs)
                    btnConnect.isEnabled = true
                }
            }


        }
    }

    private fun fetchLatency(): Boolean {
        serverList.countryServerList.forEachIndexed { index, countryServer ->
            serverList.countryServerList[index].isHostAvailable = isHostAvailable(countryServer.host, countryServer.port.toInt(), 2000)

            //  serverList.countryServerList[index].latency = /*pingg(countryServer.host)*/ ping(countryServer.host).toLong()
        }

        serverList.countryServerList.forEachIndexed { index, countryServer ->

            if (countryServer.isHostAvailable) {
                val rNum = (100..300).random()
                serverList.countryServerList[index].latency = ping(countryServer.host).toLong()
                //    list.countryServerList[index].latency = rNum.toLong()
            }else{
                serverList.countryServerList[index].latency = 9999.99.toLong()
            }

//            list.countryServerList[index].latency = ping(countryServer.host).toLong()
        }

        //check max latency
        getMaxLatency(serverList)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.txtServerName.text = getStringPrefValue(OscPreference.HOME_SELECTED_COUNTRY, prefs)
        }, 1000)

        return true
    }

    private fun getMaxLatency(serverList: CountryServerList) {
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

    private fun ping(url: String): Float {
        var str: String = ""
        try {
            val process = Runtime.getRuntime().exec(String.format(Locale.getDefault(), "/system/bin/ping -c 1 -W 1 %1\$s", url))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var i: Int
            val buffer = CharArray(4096)
            val output = StringBuilder()
            while (reader.read(buffer).also { i = it } > 0) output.append(buffer, 0, i)
            reader.close()
            str = output.toString()
        } catch (e: IOException) {
            Log.e("MyPINGLOG", "PingFailureType.COULD_NOT_PING")
        }

        //Try to get ping time, if ping was successful
        return try {
            str = str.substring(str.indexOf("time=") + 5, str.indexOf("ms") - 1)
            str.toFloat()
        } catch (e: java.lang.Exception) { /* Do nothing */
            0f
        }
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

    private fun getServerList(context: Context): CountryServerList {

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

    private fun isHostAvailable(host: String?, port: Int, timeout: Int): Boolean {
        try {
            Socket().use { socket ->
                val inetAddress: InetAddress = InetAddress.getByName(host)
                val inetSocketAddress = InetSocketAddress(inetAddress, port)
                socket.connect(inetSocketAddress, timeout)

                Log.e("MyLOGSERVERAVAIlable-=-=-=", "+_+_+VAILABLE+_+_$host")
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()

            Log.e("MyLOGSERVERAVAIlable-=-=-=", "+_+_+NOT  VAILABLE+_+_$host")
            return false
        }
    }


}
