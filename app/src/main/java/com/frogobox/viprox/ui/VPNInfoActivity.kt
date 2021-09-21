package com.frogobox.viprox.ui

import android.app.Activity
import android.content.*
import android.net.VpnService
import android.os.AsyncTask
import android.os.Bundle
import android.os.IBinder
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.frogobox.viprox.BuildConfig
import com.frogobox.viprox.R
import com.frogobox.viprox.base.BaseActivity
import com.frogobox.viprox.databinding.ActivityVpninfoBinding
import com.frogobox.viprox.util.Constant
import com.frogobox.viprox.util.Constant.Variable.BROADCAST_ACTION
import com.frogobox.viprox.util.Constant.Variable.EXTRA_AUTO_CONNECTION
import com.frogobox.viprox.util.Constant.Variable.EXTRA_FAST_CONNECTION
import com.frogobox.viprox.util.Constant.Variable.EXTRA_STATUS
import com.frogobox.viprox.util.Constant.Variable.START_VPN_PROFILE
import com.frogobox.viprox.source.model.Server
import com.frogobox.viprox.util.PropertiesService
import com.frogobox.viprox.util.TotalTraffic
import com.google.gson.Gson
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.core.*
import de.blinkt.openvpn.core.ConfigParser.ConfigParseError
import de.blinkt.openvpn.core.OpenVPNService.LocalBinder
import de.blinkt.openvpn.core.VpnStatus.ConnectionStatus
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class VPNInfoActivity : BaseActivity<ActivityVpninfoBinding>() {

    private lateinit var br: BroadcastReceiver
    private lateinit var trafficReceiver: BroadcastReceiver
    private lateinit var vpnProfile: VpnProfile
    private lateinit var currentServer: Server
    private lateinit var autoServer: Server
    private lateinit var waitConnection: WaitConnectionAsync

    private var autoConnection = false
    private var fastConnection = false
    private var statusConnection = false
    private var isBindedService = false
    private var firstData = true
    private var inBackground = false

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as LocalBinder
            mVPNService = binder.service
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mVPNService = null
        }
    }

    override fun setupViewBinding(): ActivityVpninfoBinding {
        return ActivityVpninfoBinding.inflate(layoutInflater)
    }

    override fun setupViewModel() {}

    override fun setupUI(savedInstanceState: Bundle?) {
        setupDetailActivity("")
        setupShowAdsBanner(findViewById(R.id.admob_adview))
        setupShowAdsInterstitial()

        binding.apply {
            serverTrafficInTotally.text =
                String.format(getString(R.string.traffic_in), TotalTraffic.getTotalTraffic()[0])
            serverTrafficOutTotally.text =
                String.format(getString(R.string.traffic_out), TotalTraffic.getTotalTraffic()[1])
            serverTrafficIn.text = ""
            serverTrafficOut.text = ""

            br = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    receiveStatus(intent)
                }
            }
            registerReceiver(br, IntentFilter(BROADCAST_ACTION))
            trafficReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    receiveTraffic(intent)
                }
            }
            registerReceiver(trafficReceiver, IntentFilter(TotalTraffic.TRAFFIC_ACTION))
            serverStatus.text = getString(R.string.server_not_connected)
            serverStatus.setTextColor(getColorRes(R.color.colorTextRed))
            initView(intent)
        }
    }

    private fun initView(intent: Intent) {
        binding.apply {
            autoConnection = intent.getBooleanExtra(EXTRA_AUTO_CONNECTION, false)
            fastConnection = intent.getBooleanExtra(EXTRA_FAST_CONNECTION, false)
            val tempServer = intent.getStringExtra(Server::class.java.canonicalName)
            currentServer = Gson().fromJson(tempServer, Server::class.java)

            Glide.with(this@VPNInfoActivity).load(Constant().getFlagImageUrl(currentServer)).into(ivServerFlag)
            val localeCountryName =
                if (localeCountries[currentServer.countryShort] != null) localeCountries[currentServer.countryShort] else currentServer.countryLong
            val speedValue = currentServer.speed!!.toInt() / 1048576
            var pingValue = 0
            if (currentServer.ping != "-") {
                pingValue = currentServer.ping!!.toInt()
            }

            tvCountryName.text = localeCountryName
            circleViewSpeed.text = "$speedValue Mbps"
            circleViewPing.text = "$pingValue Ms"
            circleViewSession.text = currentServer.numVpnSessions

            if (checkStatus()) {
                serverConnect.background =
                    ContextCompat.getDrawable(this@VPNInfoActivity, R.drawable.bg_button_connected)
                serverConnect.text = getString(R.string.server_btn_disconnect)
                serverStatus.text = VpnStatus.getLastCleanLogMessage(applicationContext)
                serverStatus.setTextColor(getColorRes(R.color.colorTextGreen))
            } else {
                serverConnect.background =
                    ContextCompat.getDrawable(this@VPNInfoActivity, R.drawable.bg_button_no_connected)
                serverConnect.text = getString(R.string.server_btn_connect)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initView(intent)
    }

    private fun receiveTraffic(intent: Intent) {
        binding.apply {
            if (checkStatus()) {
                var `in` = ""
                var out = ""
                if (firstData) {
                    firstData = false
                } else {
                    `in` = String.format(
                        getString(R.string.traffic_in),
                        intent.getStringExtra(TotalTraffic.DOWNLOAD_SESSION)
                    )
                    out = String.format(
                        getString(R.string.traffic_out),
                        intent.getStringExtra(TotalTraffic.UPLOAD_SESSION)
                    )
                }
                serverTrafficIn.text = `in`
                serverTrafficOut.text = out
                val inTotal = String.format(
                    getString(R.string.traffic_in),
                    intent.getStringExtra(TotalTraffic.DOWNLOAD_ALL)
                )
                serverTrafficInTotally.text = inTotal
                val outTotal = String.format(
                    getString(R.string.traffic_out),
                    intent.getStringExtra(TotalTraffic.UPLOAD_ALL)
                )
                serverTrafficOutTotally.text = outTotal
            }
        }
    }

    private fun receiveStatus(intent: Intent) {
        if (checkStatus()) {
            changeServerStatus(ConnectionStatus.valueOf(intent.getStringExtra(EXTRA_STATUS)!!))
            binding.serverStatus.text = VpnStatus.getLastCleanLogMessage(applicationContext)
        }
        if (intent.getStringExtra("detailstatus") == "NOPROCESS") {
            try {
                TimeUnit.SECONDS.sleep(1)
                if (!VpnStatus.isVPNActive()) prepareStopVPN()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        waitConnection.cancel(false)
        if (isTaskRoot) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun checkStatus(): Boolean {
        return if (connectedServer != null && connectedServer!!.hostName == currentServer.hostName) {
            VpnStatus.isVPNActive()
        } else false
    }

    private fun changeServerStatus(status: ConnectionStatus) {
        binding.apply {
            when (status) {
                ConnectionStatus.LEVEL_CONNECTED -> {
                    statusConnection = true
                    serverConnectingProgress.visibility = View.GONE
                    if (!inBackground) {
                        showToast("Connection VPN Succses")
                    }
                    serverConnect.background =
                        ContextCompat.getDrawable(this@VPNInfoActivity, R.drawable.bg_button_connected)
                    serverConnect.text = getString(R.string.server_btn_disconnect)
                    serverStatus.setTextColor(getColorRes(R.color.colorTextGreen))
                }
                ConnectionStatus.LEVEL_NOTCONNECTED -> {
                    serverConnect.background =
                        ContextCompat.getDrawable(this@VPNInfoActivity, R.drawable.bg_button_no_connected)
                    serverConnect.text = getString(R.string.server_btn_connect)
                }
                else -> {
                    serverConnect.background =
                        ContextCompat.getDrawable(this@VPNInfoActivity, R.drawable.bg_button_connected)
                    serverConnect.text = getString(R.string.server_btn_disconnect)
                    statusConnection = false
                    serverConnectingProgress.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun prepareVpn() {
        binding.apply {
            serverConnectingProgress.visibility = View.VISIBLE
            if (loadVpnProfile()) {
                waitConnection = WaitConnectionAsync()
                waitConnection.execute()
                serverConnect.background =
                    ContextCompat.getDrawable(this@VPNInfoActivity, R.drawable.bg_button_connected)
                serverConnect.text = getString(R.string.server_btn_disconnect)
                startVpn()
            } else {
                serverConnectingProgress.visibility = View.GONE
                showToast(getString(R.string.server_error_loading_profile))
            }
        }
    }

    fun serverOnClick(view: View) {
        if (view.id == R.id.serverConnect) {
            if (checkStatus()) {
                stopVpn()
            } else {
                prepareVpn()
            }
        }
    }

    private fun loadVpnProfile(): Boolean {
        val data: ByteArray
        data = try {
            Base64.decode(currentServer.configData, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        val cp = ConfigParser()
        val isr = InputStreamReader(ByteArrayInputStream(data))
        try {
            cp.parseConfig(isr)
            vpnProfile = cp.convertProfile()
            vpnProfile.mName = currentServer.countryLong
            ProfileManager.getInstance(this).addProfile(vpnProfile)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: ConfigParseError) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun prepareStopVPN() {
        binding.apply {
            if (!BuildConfig.DEBUG) {
                try {
                    var download = serverTrafficIn.text.toString()
                    download = download.substring(download.lastIndexOf(":") + 2)
                } catch (e: Exception) {
                }
            }
            statusConnection = false
            waitConnection.cancel(false)
            serverConnectingProgress.visibility = View.GONE
            serverStatus.text = getString(R.string.server_not_connected)
            serverStatus.setTextColor(getColorRes(R.color.colorTextRed))
            serverConnect.background =
                ContextCompat.getDrawable(this@VPNInfoActivity, R.drawable.bg_button_no_connected)
            serverConnect.text = getString(R.string.server_btn_connect)
            connectedServer = null
        }
    }

    private fun stopVpn() {
        ProfileManager.setConntectedVpnProfileDisconnected(this)
        if (mVPNService != null && mVPNService!!.management != null) mVPNService!!.management.stopVPN(false)
    }

    private fun startVpn() {
        connectedServer = currentServer
        hideCurrentConnection = true
        val intent = VpnService.prepare(this)
        if (intent != null) {
            VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
                    ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT)
            try {
                startActivityForResult(intent, START_VPN_PROFILE)
            } catch (ane: ActivityNotFoundException) {
                VpnStatus.logError(R.string.no_vpn_support_image)
            }
        } else {
            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            inBackground = false
            if (currentServer.city == null) getIpInfo(currentServer)
            if (connectedServer != null && currentServer.ip == connectedServer!!.ip) {
                hideCurrentConnection = true
                invalidateOptionsMenu()
            }
            val intent = Intent(this@VPNInfoActivity, OpenVPNService::class.java)
            intent.action = OpenVPNService.START_SERVICE
            isBindedService = bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
            if (checkStatus()) {
                try {
                    TimeUnit.SECONDS.sleep(1)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                if (!checkStatus()) {
                    connectedServer = null
                    serverConnect.text = getString(R.string.server_btn_connect)
                    serverConnect.background =
                        ContextCompat.getDrawable(this@VPNInfoActivity, R.drawable.bg_button_no_connected)
                    serverStatus.setText(R.string.server_not_connected)
                    serverStatus.setTextColor(getColorRes(R.color.colorTextRed))
                }
            } else {
                serverConnect.text = getString(R.string.server_btn_connect)
                serverConnect.background =
                    ContextCompat.getDrawable(this@VPNInfoActivity, R.drawable.bg_button_no_connected)
                if (autoConnection) {
                    prepareVpn()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        inBackground = true
        if (isBindedService) {
            isBindedService = false
            unbindService(mConnection)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(br)
        unregisterReceiver(trafficReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                START_VPN_PROFILE -> VPNLaunchHelper.startOpenVpn(vpnProfile, baseContext)
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.try_another_server_text))
                .setPositiveButton(getString(R.string.try_another_server_ok)
                ) { dialog: DialogInterface, id: Int ->
                    dialog.cancel()
                    stopVpn()
                    autoServer = dbHelper.getSimilarServer(currentServer.countryLong, currentServer.ip)
                    newConnecting(autoServer, false, true)
                }
                .setNegativeButton(getString(R.string.try_another_server_no)
                ) { dialog: DialogInterface, id: Int ->
                    if (!statusConnection) {
                        waitConnection = WaitConnectionAsync()
                        waitConnection.execute()
                    }
                    dialog.cancel()
                }
        val alert = builder.create()
        alert.show()
    }

    private open inner class WaitConnectionAsync : AsyncTask<Void?, Void?, Void?>() {
        protected override fun doInBackground(vararg p0: Void?): Void? {
            try {
                TimeUnit.SECONDS.sleep(PropertiesService.getAutomaticSwitchingSeconds().toLong())
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            if (!statusConnection) {
                dbHelper.setInactive(currentServer.ip)
                if (fastConnection) {
                    stopVpn()
                    newConnecting(getRandomServer(), true, true)
                } else if (PropertiesService.getAutomaticSwitching()) {
                    if (!inBackground) showAlert()
                }
            }
        }
    }

    companion object {
        private var mVPNService: OpenVPNService? = null
    }

}