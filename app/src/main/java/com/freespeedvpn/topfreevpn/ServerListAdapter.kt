package com.freespeedvpn.topfreevpn

import android.content.Context
import android.preference.PreferenceManager
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
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemServerListBinding.inflate(inflater, parent, false)
        return ServerListViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ServerListViewHolder, position: Int) {
        val bean = list.countryServerList[position]
        binding.txtItemServerName.text = bean.countryName
            binding.txtServerLatency.text = "${bean.latency} ms"

        binding.constraintRoot.setOnClickListener {
            setStringPrefValue(bean.host, OscPreference.HOME_HOSTNAME, prefs)
            setStringPrefValue(bean.countryName, OscPreference.HOME_SELECTED_COUNTRY, prefs)
            setStringPrefValue(bean.user, OscPreference.HOME_USERNAME, prefs)
            setStringPrefValue(bean.pass, OscPreference.HOME_PASSWORD, prefs)
            setIntPrefValue(bean.port.toInt(), OscPreference.SSL_PORT, prefs)
            (context as ServerListActivity).onBackPressed()
        }
    }

    override fun getItemCount(): Int {
        return list.countryServerList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}
