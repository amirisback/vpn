package com.frogobox.evpn.view.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.frogobox.evpn.R
import com.frogobox.evpn.base.adapter.BaseViewListener
import com.frogobox.evpn.base.ui.BaseActivity
import com.frogobox.evpn.helper.Constant
import com.frogobox.evpn.source.model.Server
import com.frogobox.evpn.view.adapter.CountryViewAdapter
import kotlinx.android.synthetic.main.activity_country.*
import kotlinx.android.synthetic.main.ads_banner.*

class CountryActivity : BaseActivity(), BaseViewListener<Server> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country)

        setupDetailActivity("")
        setupShowAdsInterstitial()
        setupShowAdsBanner(admob_adview)
        setupRecyclerView()

    }

    private fun setupRecyclerView() {
        val countryList = dbHelper.uniqueCountries
        val adapter = CountryViewAdapter()
        adapter.setupRequirement(this, this, countryList, R.layout.view_item_country)
        recycler_view_country.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler_view_country.adapter = adapter

    }

    override fun onItemClicked(data: Server) {
        startActivity(Intent(this, VPNListActivity::class.java).putExtra(Constant.Variable.EXTRA_COUNTRY, data.countryShort))

    }

    override fun onItemLongClicked(data: Server) {

    }
}
