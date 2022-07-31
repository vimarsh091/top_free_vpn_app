package com.freespeedvpn.topfreevpn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.freespeedvpn.topfreevpn.databinding.ActivityServerListBinding

class ServerListActivity : AppCompatActivity() {
    lateinit var binding: ActivityServerListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_server_list)


        init()
    }

    fun init() {
        binding.apply {
            rvServerList.adapter = ServerListAdapter(this@ServerListActivity)
        }
    }

}