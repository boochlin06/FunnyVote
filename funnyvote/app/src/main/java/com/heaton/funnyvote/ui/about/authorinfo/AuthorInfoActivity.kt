package com.heaton.funnyvote.ui.about.authorinfo

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

import kotlinx.android.synthetic.main.activity_author_info.*

/**
 * Created by heaton on 2017/3/2.
 */

class AuthorInfoActivity : AppCompatActivity(), AuthorInfoContract.View {
    //private var mainToolbar: Toolbar? = null
    private var tracker: Tracker? = null
    private lateinit var presenter: AuthorInfoContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_author_info)
        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker
        //mainToolbar = findViewById<View>(R.id.mainToolbar) as Toolbar

        mainToolbar.title = getString(R.string.about_author_info)
        mainToolbar.setTitleTextColor(Color.WHITE)
        mainToolbar.elevation = 10f

        mainToolbar.setNavigationOnClickListener { finish() }
        setupActionBar(mainToolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        presenter = AuthorInfoPresenter(this)
    }

    public override fun onResume() {
        super.onResume()
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_ABOUT_AUTHOR)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun setPresenter(presenter: AuthorInfoContract.Presenter) {
        this.presenter = presenter
    }
}