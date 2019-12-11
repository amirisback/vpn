package com.frogobox.vpnhero.view.adapter

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.frogobox.vpnhero.base.adapter.BaseViewAdapter
import com.frogobox.vpnhero.base.adapter.BaseViewHolder
import com.frogobox.vpnhero.helper.Constant
import com.frogobox.vpnhero.source.model.Server
import com.frogobox.vpnhero.util.ConnectionQuality
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
            Glide.with(itemView.context).load(Constant().getFlagImageUrl(data)).into(iv_flag)
            iv_connect.setImageResource(mContext.resources.getIdentifier(ConnectionQuality.getConnectIcon(data.quality), "drawable", mContext.packageName))
            tv_host_name.text = data.hostName
            tv_ip.text = data.ip
            tv_city.text = data.city
        }

    }


}