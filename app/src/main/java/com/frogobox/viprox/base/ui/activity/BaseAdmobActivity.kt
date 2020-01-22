package com.frogobox.viprox.base.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.frogobox.viprox.helper.AdmobHelper.Banner.setupBanner
import com.frogobox.viprox.helper.AdmobHelper.Banner.showBanner
import com.frogobox.viprox.helper.AdmobHelper.Interstitial.setupInterstitial
import com.frogobox.viprox.helper.AdmobHelper.Publisher.setupPublisher
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.reward.RewardedVideoAd

/**
 * Created by Faisal Amir
 * FrogoBox Inc License
 * =========================================
 * Frogobox-VPN-Hero
 * Copyright (C) 26/11/2019.
 * All rights reserved
 * -----------------------------------------
 * Name     : Muhammad Faisal Amir
 * E-mail   : faisalamircs@gmail.com
 * Github   : github.com/amirisback
 * LinkedIn : linkedin.com/in/faisalamircs
 * -----------------------------------------
 * FrogoBox Software Industries
 * com.frogobox.evpn.base
 *
 */

open class BaseAdmobActivity : AppCompatActivity() {

    lateinit var mActivity: AppCompatActivity
    lateinit var mInterstitialAd: InterstitialAd
    lateinit var mRewardedVideoAd: RewardedVideoAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this
        setupAdmob()
    }

    private fun setupAdmob() {
        setupPublisher(this)
//        setupAdmobVideo(context)
    }

    fun setupShowAdsInterstitial() {
        mInterstitialAd = InterstitialAd(this)
        setupInterstitial(this, mInterstitialAd)
    }

    fun setupShowAdsBanner(mAdView : AdView) {
        setupBanner(mAdView)
        showBanner(mAdView)
    }

}