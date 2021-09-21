package com.frogobox.viprox.util

import com.frogobox.viprox.util.Constant.Variable.BASE_FLAG_IMAGE_SIZE
import com.frogobox.viprox.util.Constant.Variable.BASE_FLAG_IMAGE_URL
import com.frogobox.viprox.source.model.Server

/**
 * Created by Faisal Amir
 * FrogoBox Inc License
 * =========================================
 * Frogobox-VPN-Hero
 * Copyright (C) 25/11/2019.
 * All rights reserved
 * -----------------------------------------
 * Name     : Muhammad Faisal Amir
 * E-mail   : faisalamircs@gmail.com
 * Github   : github.com/amirisback
 * LinkedIn : linkedin.com/in/faisalamircs
 * -----------------------------------------
 * FrogoBox Software Industries
 * com.frogobox.evpn.helper
 *
 */
class Constant {

    object Variable {

        const val LOAD_ERROR = 0
        const val DOWNLOAD_PROGRESS = 1
        const val PARSE_PROGRESS = 2
        const val LOADING_SUCCESS = 3
        const val SWITCH_TO_RESULT = 4

        const val PRECENTAGE_MAX = 100

        const val START_VPN_PROFILE = 70

        const val BASE_URL = "http://www.vpngate.net/api/iphone/"
        const val BASE_FILE_NAME = "vpngate.csv"
        const val BROADCAST_ACTION = "de.blinkt.openvpn.VPN_STATUS"

        const val EXTRA_COUNTRY = "EXTRA_COUNTRY"
        const val EXTRA_FAST_CONNECTION = "EXTRA_FAST_CONNECTION"
        const val EXTRA_AUTO_CONNECTION = "EXTRA_AUTO_CONNECTION"
        const val EXTRA_FIRST_PREMIUM_LOAD = "firstPremiumLoad"
        const val EXTRA_STATUS = "status"

        const val BASE_FLAG_IMAGE_URL = "https://www.countryflags.io/"
        const val BASE_FLAG_IMAGE_SIZE = "/flat/64.png"
    }


    fun getFlagImageUrl(data: Server): String {
        var code: String = data.countryShort!!.toLowerCase()
        if (code == "do") code = "dom"
        return "$BASE_FLAG_IMAGE_URL$code$BASE_FLAG_IMAGE_SIZE"
    }


}