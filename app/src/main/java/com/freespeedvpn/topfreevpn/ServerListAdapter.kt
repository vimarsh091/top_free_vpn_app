package com.freespeedvpn.topfreevpn

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.freespeedvpn.topfreevpn.databinding.ItemServerListBinding

class ServerListAdapter(contect: Context) :
    RecyclerView.Adapter<ServerListAdapter.ServerListViewHolder>() {
    lateinit var binding: ItemServerListBinding

    inner class ServerListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerListViewHolder {
        binding = ItemServerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServerListViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ServerListViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 20
    }

}
