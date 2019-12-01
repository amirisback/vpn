package com.frogobox.evpn.base.ui

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.frogobox.evpn.R
import com.frogobox.evpn.base.ui.activity.BaseAdmobActivity
import com.frogobox.evpn.base.util.BaseHelper
import com.frogobox.evpn.helper.Constant.Variable.EXTRA_AUTO_CONNECTION
import com.frogobox.evpn.helper.Constant.Variable.EXTRA_FAST_CONNECTION
import com.frogobox.evpn.source.local.DBHelper
import com.frogobox.evpn.source.model.Server
import com.frogobox.evpn.util.CountriesNames
import com.frogobox.evpn.util.PropertiesService
import com.frogobox.evpn.util.TotalTraffic
import com.frogobox.evpn.view.ui.activity.VPNInfoActivity
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
open class BaseActivity : BaseAdmobActivity() {

    var connectedServer: Server? = null
    protected var hideCurrentConnection = false
    protected var dbHelper = DBHelper(this)
    protected var localeCountries: MutableMap<String, String> = CountriesNames.getCountries()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        TotalTraffic.saveTotal()
    }

    protected fun setupCustomTitleToolbar(title: Int) {
        supportActionBar?.setTitle(title)
    }

    protected fun setupNoLimitStatBar() {
        val windows = window // in Activity's onCreate() for instance
        windows.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    protected fun setupChildFragment(frameId: Int, fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(frameId, fragment)
            commit()
        }
    }

    protected inline fun <reified ClassActivity> baseStartActivity() {
        this.startActivity(Intent(this, ClassActivity::class.java))
    }

    protected inline fun <reified ClassActivity, Model> baseStartActivity(
        extraKey: String,
        data: Model
    ) {
        val intent = Intent(this, ClassActivity::class.java)
        val extraData = BaseHelper().baseToJson(data)
        intent.putExtra(extraKey, extraData)
        this.startActivity(intent)
    }

    protected inline fun <reified Model> baseGetExtraData(extraKey: String): Model {
        val extraIntent = intent.getStringExtra(extraKey)
        val extraData = BaseHelper().baseFromJson<Model>(extraIntent)
        return extraData
    }

    protected fun checkExtra(extraKey: String): Boolean {
        return intent?.hasExtra(extraKey)!!
    }

    protected fun <Model> baseFragmentNewInstance(
            fragment: BaseFragment,
            argumentKey: String,
            extraDataResult: Model
    ) {
        fragment.baseNewInstance(argumentKey, extraDataResult)
    }


    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun setupDetailActivity(title: String) {
        setTitle(title)
        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_toolbar_back_home)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    R.color.colorBaseWhite
                )
            )
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

    protected fun setupEventEmptyView(view: View, isEmpty: Boolean) {
        if (isEmpty) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    protected fun setupEventProgressView(view: View, progress: Boolean) {
        if (progress) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
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
            intent.putExtra(Server::class.java.canonicalName, BaseHelper().baseToJson(server))
            intent.putExtra(EXTRA_FAST_CONNECTION, fastConnection)
            intent.putExtra(EXTRA_AUTO_CONNECTION, autoConnection)
            startActivity(intent)
        }
    }

    protected open fun ipInfoResult(){

    }

    protected open fun getIpInfo(server: Server) {
        val serverList: MutableList<Server> = ArrayList()
        serverList.add(server)
        getIpInfoFromServerList(serverList)
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
                        if (dbHelper.setIpInfo(response, serverList)) {
                        }
                    }

                    override fun onError(error: ANError) {}
                })
    }

}