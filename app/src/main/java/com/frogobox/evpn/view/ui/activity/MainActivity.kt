package com.frogobox.evpn.view.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.frogobox.evpn.R
import com.frogobox.evpn.base.adapter.BaseViewListener
import com.frogobox.evpn.base.ui.BaseActivity
import com.frogobox.evpn.helper.Constant.Variable.EXTRA_COUNTRY
import com.frogobox.evpn.source.model.Server
import com.frogobox.evpn.util.PropertiesService
import com.frogobox.evpn.view.adapter.CountryViewAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ads_banner.*
import kotlinx.android.synthetic.main.toolbar_main.*

class MainActivity : BaseActivity(), BaseViewListener<Server> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar_main)
        setupShowAdsInterstitial()
        setupShowAdsBanner(admob_adview)
        setupCheckConnectionState()
        setupViewFunction()
        setupRecyclerView()

    }

    private fun setupViewFunction() {
        tv_total_server.text = String.format(resources.getString(R.string.total_servers), dbHelper.count)

        btn_quick_connect.setOnClickListener { v: View? ->
            if (getRandomServer() != null) {
                newConnecting(getRandomServer(), fastConnection = true, autoConnection = true)
            } else {
                showToast(String.format(resources.getString(R.string.error_random_country), PropertiesService.getSelectedCountry()))
            }
        }

    }

    private fun setupCheckConnectionState() {
        if (hasConnectedServer()) {
            tv_connection_state.text = "Connected"
            tv_connection_state.setBackgroundResource(R.drawable.button3)
        } else {
            tv_connection_state.setBackgroundResource(R.drawable.button2)
            tv_connection_state.text = "No VPN Connected"
        }
    }

    override fun onResume() {
        super.onResume()
        setupCheckConnectionState()
    }

    private fun setupRecyclerView() {
        val countryList = dbHelper.uniqueCountries
        val adapter = CountryViewAdapter()
        adapter.setupRequirement(this, this, countryList, R.layout.view_item_country)
        recycler_view_country.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler_view_country.adapter = adapter

    }

    override fun onItemClicked(data: Server) {
        startActivity(Intent(this, VPNListActivity::class.java).putExtra(EXTRA_COUNTRY, data.countryShort))

    }

    override fun onItemLongClicked(data: Server) {

    }
}