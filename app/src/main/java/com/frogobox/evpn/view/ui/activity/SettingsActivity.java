package com.frogobox.evpn.view.ui.activity;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import androidx.appcompat.widget.Toolbar;

import com.frogobox.evpn.App;
import com.frogobox.evpn.R;
import com.frogobox.evpn.base.BaseActivity;
import com.frogobox.evpn.source.local.DBHelper;
import com.frogobox.evpn.source.model.Server;
import com.frogobox.evpn.util.CountriesNames;
import com.frogobox.evpn.util.PropertiesService;

import java.util.List;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        App application = (App) getApplication();
        Toolbar toolbar = findViewById(R.id.preferenceToolbar);

        toolbar.setTitle(R.string.app_name);
        toolbar.setNavigationOnClickListener(v -> finish());
        getFragmentManager().beginTransaction().replace(R.id.preferenceContent, new MyPreferenceFragment()).commit();

        setupShowAdsBanner(findViewById(R.id.admob_adview));
        setupShowAdsInterstitial();

    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            DBHelper dbHelper = new DBHelper(getActivity().getApplicationContext());
            List<Server> countryList = dbHelper.getUniqueCountries();
            CharSequence entriesValues[] = new CharSequence[countryList.size()];
            CharSequence entries[] = new CharSequence[countryList.size()];

            for (int i = 0; i < countryList.size(); i++) {
                entriesValues[i] = countryList.get(i).getCountryLong();
                String localeCountryName = CountriesNames.getCountries().get(countryList.get(i).getCountryShort()) != null ?
                        CountriesNames.getCountries().get(countryList.get(i).getCountryShort()) :
                        countryList.get(i).getCountryLong();
                entries[i] = localeCountryName;
            }

            ListPreference listPreference = (ListPreference) findPreference("selectedCountry");
            if (entries.length == 0) {
                PreferenceCategory countryPriorityCategory = (PreferenceCategory) findPreference("countryPriorityCategory");
                getPreferenceScreen().removePreference(countryPriorityCategory);
            } else {
                listPreference.setEntries(entries);
                listPreference.setEntryValues(entriesValues);
                if (PropertiesService.getSelectedCountry() == null)
                    listPreference.setValueIndex(0);
            }
        }
    }
}
