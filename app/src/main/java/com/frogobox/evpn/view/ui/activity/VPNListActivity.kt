package com.frogobox.evpn.view.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.frogobox.evpn.R
import com.frogobox.evpn.base.adapter.BaseViewListener
import com.frogobox.evpn.base.ui.BaseActivity
import com.frogobox.evpn.helper.Constant.Variable.EXTRA_COUNTRY
import com.frogobox.evpn.source.model.Server
import com.frogobox.evpn.view.adapter.ServerViewAdapter
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.android.synthetic.main.activity_vpnlist.*

class VPNListActivity : BaseActivity(), BaseViewListener<Server> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vpnlist)
        setupDetailActivity("")
        setupAdsMonetize()
        setupComponentView()
    }

    private fun setupAdsMonetize() {
        setupShowAdsBanner(findViewById(R.id.admob_adview))
        setupShowAdsInterstitial()
    }

    private fun setupComponentView() {
        val country = intent.getStringExtra(EXTRA_COUNTRY)
        var code = intent.getStringExtra(EXTRA_COUNTRY).toLowerCase()
        if (code == "do") code = "dom"

        if (!VpnStatus.isVPNActive()) connectedServer = null

        tv_country_name.text = country
        imgv.setImageResource(resources.getIdentifier(code, "drawable", packageName))

        setupRecyclerView(country)
    }

    private fun setupRecyclerView(country: String) {
        val serverList = dbHelper.getServersByCountryCode(country)
        val serverViewAdapter = ServerViewAdapter()
        serverViewAdapter.setupRequirement(this, this, serverList, R.layout.recyclerview_item_vpn)
        recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = serverViewAdapter
        getIpInfoFromServerList(serverList)
    }

    override fun onItemClicked(data: Server) {
        baseStartActivity<VPNInfoActivity, Server>(Server::class.java.canonicalName!!, data)
    }

    override fun onItemLongClicked(data: Server) {}

}