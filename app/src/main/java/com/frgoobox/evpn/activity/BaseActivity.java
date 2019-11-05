package com.frgoobox.evpn.activity;

import android.content.Intent;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;


import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.frgoobox.evpn.App;
import com.frgoobox.evpn.R;
import com.frgoobox.evpn.database.DBHelper;
import com.frgoobox.evpn.model.Server;
import com.frgoobox.evpn.util.CountriesNames;
import com.frgoobox.evpn.util.PropertiesService;

import com.frgoobox.evpn.util.TotalTraffic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.blinkt.openvpn.core.VpnStatus;



public abstract class BaseActivity extends AppCompatActivity {

    private DrawerLayout fullLayout;
    private Toolbar toolbar;
    public static Server connectedServer = null;
    boolean hideCurrentConnection = false;

    int widthWindow ;
    int heightWindow;

    static DBHelper dbHelper;
    Map<String, String> localeCountries;

    @Override
    public void setContentView(int layoutResID)
    {

        fullLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = (FrameLayout) fullLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(fullLayout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (useToolbar()) {
            setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        if (useHomeButton()) {
            if (getSupportActionBar() != null){
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        dbHelper = new DBHelper(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        widthWindow = dm.widthPixels;
        heightWindow = dm.heightPixels;

        localeCountries = CountriesNames.getCountries();

        App application = (App) getApplication();
    }

    @Override
    protected void onPause() {
        super.onPause();
        TotalTraffic.saveTotal();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    protected boolean useToolbar()
    {
        return true;
    }

    protected boolean useHomeButton()
    {
        return true;
    }

    protected boolean useMenu()
    {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == R.id.actionCurrentServer
                    && (connectedServer == null || hideCurrentConnection || !VpnStatus.isVPNActive()))
                menu.getItem(i).setVisible(false);

        }

        return useMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;


            case R.id.actionSpeed:
                startActivity(new Intent(this, SpeedTestActivity.class));
                return true;
            case R.id.actionCurrentServer:
                if (connectedServer != null)
                    startActivity(new Intent(this, VPNInfoActivity.class));
                return true;

            case R.id.action_settings:
                sendTouchButton("Settings");
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    public Server getRandomServer() {
        Server randomServer;
        if (PropertiesService.getCountryPriority()) {
            randomServer = dbHelper.getGoodRandomServer(PropertiesService.getSelectedCountry());
        } else {
            randomServer = dbHelper.getGoodRandomServer(null);
        }
        return randomServer;
    }

    public void newConnecting(Server server, boolean fastConnection, boolean autoConnection) {
        if (server != null) {
            Intent intent = new Intent(this, VPNInfoActivity.class);
            intent.putExtra(Server.class.getCanonicalName(), server);
            intent.putExtra("fastConnection", fastConnection);
            intent.putExtra("autoConnection", autoConnection);
            startActivity(intent);
        }
    }

    public static void sendTouchButton(String button) {

    }

    protected void ipInfoResult() {}

    protected void getIpInfo(Server server) {
        List<Server> serverList = new ArrayList<Server>();
        serverList.add(server);
        getIpInfo(serverList);
    }

    protected void getIpInfo(final List<Server> serverList) {
        JSONArray jsonArray = new JSONArray();

        for (Server server : serverList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("query", server.getIp());
                jsonObject.put("lang", Locale.getDefault().getLanguage());

                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        AndroidNetworking.post(getString(R.string.url_check_ip_batch))
                .addJSONArrayBody(jsonArray)
                .setTag("getIpInfo")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (dbHelper.setIpInfo(response, serverList))
                            ipInfoResult();
                    }
                    @Override
                    public void onError(ANError error) {

                    }
                });
    }
}
