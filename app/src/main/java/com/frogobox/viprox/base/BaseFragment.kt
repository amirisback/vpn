package com.frogobox.viprox.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.frogobox.sdk.core.FrogoFragment

/**
 * Created by Faisal Amir
 * FrogoBox Inc License
 * =========================================
 * ImplementationAdmob
 * Copyright (C) 25/11/2019.
 * All rights reserved
 * -----------------------------------------
 * Name     : Muhammad Faisal Amir
 * E-mail   : faisalamircs@gmail.com
 * Github   : github.com/amirisback
 * LinkedIn : linkedin.com/in/faisalamircs
 * -----------------------------------------
 * FrogoBox Software Industries
 * com.frogobox.evpn.activity
 *
 */
abstract class BaseFragment<VB : ViewBinding> : FrogoFragment<VB>() {

    lateinit var mBaseActivity: BaseActivity<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBaseActivity = (activity as BaseActivity<*>)
    }

    protected fun setupShowAdsInterstitial() {
        mBaseActivity.setupShowAdsInterstitial()
    }

}