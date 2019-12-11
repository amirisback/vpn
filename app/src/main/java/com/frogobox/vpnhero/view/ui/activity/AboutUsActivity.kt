package com.frogobox.vpnhero.view.ui.activity

import android.os.Bundle
import com.frogobox.vpnhero.R
import com.frogobox.vpnhero.base.ui.BaseActivity

class AboutUsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        setupDetailActivity("")
    }
}
