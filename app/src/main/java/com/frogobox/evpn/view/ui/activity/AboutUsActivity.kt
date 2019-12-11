package com.frogobox.evpn.view.ui.activity

import android.os.Bundle
import com.frogobox.evpn.R
import com.frogobox.evpn.base.ui.BaseActivity

class AboutUsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        setupDetailActivity("")
    }
}
