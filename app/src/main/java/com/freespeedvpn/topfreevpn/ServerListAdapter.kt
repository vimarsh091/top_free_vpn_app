package com.freespeedvpn.topfreevpn

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.freespeedvpn.topfreevpn.databinding.ItemServerListBinding
import com.freespeedvpn.topfreevpn.model.CountryServerList
import kittoku.osc.preference.OscPreference
import kittoku.osc.preference.accessor.setIntPrefValue
import kittoku.osc.preference.accessor.setStringPrefValue

class ServerListAdapter(val context: Context, val list: CountryServerList) :
    RecyclerView.Adapter<ServerListAdapter.ServerListViewHolder>() {
    lateinit var binding: ItemServerListBinding
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    inner class ServerListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerListViewHolder {
        binding = ItemServerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServerListViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ServerListViewHolder, position: Int) {
        val bean = list.countryServerList[position]
        binding.txtItemServerName.text = bean.countryName
        val latency = pingg(bean.host)
        binding.txtServerLatency.text = "${latency.toString()} ms"
        bean.latency = latency

        binding.constraintRoot.setOnClickListener {
            setStringPrefValue(bean.host, OscPreference.HOME_HOSTNAME, prefs)
            setStringPrefValue(bean.countryName, OscPreference.HOME_SELECTED_COUNTRY, prefs)
            setStringPrefValue(bean.user, OscPreference.HOME_USERNAME, prefs)
            setStringPrefValue(bean.pass, OscPreference.HOME_PASSWORD, prefs)
            setIntPrefValue(bean.port.toInt(), OscPreference.SSL_PORT, prefs)
            (context as ServerListActivity).onBackPressed()
            Log.e("selectedServerCountryName", bean.countryName)
        }
    }

    override fun getItemCount(): Int {
        return list.countryServerList.size
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

}
