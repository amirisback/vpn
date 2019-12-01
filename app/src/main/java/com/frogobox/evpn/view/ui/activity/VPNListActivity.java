package com.frogobox.evpn.view.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.frogobox.evpn.R;
import com.frogobox.evpn.base.adapter.BaseViewListener;
import com.frogobox.evpn.base.ui.BaseActivity;
import com.frogobox.evpn.source.model.Server;
import com.frogobox.evpn.view.adapter.ServerViewAdapter;

import java.util.List;

import de.blinkt.openvpn.core.VpnStatus;

import static com.frogobox.evpn.helper.Constant.Variable.EXTRA_COUNTRY;

public class VPNListActivity extends BaseActivity implements BaseViewListener<Server> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpnlist);

        setupDetailActivity("");

        setupShowAdsBanner(findViewById(R.id.admob_adview));
        setupShowAdsInterstitial();

        String country = getIntent().getStringExtra(EXTRA_COUNTRY);
        String code = getIntent().getStringExtra(EXTRA_COUNTRY).toLowerCase();
        if (code.equals("do"))
            code = "dom";

        TextView tv_country_name = findViewById(R.id.tv_country_name);
        ImageView imgv = findViewById(R.id.imgv);

        tv_country_name.setText(country);
        imgv.setImageResource(getResources().getIdentifier(code, "drawable", getPackageName()));

        if (!VpnStatus.isVPNActive())
            setConnectedServer(null);

        setupRecyclerView(country);

    }

    private void setupRecyclerView(String country) {

        List<Server> serverList = getDbHelper().getServersByCountryCode(country);

        ServerViewAdapter serverViewAdapter = new ServerViewAdapter();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        serverViewAdapter.setupRequirement(this, this, serverList, R.layout.recyclerview_item_vpn);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(serverViewAdapter);

        getIpInfoFromServerList(serverList);
    }

    @Override
    public void onItemClicked(Server data) {
        Intent intent = new Intent(VPNListActivity.this, VPNInfoActivity.class);
        intent.putExtra(Server.class.getCanonicalName(), data);
        VPNListActivity.this.startActivity(intent);
    }

    @Override
    public void onItemLongClicked(Server data) {

    }
}