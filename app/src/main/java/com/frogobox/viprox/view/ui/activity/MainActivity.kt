package com.frogobox.viprox.view.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.frogobox.viprox.R
import com.frogobox.viprox.base.ui.BaseActivity
import com.frogobox.viprox.util.PropertiesService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ads_banner.*
import kotlinx.android.synthetic.main.toolbar_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar_main)
        setupShowAdsInterstitial()
        setupShowAdsBanner(admob_adview)
        setupViewFunction()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.toolbar_menu_about -> {
                baseStartActivity<AboutUsActivity>()
                true
            }

            R.id.toolbar_location -> {
                baseStartActivity<CountryActivity>()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewFunction() {
        btn_quick_connect.setOnClickListener { v: View? ->
            if (getRandomServer() != null) {
                newConnecting(getRandomServer(), fastConnection = true, autoConnection = true)
            } else {
                showToast(String.format(resources.getString(R.string.error_random_country), PropertiesService.getSelectedCountry()))
            }
        }

    }

}