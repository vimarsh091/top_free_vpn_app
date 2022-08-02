package com.freespeedvpn.topfreevpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.freespeedvpn.topfreevpn.databinding.ActivityHomeBinding
import com.freespeedvpn.topfreevpn.sstpservice.ACTION_VPN_CONNECT
import com.freespeedvpn.topfreevpn.sstpservice.ACTION_VPN_DISCONNECT
import com.freespeedvpn.topfreevpn.sstpservice.SstpVpnService

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding

    var isVpnConnected = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        init()

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
        this.startService(Intent(this, SstpVpnService::class.java).setAction(action))
    }

}
