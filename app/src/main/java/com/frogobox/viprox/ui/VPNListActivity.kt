package com.frogobox.viprox.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.frogobox.recycler.core.FrogoLayoutManager
import com.frogobox.recycler.core.IFrogoBuilderRvBinding
import com.frogobox.viprox.R
import com.frogobox.viprox.base.BaseActivity
import com.frogobox.viprox.databinding.ActivityVpnlistBinding
import com.frogobox.viprox.databinding.ViewItemServerBinding
import com.frogobox.viprox.util.Constant
import com.frogobox.viprox.util.Constant.Variable.EXTRA_COUNTRY
import com.frogobox.viprox.source.model.Server
import com.frogobox.viprox.util.ConnectionQuality
import de.blinkt.openvpn.core.VpnStatus

class VPNListActivity : BaseActivity<ActivityVpnlistBinding>() {

    override fun setupViewBinding(): ActivityVpnlistBinding {
        return ActivityVpnlistBinding.inflate(layoutInflater)
    }

    override fun setupViewModel() {}

    override fun setupUI(savedInstanceState: Bundle?) {
        setupShowAdsBanner(findViewById(R.id.admob_adview))
        setupShowAdsInterstitial()
        setupDetailActivity("")

        val country = intent.getStringExtra(EXTRA_COUNTRY)

        if (!VpnStatus.isVPNActive()) {
            connectedServer = null
        }

        if (country != null) {
            getIpInfoFromServerList(dbHelper.getServersByCountryCode(country))
            setupRecyclerView(country)
        }

    }

    private fun setupRecyclerView(country: String) {

        binding.recyclerView.builderBinding(object :
            IFrogoBuilderRvBinding<Server, ViewItemServerBinding> {
            override fun onItemClicked(data: Server) {
                baseStartActivity<VPNInfoActivity, Server>(Server::class.java.canonicalName!!, data)
            }

            override fun onItemLongClicked(data: Server) {}

            override fun setViewBinding(parent: ViewGroup): ViewItemServerBinding {
                return ViewItemServerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            }

            override fun setupData(): List<Server> {
                return dbHelper.getServersByCountryCode(country)
            }

            override fun setupInitComponent(binding: ViewItemServerBinding, data: Server) {
                binding.apply {
                    Glide.with(binding.root.context).load(Constant().getFlagImageUrl(data))
                        .into(imageFlag)
                    imageConnect.setImageResource(
                        binding.root.context.resources.getIdentifier(
                            ConnectionQuality.getConnectIcon(
                                data.quality
                            ), "drawable", binding.root.context.packageName
                        )
                    )
                    textHostName.text = data.hostName
                    textIP.text = data.ip
                    textCity.text = data.city
                }
            }

            override fun setupLayoutManager(context: Context): RecyclerView.LayoutManager {
                return FrogoLayoutManager.linearLayoutVertical(context)
            }
        })

    }

}