package com.frogobox.viprox.view.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.frogobox.viprox.R
import com.frogobox.viprox.base.adapter.BaseViewListener
import com.frogobox.viprox.base.ui.BaseActivity
import com.frogobox.viprox.helper.Constant
import com.frogobox.viprox.source.model.Server
import com.frogobox.viprox.view.adapter.CountryViewAdapter
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
        recycler_view_country.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recycler_view_country.adapter = adapter

    }

    override fun onItemClicked(data: Server) {
        startActivity(Intent(this, VPNListActivity::class.java).putExtra(Constant.Variable.EXTRA_COUNTRY, data.countryShort))

    }

    override fun onItemLongClicked(data: Server) {

    }
}
