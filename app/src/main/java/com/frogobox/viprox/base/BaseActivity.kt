package com.frogobox.viprox.base

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.frogobox.admob.core.FrogoAdmob
import com.frogobox.sdk.core.FrogoActivity
import com.frogobox.viprox.R
import com.frogobox.viprox.util.Constant.Variable.EXTRA_AUTO_CONNECTION
import com.frogobox.viprox.util.Constant.Variable.EXTRA_FAST_CONNECTION
import com.frogobox.viprox.source.local.DBHelper
import com.frogobox.viprox.source.model.Server
import com.frogobox.viprox.util.CountriesNames
import com.frogobox.viprox.util.PropertiesService
import com.frogobox.viprox.util.TotalTraffic
import com.frogobox.viprox.ui.VPNInfoActivity
import com.google.android.gms.ads.AdView
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Created by Faisal Amir
 * FrogoBox Inc License
 * =========================================
 * ImplementationAdmob
 * Copyright (C) 27/11/2019.
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
abstract class BaseActivity<VB : ViewBinding> : FrogoActivity<VB>() {

    var connectedServer: Server? = null
    protected var hideCurrentConnection = false
    protected var dbHelper = DBHelper(this)
    protected var localeCountries: MutableMap<String, String> = CountriesNames.getCountries()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupAdmob()
    }

    private fun setupAdmob(){
        setPublisher()
        setBanner()
        setInterstitial()
    }

    private fun setupAdsPublisher(mPublisherId: String) {
        FrogoAdmob.setupPublisherID(mPublisherId)
        FrogoAdmob.Publisher.setupPublisher(this)
    }

    private fun setupAdsBanner(mAdUnitId: String) {
        FrogoAdmob.setupBannerAdUnitID(mAdUnitId)
    }

    private fun setupAdsInterstitial(mAdUnitId: String) {
        FrogoAdmob.setupInterstialAdUnitID(mAdUnitId)
        FrogoAdmob.Interstitial.setupInterstitial(this)
    }

    private fun setPublisher() {
        setupAdsPublisher(getString(R.string.admob_publisher_id))
    }

    private fun setBanner() {
        setupAdsBanner(getString(R.string.admob_banner))
    }

    private fun setInterstitial() {
        setupAdsInterstitial(getString(R.string.admob_interstitial))
    }

    fun setupShowAdsBanner(mAdView: AdView) {
        FrogoAdmob.Banner.setupBanner(mAdView)
        FrogoAdmob.Banner.showBanner(mAdView)
    }

    fun setupShowAdsInterstitial() {
        FrogoAdmob.Interstitial.showInterstitial(this)
    }

    override fun onPause() {
        super.onPause()
        TotalTraffic.saveTotal()
    }

    protected fun setupNoLimitStatBar() {
        val windows = window // in Activity's onCreate() for instance
        windows.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    protected fun getRandomServer(): Server? {
        return if (PropertiesService.getCountryPriority()) {
            dbHelper.getGoodRandomServer(PropertiesService.getSelectedCountry())
        } else {
            dbHelper.getGoodRandomServer(null)
        }
    }

    protected open fun newConnecting(server: Server?, fastConnection: Boolean, autoConnection: Boolean) {
        if (server != null) {
            val intent = Intent(this, VPNInfoActivity::class.java)
            intent.putExtra(Server::class.java.canonicalName, Gson().toJson(server))
            intent.putExtra(EXTRA_FAST_CONNECTION, fastConnection)
            intent.putExtra(EXTRA_AUTO_CONNECTION, autoConnection)
            startActivity(intent)
        }
    }

    protected open fun ipInfoResult(){

    }

    protected fun hasConnectedServer(): Boolean {
        return connectedServer != null
    }

    protected open fun getIpInfo(server: Server) {
        val serverList: MutableList<Server> = ArrayList()
        serverList.add(server)
        getIpInfoFromServerList(serverList)
    }

    fun getColorRes(res: Int): Int {
        return ContextCompat.getColor(this, res)
    }

    protected open fun getIpInfoFromServerList(serverList: List<Server>) {
        val jsonArray = JSONArray()
        for (server in serverList) {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("query", server.ip)
                jsonObject.put("lang", Locale.getDefault().language)
                jsonArray.put(jsonObject)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        AndroidNetworking.post(getString(R.string.url_check_ip_batch))
                .addJSONArrayBody(jsonArray)
                .setTag("getIpInfo")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONArray(object : JSONArrayRequestListener {
                    override fun onResponse(response: JSONArray) {
                        dbHelper.setIpInfo(response, serverList)
                        // TODO WHEN VPN CONNECTED
                    }

                    override fun onError(error: ANError) {}
                })
    }

}