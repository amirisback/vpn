package com.frogobox.evpn.view.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.frogobox.evpn.R;
import com.frogobox.evpn.base.ui.BaseActivity;
import com.frogobox.evpn.source.model.Server;
import com.frogobox.evpn.util.ConnectionQuality;
import com.frogobox.evpn.util.CountriesNames;

import java.util.List;
import java.util.Map;


public class ServerListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<Server> serverList;
    private Context context;
    private Map<String, String> localeCountries;

    public ServerListAdapter(Context c, List<Server> serverList) {
        inflater = LayoutInflater.from(c);
        context = c;
        this.serverList =  serverList;
        localeCountries = CountriesNames.getCountries();
    }


    @Override
    public int getCount() {
        return serverList.size();
    }


    @Override
    public Server getItem(int position) {
        return serverList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {

        v = inflater.inflate(R.layout.recyclerview_item_vpn, parent, false);

        final Server server = getItem(position);

        String code = server.getCountryShort().toLowerCase();
        if (code.equals("do"))
            code = "dom";


        ((ImageView) v.findViewById(R.id.imageFlag))
                .setImageResource(
                        context.getResources().getIdentifier(code,
                                "drawable",
                                context.getPackageName()));
        ((ImageView) v.findViewById(R.id.imageConnect))
                .setImageResource(
                        context.getResources().getIdentifier(ConnectionQuality.getConnectIcon(server.getQuality()),
                                "drawable",
                                context.getPackageName()));

        ((TextView) v.findViewById(R.id.textHostName)).setText(server.getHostName());
        ((TextView) v.findViewById(R.id.textIP)).setText(server.getIp());
        ((TextView) v.findViewById(R.id.textCity)).setText(server.getCity());

        String localeCountryName = localeCountries.get(server.getCountryShort()) != null ?
                localeCountries.get(server.getCountryShort()) : server.getCountryLong();
        ((TextView) v.findViewById(R.id.textCountry)).setText(localeCountryName);

        if (new BaseActivity().getConnectedServer() != null && new BaseActivity().getConnectedServer().getHostName().equals(server.getHostName())) {
            v.setBackgroundColor(ContextCompat.getColor(context, R.color.activeServer));
        }

        if (server.getType() == 1) {
            v.setBackgroundColor(ContextCompat.getColor(context, R.color.additionalServer));
        }

        return v;
    }

}
