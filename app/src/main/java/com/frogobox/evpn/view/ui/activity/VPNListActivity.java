package com.frogobox.evpn.view.ui.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.frogobox.evpn.R;
import com.frogobox.evpn.base.ui.BaseActivity;
import com.frogobox.evpn.source.model.Server;
import com.frogobox.evpn.view.adapter.ServerListAdapter;

import java.util.List;

import de.blinkt.openvpn.core.VpnStatus;

import static com.frogobox.evpn.helper.Constant.Variable.EXTRA_COUNTRY;

public class VPNListActivity extends BaseActivity {

    private ListView list;
    private ServerListAdapter serverListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpnlist);

        setSupportActionBar(findViewById(R.id.toolbar_main));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);

        upArrow.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        setupShowAdsBanner(findViewById(R.id.admob_adview));
        setupShowAdsInterstitial();

        if (!VpnStatus.isVPNActive())
            setConnectedServer(null);

        list = findViewById(R.id.list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        buildList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void ipInfoResult() {
        serverListAdapter.notifyDataSetChanged();
    }

    private void buildList() {
        String country = getIntent().getStringExtra(EXTRA_COUNTRY);
        final List<Server> serverList = getDbHelper().getServersByCountryCode(country);
        serverListAdapter = new ServerListAdapter(this, serverList);

        TextView elapse = findViewById(R.id.elapse);
        elapse.setText(country);

        String code = getIntent().getStringExtra(EXTRA_COUNTRY).toLowerCase();
        if (code.equals("do"))
            code = "dom";

        ((ImageView) findViewById(R.id.imgv))
                .setImageResource(
                        getResources().getIdentifier(code,
                                "drawable",
                                getPackageName()));

        list.setAdapter(serverListAdapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Server server = serverList.get(position);
            Intent intent = new Intent(VPNListActivity.this, VPNInfoActivity.class);
            intent.putExtra(Server.class.getCanonicalName(), server);
            VPNListActivity.this.startActivity(intent);
        });

        getIpInfoFromServerList(serverList);
    }
}