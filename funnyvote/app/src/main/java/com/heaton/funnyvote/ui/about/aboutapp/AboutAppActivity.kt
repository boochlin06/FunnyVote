package com.heaton.funnyvote.ui.about.aboutapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.databinding.ActivityAboutAppBinding
import com.heaton.funnyvote.utils.Util

class AboutAppActivity : AppCompatActivity(), AboutAppContract.View {
    private lateinit var binding: ActivityAboutAppBinding
    private var tracker: Tracker? = null
    private lateinit var presenter: AboutAppContract.Presenter

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker

        binding.mainToolbar.title = getString(R.string.about_funnyvote)
        binding.mainToolbar.setTitleTextColor(Color.WHITE)
        binding.mainToolbar.elevation = 10f

        binding.mainToolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val desc = getString(R.string.about_introduction_desc)
        binding.txtAppDesc.text = Html.fromHtml(desc, FROM_HTML_MODE_LEGACY)
        presenter = AboutAppPresenter(this)
        binding.btnShareApp.setOnClickListener { presenter.shareApp() }
    }

    public override fun onResume() {
        super.onResume()
        tracker?.setScreenName(AnalyzticsTag.SCREEN_ABOUT_FUNNYVOTE_APP)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
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
