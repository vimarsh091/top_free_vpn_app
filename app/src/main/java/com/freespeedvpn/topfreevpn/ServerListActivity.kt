package com.freespeedvpn.topfreevpn

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.freespeedvpn.topfreevpn.databinding.ActivityServerListBinding
import com.freespeedvpn.topfreevpn.model.CountryServerList
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

class ServerListActivity : AppCompatActivity() {
    private lateinit var serverAdapter: ServerListAdapter
    private var prefs: SharedPreferences? = null
    private lateinit var list: CountryServerList
    lateinit var binding: ActivityServerListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_server_list)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        init()

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

    private fun init() {

        binding.apply {
            list = getServerList(this@ServerListActivity)
            serverAdapter = ServerListAdapter(this@ServerListActivity, list)
            binding.rvServerList.adapter = serverAdapter
            setListData(list)

            imgBack.setOnClickListener {
                onBackPressed()
            }

            imgRefresh.setOnClickListener {
                setListData(list)
            }
        }

    }

    private fun setListData(list: CountryServerList) {
        binding.progressBar.visibility = View.VISIBLE

        GlobalScope.launch {
            val fetchLatency = async { fetLatency(list) }

            if (fetchLatency.await()) {
                runOnUiThread {
                    serverAdapter = ServerListAdapter(this@ServerListActivity, list)
                    binding.rvServerList.adapter = serverAdapter
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun fetLatency(list: CountryServerList): Boolean {
        list.countryServerList.forEachIndexed { index, countryServer ->
            list.countryServerList[index].isHostAvailable = isHostAvailable(countryServer.host, countryServer.port.toInt(), 2000)
        }

        list.countryServerList.forEachIndexed { index, countryServer ->

            if (countryServer.isHostAvailable) {
                val rNum = (100..300).random()
                list.countryServerList[index].latency = ping(countryServer.host).toLong()
            //    list.countryServerList[index].latency = rNum.toLong()
            }else{
                list.countryServerList[index].latency = 9999.99.toLong()
            }

//            list.countryServerList[index].latency = ping(countryServer.host).toLong()
        }
        return true
    }

    private fun isHostAvailable(host: String?, port: Int, timeout: Int): Boolean {
        try {
            Socket().use { socket ->
                val inetAddress: InetAddress = InetAddress.getByName(host)
                val inetSocketAddress = InetSocketAddress(inetAddress, port)
                socket.connect(inetSocketAddress, timeout)

                Log.e("MyLOGSERVERAVAIlable-=-=-=","+_+_+VAILABLE+_+_$host")
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()

            Log.e("MyLOGSERVERAVAIlable-=-=-=","+_+_+NOT  VAILABLE+_+_$host")
            return false
        }
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

}