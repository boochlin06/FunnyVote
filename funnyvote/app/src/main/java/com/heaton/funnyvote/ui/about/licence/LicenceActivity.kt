package com.heaton.funnyvote.ui.about.licence

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.databinding.ActivityLicenceBinding
import java.util.ArrayList

class LicenceActivity : AppCompatActivity(), LicenceContract.View {
    private lateinit var binding: ActivityLicenceBinding
    private var tracker: Tracker? = null
    private var presenter: LicenceContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLicenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker
        val titles = resources.getStringArray(R.array.licences_title)
        val descs = resources.getStringArray(R.array.licences_desc)
        val licenceItemList = ArrayList<LicenceItem>()
        for (i in titles.indices) {
            licenceItemList.add(LicenceItem(titles[i], descs[i]))
        }
        binding.ryLicence.adapter = LicenceItemAdapter(licenceItemList)

        binding.mainToolbar.title = getString(R.string.about_licence)
        binding.mainToolbar.setTitleTextColor(Color.WHITE)
        binding.mainToolbar.elevation = 10f

        binding.mainToolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        presenter = LicencePresenter(this)
    }

    public override fun onResume() {
        super.onResume()
        tracker?.setScreenName(AnalyzticsTag.SCREEN_ABOUT_LICENCE)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setPresenter(presenter: LicenceContract.Presenter) {
        this.presenter = presenter
    }

    class LicenceItem(var title: String?, var desc: String?)
}
