package com.heaton.funnyvote.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.ui.about.aboutapp.AboutAppActivity
import com.heaton.funnyvote.ui.about.authorinfo.AuthorInfoActivity
import com.heaton.funnyvote.ui.about.licence.LicenceActivity
import com.heaton.funnyvote.ui.about.problem.ProblemActivity
import com.heaton.funnyvote.ui.introduction.IntroductionActivity
import com.heaton.funnyvote.utils.Util
import kotlinx.android.synthetic.main.fragment_about.*

/**
 * Created by heaton on 2017/3/2.
 */

class AboutFragment : Fragment(), AboutContract.View, OnClickListener {

    private var tracker: Tracker? = null
    private lateinit var presenter: AboutContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_about, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = AboutPresenter(this)

        val application = requireActivity().application as FunnyVoteApplication
        tracker = application.defaultTracker
        try {
            var pinfo = application.packageManager.getPackageInfo(requireActivity().packageName, 0)
            val versionName = pinfo.versionName
            txtVersionName.text = versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        txtTutorial.setOnClickListener(this)
        txtAuthorInfo.setOnClickListener(this)
        txtLicence.setOnClickListener(this)
        txtProblem.setOnClickListener(this)
        txtUpdate.setOnClickListener(this)
        txtAppIntroduction.setOnClickListener(this)
        btnShareApp.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.txtTutorial -> presenter.IntentToIntroduction()
            R.id.txtAuthorInfo -> presenter.IntentToAuthorInfo()
            R.id.txtLicence -> presenter.IntentToLicence()
            R.id.txtProblem -> presenter.IntentToProblem()
            R.id.txtUpdate -> presenter.IntentToAppStore()
            R.id.txtAppIntroduction -> presenter.IntentToAbout()
            R.id.btnShareApp -> presenter.IntentToShareApp()
        }
    }

    override fun showIntroduction() {
        startActivity(Intent(activity, IntroductionActivity::class.java))
    }

    override fun showAuthorInfo() {
        startActivity(Intent(activity, AuthorInfoActivity::class.java))
    }

    override fun showLicence() {
        startActivity(Intent(activity, LicenceActivity::class.java))
    }

    override fun showProblem() {
        startActivity(Intent(activity, ProblemActivity::class.java))
    }

    override fun showAppStore() {
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_ABOUT_UPDATE_APP)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
        val appPackageName = activity!!.packageName

        try {
            val intent = Intent(Intent(Intent.ACTION_VIEW
                    , Uri.parse("market://details?id=$appPackageName")))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (anfe: ActivityNotFoundException) {
            val intent = Intent(Intent(Intent.ACTION_VIEW
                    , Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

    }

    override fun showAbout() {
        startActivity(Intent(requireContext(), AboutAppActivity::class.java))
    }

    override fun showShareApp() {
        Util.sendShareAppIntent(requireActivity())
    }

    override fun setPresenter(presenter: AboutContract.Presenter) {
        this.presenter = presenter
    }
}
