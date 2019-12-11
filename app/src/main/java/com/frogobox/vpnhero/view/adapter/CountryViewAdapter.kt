package com.frogobox.vpnhero.view.adapter

import android.view.View
import android.view.ViewGroup
import com.frogobox.vpnhero.base.adapter.BaseViewAdapter
import com.frogobox.vpnhero.base.adapter.BaseViewHolder
import com.frogobox.vpnhero.source.model.Server
import com.frogobox.vpnhero.util.CountriesNames
import kotlinx.android.synthetic.main.view_item_country.view.*

/**
 * Created by Faisal Amir
 * FrogoBox Inc License
 * =========================================
 * Frogobox-VPN-Hero
 * Copyright (C) 02/12/2019.
 * All rights reserved
 * -----------------------------------------
 * Name     : Muhammad Faisal Amir
 * E-mail   : faisalamircs@gmail.com
 * Github   : github.com/amirisback
 * LinkedIn : linkedin.com/in/faisalamircs
 * -----------------------------------------
 * FrogoBox Software Industries
 * com.frogobox.evpn.view.adapter
 *
 */
class CountryViewAdapter : BaseViewAdapter<Server>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Server> {
        return CountryViewHolder(viewLayout(parent))
    }

    inner class CountryViewHolder(view: View) : BaseViewHolder<Server>(view) {

        private val tv_country = view.tv_country_name

        override fun initComponent(data: Server) {
            super.initComponent(data)

            val localeCountries: MutableMap<String, String> = CountriesNames.getCountries()
            val localeCountryName = if (localeCountries[data.countryShort] != null) localeCountries[data.countryShort] else data.countryLong
            tv_country.text = localeCountryName
        }
    }

}