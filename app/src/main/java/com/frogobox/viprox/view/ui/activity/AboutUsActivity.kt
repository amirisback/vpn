package com.frogobox.viprox.view.ui.activity

import android.os.Bundle
import com.frogobox.viprox.R
import com.frogobox.viprox.base.ui.BaseActivity

class AboutUsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        setupDetailActivity("")
    }
}
