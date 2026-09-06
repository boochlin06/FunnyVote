package com.heaton.funnyvote.ui.about.authorinfo

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.databinding.ActivityAuthorInfoBinding

class AuthorInfoActivity : AppCompatActivity(), AuthorInfoContract.View {
    private lateinit var binding: ActivityAuthorInfoBinding
    private var tracker: Tracker? = null
    private lateinit var presenter: AuthorInfoContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthorInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker

        binding.mainToolbar.title = getString(R.string.about_author_info)
        binding.mainToolbar.setTitleTextColor(Color.WHITE)
        binding.mainToolbar.elevation = 10f

        binding.mainToolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        presenter = AuthorInfoPresenter(this)
    }

    public override fun onResume() {
        super.onResume()
        tracker?.setScreenName(AnalyzticsTag.SCREEN_ABOUT_AUTHOR)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setPresenter(presenter: AuthorInfoContract.Presenter) {
        this.presenter = presenter
    }
}
