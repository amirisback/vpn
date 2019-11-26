package com.frogobox.evpn.view.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.frogobox.evpn.R;
import com.frogobox.evpn.base.BaseActivity;
import com.frogobox.evpn.source.model.Server;
import com.frogobox.evpn.util.PropertiesService;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.frogobox.evpn.helper.Constant.Variable.EXTRA_COUNTRY;


public class MainActivity extends BaseActivity {

    private PopupWindow popupWindow;
    private RelativeLayout homeContextRL;
    private List<Server> countryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView centree = findViewById(R.id.centree);
        DecoView dynamicArcView2 = findViewById(R.id.dynamicArcView2);
        DecoView dynamicArcView3 = findViewById(R.id.dynamicArcView3);
        homeContextRL = findViewById(R.id.homeContextRL);
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        LinearLayout button1 = findViewById(R.id.homeBtnRandomConnection);
        LinearLayout button2 = findViewById(R.id.homeBtnChooseCountry);

        long totalServ = dbHelper.getCount();
        String totalServers = String.format(getResources().getString(R.string.total_servers), totalServ);

        countryList = dbHelper.getUniqueCountries();

        setSupportActionBar(toolbar);

        setupShowAdsInterstitial();
        setupShowAdsBanner(findViewById(R.id.admob_adview));

        checkState();

        centree.setText(totalServers);

        dynamicArcView3.setVisibility(View.VISIBLE);
        dynamicArcView2.setVisibility(View.GONE);

        dynamicArcView2.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, 100, 0)
                .setInterpolator(new AccelerateInterpolator())
                .build());

        SeriesItem seriesItem1 = new SeriesItem.Builder(Color.parseColor("#00000000"))
                .setRange(0, 100, 0)
                .setLineWidth(32f)
                .build();

        SeriesItem seriesItem2 = new SeriesItem.Builder(Color.parseColor("#ffffff"))
                .setRange(0, 100, 0)
                .setLineWidth(32f)
                .build();

        int series1Index2 = dynamicArcView2.addSeries(seriesItem2);
        int proc = new Random().nextInt(10) + 5;
        dynamicArcView2.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(0)
                .setDuration(600)
                .build());


        dynamicArcView2.addEvent(new DecoEvent.Builder(proc).setIndex(series1Index2).setDelay(2000).setListener(new DecoEvent.ExecuteEventListener() {
            @Override
            public void onEventStart(DecoEvent decoEvent) {
            }

            @Override
            public void onEventEnd(DecoEvent decoEvent) {
                long totalServ = dbHelper.getCount();
                String totalServers = String.format(getResources().getString(R.string.total_servers), totalServ);
                centree.setText(totalServers);
            }
        }).build());


        button1.setOnClickListener(v -> {

            Server randomServer = getRandomServer();
            if (randomServer != null) {
                newConnecting(randomServer, true, true);
            } else {
                String randomError = String.format(getResources().getString(R.string.error_random_country), PropertiesService.getSelectedCountry());
                Toast.makeText(MainActivity.this, randomError, Toast.LENGTH_LONG).show();
            }
            
        });

        button2.setOnClickListener(v -> {
            chooseCountry(initPopUp());
        });

    }

    private void checkState() {
        Button hello = findViewById(R.id.elapse2);
        if (connectedServer == null) {
            hello.setBackgroundResource(R.drawable.button2);
            hello.setText("No VPN Connected");
        } else {
            hello.setText("Connected");
            hello.setBackgroundResource(R.drawable.button3);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkState();
        invalidateOptionsMenu();
    }

    private View initPopUp() {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.choose_country, null);

        int widthWindow = 300;
        int heightWindow = 500;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            popupWindow = new PopupWindow(
                    view,
                    (int) (widthWindow * (float) 0.6),
                    (int) (heightWindow * (float) 0.8)
            );
        } else {
            popupWindow = new PopupWindow(
                    view,
                    (int) (widthWindow * (float) 0.8),
                    (int) (heightWindow * (float) 0.7)
            );
        }


        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        return view;
    }

    private void chooseCountry(@NotNull View view) {

        ListView lvCountry = view.findViewById(R.id.homeCountryList);

        final List<String> countryListName = new ArrayList<>();
        for (Server server : countryList) {
            String localeCountryName = localeCountries.get(server.getCountryShort()) != null ?
                    localeCountries.get(server.getCountryShort()) : server.getCountryLong();
            countryListName.add(localeCountryName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, countryListName);

        lvCountry.setAdapter(adapter);
        lvCountry.setOnItemClickListener((parent, view1, position, id) -> {
            popupWindow.dismiss();
            startActivity(new Intent(this, VPNListActivity.class).putExtra(EXTRA_COUNTRY, countryList.get(position).getCountryShort()));
        });

        popupWindow.showAtLocation(homeContextRL, Gravity.CENTER, 0, 0);
    }
    
}
