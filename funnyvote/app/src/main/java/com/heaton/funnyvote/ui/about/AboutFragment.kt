package com.heaton.funnyvote.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.databinding.FragmentAboutBinding
import com.heaton.funnyvote.ui.about.aboutapp.AboutAppActivity
import com.heaton.funnyvote.ui.about.authorinfo.AuthorInfoActivity
import com.heaton.funnyvote.ui.about.licence.LicenceActivity
import com.heaton.funnyvote.ui.about.problem.ProblemActivity
import com.heaton.funnyvote.ui.introduction.IntroductionActivity
import com.heaton.funnyvote.utils.Util

class AboutFragment : Fragment(), AboutContract.View, OnClickListener {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private var tracker: Tracker? = null
    private lateinit var presenter: AboutContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = AboutPresenter(this)

        val application = requireActivity().application as FunnyVoteApplication
        tracker = application.defaultTracker
        try {
            val pinfo = application.packageManager.getPackageInfo(requireActivity().packageName, 0)
            val versionName = pinfo.versionName
            binding.txtVersionName.text = versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        binding.txtTutorial.setOnClickListener(this)
        binding.txtAuthorInfo.setOnClickListener(this)
        binding.txtLicence.setOnClickListener(this)
        binding.txtProblem.setOnClickListener(this)
        binding.txtUpdate.setOnClickListener(this)
        binding.txtAppIntroduction.setOnClickListener(this)
        binding.btnShareApp.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View) {
        when (v.id) {
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
        tracker?.setScreenName(AnalyzticsTag.SCREEN_ABOUT_UPDATE_APP)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
        val appPackageName = requireActivity().packageName

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (anfe: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
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
