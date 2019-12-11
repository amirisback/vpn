package com.frogobox.vpnhero.view.ui.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.frogobox.vpnhero.BuildConfig;
import com.frogobox.vpnhero.R;
import com.frogobox.vpnhero.base.ui.BaseActivity;
import com.frogobox.vpnhero.source.model.Server;
import com.frogobox.vpnhero.util.PropertiesService;
import com.frogobox.vpnhero.util.Stopwatch;
import com.frogobox.vpnhero.util.TotalTraffic;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import at.grabner.circleprogress.CircleProgressView;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;

import static com.frogobox.vpnhero.helper.Constant.Variable.BROADCAST_ACTION;
import static com.frogobox.vpnhero.helper.Constant.Variable.EXTRA_AUTO_CONNECTION;
import static com.frogobox.vpnhero.helper.Constant.Variable.EXTRA_FAST_CONNECTION;
import static com.frogobox.vpnhero.helper.Constant.Variable.START_VPN_PROFILE;

public class VPNInfoActivity extends BaseActivity {

    private static OpenVPNService mVPNService;
    private static Stopwatch stopwatch;
    private static boolean filterAds = false;
    private static boolean defaultFilterAds = true;
    private CircleProgressView circleView;
    private BroadcastReceiver br;
    private BroadcastReceiver trafficReceiver;
    private VpnProfile vpnProfile;
    private Server currentServer = null;
    private Server autoServer;
    private Button unblockCheck;
    private Button serverConnect;
    private WaitConnectionAsync waitConnection;
    private ProgressBar serverConnectingProgress;
    private PopupWindow popupWindow;
    private RelativeLayout serverParentLayout;
    private TextView serverTrafficInTotally;
    private TextView serverTrafficOutTotally;
    private TextView serverTrafficOut;
    private TextView serverTrafficIn;
    private TextView serverStatus;
    private ImageButton bookmark;
    private boolean autoConnection;
    private boolean fastConnection;
    private boolean statusConnection = false;
    private boolean firstData = true;
    private boolean inBackground;
    private boolean isBindedService = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) service;
            mVPNService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mVPNService = null;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpninfo);

        serverParentLayout = findViewById(R.id.serverParentLayout);
        serverConnectingProgress = findViewById(R.id.serverConnectingProgress);
        serverStatus = findViewById(R.id.serverStatus);
        serverConnect = findViewById(R.id.serverConnect);

        serverTrafficInTotally = findViewById(R.id.serverTrafficInTotally);
        serverTrafficOutTotally = findViewById(R.id.serverTrafficOutTotally);
        serverTrafficIn = findViewById(R.id.serverTrafficIn);
        serverTrafficOut = findViewById(R.id.serverTrafficOut);

        setupDetailActivity("");

        setupShowAdsBanner(findViewById(R.id.admob_adview));
        setupShowAdsInterstitial();

        String totalIn = String.format(getResources().getString(R.string.traffic_in), TotalTraffic.getTotalTraffic().get(0));
        String totalOut = String.format(getResources().getString(R.string.traffic_out), TotalTraffic.getTotalTraffic().get(1));

        serverTrafficInTotally.setText(totalIn);
        serverTrafficOutTotally.setText(totalOut);
        serverTrafficIn.setText("");
        serverTrafficOut.setText("");

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receiveStatus(context, intent);
            }
        };

        registerReceiver(br, new IntentFilter(BROADCAST_ACTION));

        trafficReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receiveTraffic(context, intent);
            }
        };

        registerReceiver(trafficReceiver, new IntentFilter(TotalTraffic.TRAFFIC_ACTION));

        serverStatus.setText(R.string.server_not_connected);

        initView(getIntent());
    }


    private void initView(Intent intent) {

        autoConnection = intent.getBooleanExtra(EXTRA_AUTO_CONNECTION, false);
        fastConnection = intent.getBooleanExtra(EXTRA_FAST_CONNECTION, false);
        String tempServer = intent.getStringExtra(Server.class.getCanonicalName());
        currentServer = new Gson().fromJson(tempServer, Server.class);

        if (currentServer == null) {
            if (getConnectedServer() != null) {
                currentServer = getConnectedServer();
            } else {
                onBackPressed();
                return;
            }
        }


        String code = currentServer.getCountryShort().toLowerCase();
        if (code.equals("do"))
            code = "dom";

        ((ImageView) findViewById(R.id.ivServerFlag))
                .setImageResource(
                        getResources().getIdentifier(code,
                                "drawable",
                                getPackageName()));


        String localeCountryName = getLocaleCountries().get(currentServer.getCountryShort()) != null ?
                getLocaleCountries().get(currentServer.getCountryShort()) : currentServer.getCountryLong();

        TextView elapse = findViewById(R.id.tv_country_name);
        elapse.setText(localeCountryName);


        double speedValue = (double) Integer.parseInt(currentServer.getSpeed()) / 1048576;
        speedValue = new BigDecimal(speedValue).setScale(3, RoundingMode.UP).doubleValue();


        circleView = findViewById(R.id.circleViewSpeed);
        circleView.setOnProgressChangedListener(value -> {

        });
        circleView.setValue(Integer.parseInt(currentServer.getSpeed()) / 1048576);
        circleView.setUnit("Mbps");


        CircleProgressView circleView3 = findViewById(R.id.circleViewPing);
        circleView3.setOnProgressChangedListener(value -> {

        });
        if (currentServer.getPing().equals("-")) {
            circleView3.setValue(0);
            circleView3.setUnit("Ms");
        } else {
            circleView3.setValue(Integer.parseInt(currentServer.getPing()));
            circleView3.setUnit("Ms");
        }
        CircleProgressView circleView2 = findViewById(R.id.circleViewSession);
        circleView2.setOnProgressChangedListener(value -> {

        });

        circleView2.setValue(Integer.parseInt(currentServer.getNumVpnSessions()));
        if (checkStatus()) {
            serverConnect.setBackground(getResources().getDrawable(R.drawable.button3));
            serverConnect.setText(getString(R.string.server_btn_disconnect));
            ((TextView) findViewById(R.id.serverStatus)).setText(VpnStatus.getLastCleanLogMessage(getApplicationContext()));
        } else {
            serverConnect.setBackground(getResources().getDrawable(R.drawable.button2));
            serverConnect.setText(getString(R.string.server_btn_connect));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        initView(intent);
    }

    private void receiveTraffic(Context context, Intent intent) {
        if (checkStatus()) {
            String in = "";
            String out = "";
            if (firstData) {
                firstData = false;
            } else {
                in = String.format(getResources().getString(R.string.traffic_in),
                        intent.getStringExtra(TotalTraffic.DOWNLOAD_SESSION));
                out = String.format(getResources().getString(R.string.traffic_out),
                        intent.getStringExtra(TotalTraffic.UPLOAD_SESSION));
            }

            serverTrafficIn.setText(in);
            serverTrafficOut.setText(out);

            String inTotal = String.format(getResources().getString(R.string.traffic_in),
                    intent.getStringExtra(TotalTraffic.DOWNLOAD_ALL));
            serverTrafficInTotally.setText(inTotal);

            String outTotal = String.format(getResources().getString(R.string.traffic_out),
                    intent.getStringExtra(TotalTraffic.UPLOAD_ALL));
            serverTrafficOutTotally.setText(outTotal);
        }
    }

    private void receiveStatus(Context context, Intent intent) {
        if (checkStatus()) {
            changeServerStatus(VpnStatus.ConnectionStatus.valueOf(intent.getStringExtra("status")));
            serverStatus.setText(VpnStatus.getLastCleanLogMessage(getApplicationContext()));
        }

        if (intent.getStringExtra("detailstatus").equals("NOPROCESS")) {
            try {
                TimeUnit.SECONDS.sleep(1);
                if (!VpnStatus.isVPNActive())
                    prepareStopVPN();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (waitConnection != null)
            waitConnection.cancel(false);

        if (isTaskRoot()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private boolean checkStatus() {
        if (getConnectedServer() != null && getConnectedServer().getHostName().equals(currentServer.getHostName())) {
            return VpnStatus.isVPNActive();
        }

        return false;
    }

    private void changeServerStatus(VpnStatus.ConnectionStatus status) {
        switch (status) {
            case LEVEL_CONNECTED:
                statusConnection = true;
                serverConnectingProgress.setVisibility(View.GONE);

                if (!inBackground) {

                    chooseAction();

                }
                serverConnect.setBackground(getResources().getDrawable(R.drawable.button3));
                serverConnect.setText(getString(R.string.server_btn_disconnect));
                break;
            case LEVEL_NOTCONNECTED:
                serverConnect.setBackground(getResources().getDrawable(R.drawable.button2));
                serverConnect.setText(getString(R.string.server_btn_connect));
                break;
            default:
                serverConnect.setBackground(getResources().getDrawable(R.drawable.button3));
                serverConnect.setText(getString(R.string.server_btn_disconnect));
                statusConnection = false;
                serverConnectingProgress.setVisibility(View.VISIBLE);
        }
    }

    private void prepareVpn() {
        serverConnectingProgress.setVisibility(View.VISIBLE);
        if (loadVpnProfile()) {
            waitConnection = new WaitConnectionAsync();
            waitConnection.execute();
            serverConnect.setBackground(getResources().getDrawable(R.drawable.button3));
            serverConnect.setText(getString(R.string.server_btn_disconnect));
            startVpn();
        } else {
            serverConnectingProgress.setVisibility(View.GONE);
            Toast.makeText(this, getString(R.string.server_error_loading_profile), Toast.LENGTH_SHORT).show();
        }
    }

    public void serverOnClick(View view) {
        if (view.getId() == R.id.serverConnect) {
            if (checkStatus()) {
                stopVpn();
            } else {
                prepareVpn();
            }
        }
    }

    private boolean loadVpnProfile() {
        byte[] data;
        try {
            data = Base64.decode(currentServer.getConfigData(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        ConfigParser cp = new ConfigParser();
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(data));
        try {
            cp.parseConfig(isr);
            vpnProfile = cp.convertProfile();
            vpnProfile.mName = currentServer.getCountryLong();

            ProfileManager.getInstance(this).addProfile(vpnProfile);
        } catch (IOException | ConfigParser.ConfigParseError e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void prepareStopVPN() {
        if (!BuildConfig.DEBUG) {
            try {
                String download = serverTrafficIn.getText().toString();
                download = download.substring(download.lastIndexOf(":") + 2);

            } catch (Exception e) {

            }
        }

        statusConnection = false;
        if (waitConnection != null)
            waitConnection.cancel(false);
        serverConnectingProgress.setVisibility(View.GONE);
        serverStatus.setText(R.string.server_not_connected);
        serverConnect.setBackground(getResources().getDrawable(R.drawable.button2));
        serverConnect.setText(getString(R.string.server_btn_connect));
        setConnectedServer(null);
    }

    private void stopVpn() {
        ProfileManager.setConntectedVpnProfileDisconnected(this);
        if (mVPNService != null && mVPNService.getManagement() != null)
            mVPNService.getManagement().stopVPN(false);

    }

    private void startVpn() {
        stopwatch = new Stopwatch();
        setConnectedServer(currentServer);
        setHideCurrentConnection(true);

        Intent intent = VpnService.prepare(this);

        if (intent != null) {
            VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
                    VpnStatus.ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
            try {
                startActivityForResult(intent, START_VPN_PROFILE);
            } catch (ActivityNotFoundException ane) {
                VpnStatus.logError(R.string.no_vpn_support_image);
            }
        } else {
            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        inBackground = false;

        if (currentServer.getCity() == null)
            getIpInfo(currentServer);

        if (getConnectedServer() != null && currentServer.getIp().equals(getConnectedServer().getIp())) {
            setHideCurrentConnection(true);
            invalidateOptionsMenu();
        }

        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        isBindedService = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (checkStatus()) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!checkStatus()) {
                setConnectedServer(null);
                serverConnect.setText(getString(R.string.server_btn_connect));
                serverConnect.setBackground(getResources().getDrawable(R.drawable.button2));
                serverStatus.setText(R.string.server_not_connected);
            }
        } else {
            serverConnect.setText(getString(R.string.server_btn_connect));
            serverConnect.setBackground(getResources().getDrawable(R.drawable.button2));
            if (autoConnection) {
                prepareVpn();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        inBackground = true;

        if (isBindedService) {
            isBindedService = false;
            unbindService(mConnection);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
        unregisterReceiver(trafficReceiver);
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case START_VPN_PROFILE:
                    VPNLaunchHelper.startOpenVpn(vpnProfile, getBaseContext());
                    break;

            }
        }
    }

    private void chooseAction() {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.popup_connected, null);

        popupWindow = new PopupWindow(
                view,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );

        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        Button successPopUpBtnPlayMarket = view.findViewById(R.id.successPopUpBtnPlayMarket);
        successPopUpBtnPlayMarket.setOnClickListener(v -> {
            final String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });


        (view.findViewById(R.id.successPopUpBtnBrowser)).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com"))));
        (view.findViewById(R.id.successPopUpBtnDesktop)).setOnClickListener(v -> {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        });
        view.findViewById(R.id.successPopUpBtnClose).setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.showAtLocation(serverParentLayout, Gravity.CENTER, 0, 0);

    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.try_another_server_text))
                .setPositiveButton(getString(R.string.try_another_server_ok),
                        (dialog, id) -> {
                            dialog.cancel();
                            stopVpn();
                            autoServer = getDbHelper().getSimilarServer(currentServer.getCountryLong(), currentServer.getIp());
                            if (autoServer != null) {
                                newConnecting(autoServer, false, true);
                            } else {
                                onBackPressed();
                            }
                        })
                .setNegativeButton(getString(R.string.try_another_server_no),
                        (dialog, id) -> {
                            if (!statusConnection) {
                                waitConnection = new WaitConnectionAsync();
                                waitConnection.execute();
                            }
                            dialog.cancel();
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class WaitConnectionAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                TimeUnit.SECONDS.sleep(PropertiesService.getAutomaticSwitchingSeconds());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!statusConnection) {
                if (currentServer != null)
                    getDbHelper().setInactive(currentServer.getIp());

                if (fastConnection) {
                    stopVpn();
                    newConnecting(getRandomServer(), true, true);
                } else if (PropertiesService.getAutomaticSwitching()) {
                    if (!inBackground)
                        showAlert();
                }
            }
        }
    }
}
