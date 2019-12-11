package com.frogobox.vpnhero.view.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.frogobox.vpnhero.R
import com.frogobox.vpnhero.base.adapter.BaseViewListener
import com.frogobox.vpnhero.base.ui.BaseActivity
import com.frogobox.vpnhero.helper.Constant.Variable.EXTRA_COUNTRY
import com.frogobox.vpnhero.source.model.Server
import com.frogobox.vpnhero.view.adapter.ServerViewAdapter
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.android.synthetic.main.activity_vpnlist.*

class VPNListActivity : BaseActivity(), BaseViewListener<Server> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vpnlist)
        setupAdsMonetize()
        setupComponentView()
    }

    private fun setupAdsMonetize() {
        setupShowAdsBanner(findViewById(R.id.admob_adview))
        setupShowAdsInterstitial()
    }

    private fun setupComponentView() {
        val country = intent.getStringExtra(EXTRA_COUNTRY)
        if (!VpnStatus.isVPNActive()) connectedServer = null
        setupDetailActivity("")
        setupRecyclerView(country)
    }

    private fun setupRecyclerView(country: String) {
        val serverList = dbHelper.getServersByCountryCode(country)
        val serverViewAdapter = ServerViewAdapter()
        serverViewAdapter.setupRequirement(this, this, serverList, R.layout.view_item_server)
        recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = serverViewAdapter
        getIpInfoFromServerList(serverList)
    }

    override fun onItemClicked(data: Server) {
        baseStartActivity<VPNInfoActivity, Server>(Server::class.java.canonicalName!!, data)
    }

    override fun onItemLongClicked(data: Server) {}

}