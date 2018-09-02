package com.heaton.funnyvote.ui.about.licence

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.utils.Util.setupActionBar
import kotlinx.android.synthetic.main.activity_licence.*
import java.util.*

/**
 * Created by heaton on 2017/3/2.
 */

class LicenceActivity : AppCompatActivity(), LicenceContract.View {
    private var tracker: Tracker? = null
    private var presenter: LicenceContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licence)
        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker
        val titles = resources.getStringArray(R.array.licences_title)
        val descs = resources.getStringArray(R.array.licences_desc)
        val licenceItemList = ArrayList<LicenceItem>()
        for (i in titles.indices) {
            licenceItemList.add(LicenceItem(titles[i], descs[i]))
        }
        ryLicence.adapter = LicenceItemAdapter(licenceItemList)

        mainToolbar.title = getString(R.string.about_licence)
        mainToolbar.setTitleTextColor(Color.WHITE)
        mainToolbar.elevation = 10f

        mainToolbar.setNavigationOnClickListener { finish() }
        setupActionBar(mainToolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        presenter = LicencePresenter(this)
    }

    public override fun onResume() {
        super.onResume()
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_ABOUT_LICENCE)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun setPresenter(presenter: LicenceContract.Presenter) {
        this.presenter = presenter
    }

    inner class LicenceItem(var title: String?, var desc: String?)
}