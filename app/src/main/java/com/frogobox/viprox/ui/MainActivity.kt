package com.frogobox.viprox.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.frogobox.viprox.R
import com.frogobox.viprox.base.BaseActivity
import com.frogobox.viprox.databinding.ActivityMainBinding
import com.frogobox.viprox.util.PropertiesService

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun setupViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun setupViewModel() {}

    override fun setupUI(savedInstanceState: Bundle?) {
        binding.apply {
            setSupportActionBar(toolbar.toolbarMain)
            setupShowAdsInterstitial()
            setupShowAdsBanner(ads.admobAdview)
            setupViewFunction()
        }
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
        binding.btnQuickConnect.setOnClickListener { v: View? ->
            if (getRandomServer() != null) {
                newConnecting(getRandomServer(), fastConnection = true, autoConnection = true)
            } else {
                showToast(
                    String.format(
                        resources.getString(R.string.error_random_country),
                        PropertiesService.getSelectedCountry()
                    )
                )
            }
        }

    }

}