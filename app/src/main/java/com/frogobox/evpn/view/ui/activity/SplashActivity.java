package com.frogobox.evpn.view.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.frogobox.evpn.R;
import com.frogobox.evpn.base.ui.BaseActivity;
import com.frogobox.evpn.helper.Constant;
import com.frogobox.evpn.source.model.Server;
import com.frogobox.evpn.util.NetworkState;
import com.frogobox.evpn.util.PropertiesService;
import com.frogobox.evpn.util.Stopwatch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.frogobox.evpn.helper.Constant.Variable.BASE_FILE_NAME;
import static com.frogobox.evpn.helper.Constant.Variable.BASE_URL;
import static com.frogobox.evpn.helper.Constant.Variable.DOWNLOAD_PROGRESS;
import static com.frogobox.evpn.helper.Constant.Variable.LOADING_SUCCESS;
import static com.frogobox.evpn.helper.Constant.Variable.LOAD_ERROR;

public class SplashActivity extends BaseActivity {

    private static boolean loadStatus = false;

    private NumberProgressBar number_progress_bar;
    private TextView commentsText;
    private Handler updateHandler;
    private boolean premiumStage = true;

    private int percentDownload = 0;
    private Stopwatch stopwatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        number_progress_bar = findViewById(R.id.number_progress_bar);
        commentsText = findViewById(R.id.commentsText);

        if (NetworkState.isOnline()) {
            if (loadStatus) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                loadStatus = true;
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.network_error))
                    .setMessage(getString(R.string.network_error_message))
                    .setNegativeButton(getString(R.string.ok),
                            (dialog, id) -> {
                                dialog.cancel();
                                onBackPressed();
                            });
            builder.create().show();
        }

        if (getIntent().getBooleanExtra("firstPremiumLoad", false))
            (findViewById(R.id.loaderPremiumText)).setVisibility(View.VISIBLE);

        number_progress_bar.setMax(100);

        updateHandler = new Handler(msg -> {
            switch (msg.arg1) {
                case Constant.Variable.LOAD_ERROR: {
                    commentsText.setText(msg.arg2);
                    number_progress_bar.setProgress(100);
                }
                break;
                case Constant.Variable.DOWNLOAD_PROGRESS: {
                    commentsText.setText(R.string.downloading_csv_text);
                    number_progress_bar.setProgress(msg.arg2);

                }
                break;
                case Constant.Variable.PARSE_PROGRESS: {
                    commentsText.setText(R.string.parsing_csv_text);
                    number_progress_bar.setProgress(msg.arg2);
                }
                break;
                case Constant.Variable.LOADING_SUCCESS: {
                    commentsText.setText(R.string.successfully_loaded);
                    number_progress_bar.setProgress(100);
                    Message end = new Message();
                    end.arg1 = Constant.Variable.SWITCH_TO_RESULT;
                    updateHandler.sendMessageDelayed(end, 500);
                }
                break;
                case Constant.Variable.SWITCH_TO_RESULT: {

                    if (PropertiesService.getConnectOnStart()) {
                        Server randomServer = getRandomServer();
                        if (randomServer != null) {
                            newConnecting(randomServer, true, true);
                        } else {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        }
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                }
            }
            return true;
        });
        number_progress_bar.setProgress(0);


    }

    @Override
    protected void onResume() {
        super.onResume();
        downloadCSVFile(BASE_URL, BASE_FILE_NAME);
    }

    private void downloadCSVFile(String url, String fileName) {
        stopwatch = new Stopwatch();

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.download(url, getCacheDir().getPath(), fileName)
                .setTag("downloadCSV")
                .setPriority(Priority.MEDIUM)
                .setOkHttpClient(okHttpClient)
                .build()
                .setDownloadProgressListener(new DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long totalBytes) {
                        if (totalBytes <= 0) {
                            // when we dont know the file size, assume it is 1200000 bytes :)
                            totalBytes = 1200000;
                        }

                        percentDownload = (int) ((100 * bytesDownloaded) / totalBytes);

                        Message msg = new Message();
                        msg.arg1 = DOWNLOAD_PROGRESS;
                        msg.arg2 = percentDownload;
                        updateHandler.sendMessage(msg);
                    }
                })
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        parseCSVFile(BASE_FILE_NAME);
                    }

                    @Override
                    public void onError(ANError error) {
                        Message msg = new Message();
                        msg.arg1 = LOAD_ERROR;
                        msg.arg2 = R.string.network_error;
                        updateHandler.sendMessage(msg);
                    }
                });
    }

    private void parseCSVFile(String fileName) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(getCacheDir().getPath().concat("/").concat(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
            Message msg = new Message();
            msg.arg1 = LOAD_ERROR;
            msg.arg2 = R.string.csv_file_error;
            updateHandler.sendMessage(msg);
        }
        if (reader != null) {
            try {
                int startLine = 2;
                int type = 0;

                getDbHelper().clearTable();

                int counter = 0;
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (counter >= startLine) {
                        getDbHelper().putLine(line, type);
                    }
                    counter++;
                }

                Message end = new Message();
                end.arg1 = LOADING_SUCCESS;
                updateHandler.sendMessageDelayed(end, 200);

            } catch (Exception e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.arg1 = LOAD_ERROR;
                msg.arg2 = R.string.csv_file_error_parsing;
                updateHandler.sendMessage(msg);
            }
        }
    }
}
