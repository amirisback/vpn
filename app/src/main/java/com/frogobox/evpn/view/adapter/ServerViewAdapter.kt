package com.frogobox.evpn.view.adapter

import android.view.View
import android.view.ViewGroup
import com.frogobox.evpn.base.adapter.BaseViewAdapter
import com.frogobox.evpn.base.adapter.BaseViewHolder
import com.frogobox.evpn.source.model.Server
import com.frogobox.evpn.util.ConnectionQuality
import com.frogobox.evpn.util.CountriesNames
import kotlinx.android.synthetic.main.view_item_server.view.*

/**
 * Created by Faisal Amir
 * FrogoBox Inc License
 * =========================================
 * Frogobox-VPN-Hero
 * Copyright (C) 30/11/2019.
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
class ServerViewAdapter : BaseViewAdapter<Server>() {

    private val localeCountries: Map<String, String> = CountriesNames.getCountries()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Server> {
        return ServerViewHolder(viewLayout(parent))
    }

    inner class ServerViewHolder(view: View) : BaseViewHolder<Server>(view) {

        private val iv_flag = view.imageFlag
        private val iv_connect = view.imageConnect
        private val tv_host_name = view.textHostName
        private val tv_ip = view.textIP
        private val tv_city = view.textCity

        override fun initComponent(data: Server) {
            super.initComponent(data)

            var code: String = data.countryShort!!.toLowerCase()
            if (code == "do") code = "dom"

            iv_flag.setImageResource(mContext.resources.getIdentifier(code, "drawable", mContext.packageName))
            iv_connect.setImageResource(mContext.resources.getIdentifier(ConnectionQuality.getConnectIcon(data.quality), "drawable", mContext.packageName))
            tv_host_name.text = data.hostName
            tv_ip.text = data.ip
            tv_city.text = data.city
        }

    }


}