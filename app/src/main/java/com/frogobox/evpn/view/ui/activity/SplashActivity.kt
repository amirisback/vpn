package com.frogobox.evpn.view.ui.activity

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.frogobox.evpn.BuildConfig
import com.frogobox.evpn.R
import com.frogobox.evpn.base.ui.BaseActivity
import com.frogobox.evpn.helper.Constant.Variable.BASE_FILE_NAME
import com.frogobox.evpn.helper.Constant.Variable.BASE_URL
import com.frogobox.evpn.helper.Constant.Variable.DOWNLOAD_PROGRESS
import com.frogobox.evpn.helper.Constant.Variable.EXTRA_FIRST_PREMIUM_LOAD
import com.frogobox.evpn.helper.Constant.Variable.LOADING_SUCCESS
import com.frogobox.evpn.helper.Constant.Variable.LOAD_ERROR
import com.frogobox.evpn.helper.Constant.Variable.PARSE_PROGRESS
import com.frogobox.evpn.helper.Constant.Variable.PRECENTAGE_MAX
import com.frogobox.evpn.helper.Constant.Variable.SWITCH_TO_RESULT
import com.frogobox.evpn.util.NetworkState
import com.frogobox.evpn.util.PropertiesService
import kotlinx.android.synthetic.main.activity_splash.*
import okhttp3.OkHttpClient
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.concurrent.TimeUnit

class SplashActivity : BaseActivity() {

    private var updateHandler: Handler? = null
    private val premiumStage = true
    private var percentDownload = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        runBackgroundProcess()
        tv_version_app.text = "v" + BuildConfig.VERSION_NAME

    }

    override fun onResume() {
        super.onResume()
        downloadCSVFile(BASE_URL, BASE_FILE_NAME)
    }

    private fun runBackgroundProcess() {
        if (NetworkState.isOnline()) {
            if (loadStatus) {
                baseStartActivity<MainActivity>()
                finish()
            } else {
                loadStatus = true
            }
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.network_error))
                    .setMessage(getString(R.string.network_error_message))
                    .setNegativeButton(getString(R.string.ok)
                    ) { dialog: DialogInterface, id: Int ->
                        dialog.cancel()
                        onBackPressed()
                    }
            builder.create().show()
        }
        if (intent.getBooleanExtra(EXTRA_FIRST_PREMIUM_LOAD, false)) loaderPremiumText.visibility = View.VISIBLE
        number_progress_bar.max = PRECENTAGE_MAX
        updateHandler = Handler(Handler.Callback { msg: Message ->
            when (msg.arg1) {
                LOAD_ERROR -> {
                    commentsText.setText(msg.arg2)
                    number_progress_bar.progress = PRECENTAGE_MAX
                }
                DOWNLOAD_PROGRESS -> {
                    commentsText.text = getString(R.string.downloading_csv_text)
                    number_progress_bar.progress = msg.arg2
                }
                PARSE_PROGRESS -> {
                    commentsText.setText(R.string.parsing_csv_text)
                    number_progress_bar.progress = msg.arg2
                }
                LOADING_SUCCESS -> {
                    commentsText.setText(R.string.successfully_loaded)
                    number_progress_bar.progress = PRECENTAGE_MAX
                    val end = Message()
                    end.arg1 = SWITCH_TO_RESULT
                    updateHandler!!.sendMessageDelayed(end, 500)
                }
                SWITCH_TO_RESULT -> {
                    if (PropertiesService.getConnectOnStart()) {
                        val randomServer = getRandomServer()
                        if (randomServer != null) {
                            newConnecting(randomServer, true, true)
                        } else {
                            baseStartActivity<MainActivity>()
                        }
                    } else {
                        baseStartActivity<MainActivity>()
                    }
                }
            }
            true
        })
        number_progress_bar.progress = 0
    }

    private fun downloadCSVFile(url: String, fileName: String) {
        val okHttpClient = OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
        AndroidNetworking.download(url, cacheDir.path, fileName)
                .setTag("downloadCSV")
                .setPriority(Priority.MEDIUM)
                .setOkHttpClient(okHttpClient)
                .build()
                .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                    var totalBytes = totalBytes
                    if (totalBytes <= 0) { // when we dont know the file size, assume it is 1200000 bytes :)
                        totalBytes = 1200000
                    }
                    percentDownload = (PRECENTAGE_MAX * bytesDownloaded / totalBytes).toInt()
                    val msg = Message()
                    msg.arg1 = DOWNLOAD_PROGRESS
                    msg.arg2 = percentDownload
                    updateHandler!!.sendMessage(msg)
                }
                .startDownload(object : DownloadListener {
                    override fun onDownloadComplete() {
                        parseCSVFile(BASE_FILE_NAME)
                    }

                    override fun onError(error: ANError) {
                        val msg = Message()
                        msg.arg1 = LOAD_ERROR
                        msg.arg2 = R.string.network_error
                        updateHandler!!.sendMessage(msg)
                    }
                })
    }

    private fun parseCSVFile(fileName: String) {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader(cacheDir.path + "/" + fileName))
        } catch (e: IOException) {
            e.printStackTrace()
            val msg = Message()
            msg.arg1 = LOAD_ERROR
            msg.arg2 = R.string.csv_file_error
            updateHandler!!.sendMessage(msg)
        }
        if (reader != null) {
            try {
                val startLine = 2
                val type = 0
                dbHelper.clearTable()
                var counter = 0
                var line: String? = null
                while (reader.readLine().also { line = it } != null) {
                    if (counter >= startLine) {
                        dbHelper.putLine(line, type)
                    }
                    counter++
                }
                val end = Message()
                end.arg1 = LOADING_SUCCESS
                updateHandler!!.sendMessageDelayed(end, 200)
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = Message()
                msg.arg1 = LOAD_ERROR
                msg.arg2 = R.string.csv_file_error_parsing
                updateHandler!!.sendMessage(msg)
            }
        }
    }

    companion object {
        private var loadStatus = false
    }
}