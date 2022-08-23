package com.freespeedvpn.topfreevpn

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.freespeedvpn.topfreevpn.databinding.ActivityServerListBinding
import com.freespeedvpn.topfreevpn.model.CountryServerList
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kittoku.osc.preference.OscPreference
import kittoku.osc.preference.accessor.setIntPrefValue
import kittoku.osc.preference.accessor.setStringPrefValue
import java.io.IOException

class ServerListActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null
    private lateinit var list: CountryServerList
    lateinit var binding: ActivityServerListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_server_list)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        init()
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

    fun init() {

        list = getServerList(this)

        binding.apply {

            val serverAdapter = ServerListAdapter(this@ServerListActivity, list)
            rvServerList.adapter = serverAdapter
            rvServerList.setHasFixedSize(true)
            imgBack.setOnClickListener {
                onBackPressed()
            }

            imgRefresh.setOnClickListener {
                rvServerList.removeAllViews()
                serverAdapter.notifyDataSetChanged()
            }

        }
    }
}