package com.freespeedvpn.topfreevpn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.freespeedvpn.topfreevpn.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
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
    }
}