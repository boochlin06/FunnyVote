package com.heaton.funnyvote.ui.about.aboutapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.MenuItem
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.utils.Util
import com.heaton.funnyvote.utils.Util.setupActionBar
import kotlinx.android.synthetic.main.activity_about_app.*

/**
 * Created by heaton on 2017/3/4.
 */

class AboutAppActivity : AppCompatActivity(), AboutAppContract.View {
    private var tracker: Tracker? = null
    private lateinit var presenter: AboutAppContract.Presenter

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)
        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker

        mainToolbar.title = getString(R.string.about_funnyvote)
        mainToolbar.setTitleTextColor(Color.WHITE)
        mainToolbar.elevation = 10f

        mainToolbar.setNavigationOnClickListener { finish() }
        setupActionBar(mainToolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val desc = getString(R.string.about_introduction_desc)
        txtAppDesc!!.text = Html.fromHtml(desc, FROM_HTML_MODE_LEGACY)
        presenter = AboutAppPresenter(this)
        btnShareApp.setOnClickListener { presenter.shareApp() }
    }

    public override fun onResume() {
        super.onResume()
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_ABOUT_FUNNYVOTE_APP)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun showShareApp() {
        Util.sendShareAppIntent(applicationContext)
    }

    override fun setPresenter(presenter: AboutAppContract.Presenter) {
        this.presenter = presenter
    }
}
