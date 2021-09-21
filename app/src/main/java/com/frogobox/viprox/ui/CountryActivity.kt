package com.frogobox.viprox.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.frogobox.recycler.core.FrogoLayoutManager
import com.frogobox.recycler.core.IFrogoBuilderRvBinding
import com.frogobox.viprox.base.BaseActivity
import com.frogobox.viprox.databinding.ActivityCountryBinding
import com.frogobox.viprox.databinding.ViewItemCountryBinding
import com.frogobox.viprox.util.Constant
import com.frogobox.viprox.source.model.Server
import com.frogobox.viprox.util.CountriesNames

class CountryActivity : BaseActivity<ActivityCountryBinding>() {

    override fun setupViewBinding(): ActivityCountryBinding {
        return ActivityCountryBinding.inflate(layoutInflater)
    }

    override fun setupViewModel() {

    }

    override fun setupUI(savedInstanceState: Bundle?) {
        setupDetailActivity("")
        setupShowAdsInterstitial()
        setupShowAdsBanner(binding.ads.admobAdview)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        binding.recyclerViewCountry.builderBinding(object :
            IFrogoBuilderRvBinding<Server, ViewItemCountryBinding> {

            override fun onItemClicked(data: Server) {
                val intent = Intent(this@CountryActivity, VPNListActivity::class.java)
                intent.putExtra(Constant.Variable.EXTRA_COUNTRY, data.countryShort)
                startActivity(intent)
            }

            override fun onItemLongClicked(data: Server) {}

            override fun setViewBinding(parent: ViewGroup): ViewItemCountryBinding {
                return ViewItemCountryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            }

            override fun setupData(): List<Server> {
                return dbHelper.uniqueCountries
            }

            override fun setupInitComponent(binding: ViewItemCountryBinding, data: Server) {
                binding.apply {
                    val localeCountries: MutableMap<String, String> = CountriesNames.getCountries()
                    val localeCountryName =
                        if (localeCountries[data.countryShort] != null) localeCountries[data.countryShort] else data.countryLong

                    tvCountryName.text = localeCountryName
                    Glide.with(binding.root.context).load(Constant().getFlagImageUrl(data))
                        .into(ivServerFlag)
                }
            }

            override fun setupLayoutManager(context: Context): RecyclerView.LayoutManager {
                return FrogoLayoutManager.staggeredGridLayout(2)
            }
        })

    }

}
