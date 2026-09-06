package com.heaton.funnyvote.ui.main

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import at.grabner.circleprogress.TextMode
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar
import com.amulyakhare.textdrawable.TextDrawable
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.google.android.material.appbar.AppBarLayout
import com.heaton.funnyvote.FirstTimePref
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.databinding.FragmentMainPageTopBinding
import com.heaton.funnyvote.ui.createvote.CreateVoteActivity
import com.heaton.funnyvote.utils.Util
import java.util.ArrayList

class MainPageFragment : Fragment(), MainPageContract.MainPageView {

    private var _binding: FragmentMainPageTopBinding? = null
    private val binding get() = _binding!!

    private var promotionADMOB: View? = null
    private lateinit var tabsAdapter: TabsAdapter
    private var tracker: Tracker? = null
    private lateinit var pagePresenter: MainPageContract.Presenter
    private var hotsFragment: MainPageTabFragment? = null
    private var newsFragment: MainPageTabFragment? = null
    private var passwordDialog: AlertDialog? = null

    override val isPasswordDialogShowing: Boolean
        get() = passwordDialog != null && passwordDialog!!.isShowing

    override fun setPresenter(presenter: MainPageContract.Presenter) {
        this.pagePresenter = presenter
    }

    class PromotionType(val promotionType: Int, val promotion: Promotion) {
        companion object {
            const val PROM0TION_TYPE_ADMOB = 0
            const val PROMOTION_TYPE_FUNNY_VOTE = 1
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainPageTopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val application = requireActivity().application as FunnyVoteApplication
        tracker = application.defaultTracker

        binding.circleLoad.setTextMode(TextMode.TEXT)
        binding.circleLoad.isShowTextWhileSpinning = true
        binding.circleLoad.setFillCircleColor(ContextCompat.getColor(requireActivity(), R.color.md_amber_50))
        binding.circleLoad.setText(getString(R.string.vote_detail_circle_loading))

        binding.vpHeader.adapter = HeaderAdapter(ArrayList(), User())
        binding.vpHeader.currentItem = 0

        binding.tabLayoutMainPage.setupWithViewPager(binding.vpMainPage)

        binding.vpIndicator.setViewPager(binding.vpHeader)
        binding.vpHeader.interval = 100000
        binding.vpHeader.setScrollDurationFactor(5.0)

        binding.appBarMain.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                if (state == State.EXPANDED) {
                    _binding?.vpHeader?.startAutoScroll()
                } else if (state == State.COLLAPSED) {
                    _binding?.vpHeader?.stopAutoScroll()
                }
            }
        })

        tracker?.setScreenName(AnalyzticsTag.SCREEN_MAIN_HOT)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())

        binding.vpMainPage.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    tracker?.setScreenName(AnalyzticsTag.SCREEN_MAIN_HOT)
                } else if (position == 1) {
                    tracker?.setScreenName(AnalyzticsTag.SCREEN_MAIN_NEW)
                }
                tracker?.send(HitBuilders.ScreenViewBuilder().build())
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        ENABLE_PROMOTION_ADMOB = resources.getBoolean(R.bool.enable_promotion_admob)
        pagePresenter = MainPagePresenter(
            Injection.provideVoteDataRepository(requireContext()),
            Injection.provideUserRepository(requireContext()),
            Injection.providePromotionRepository(requireContext()),
            this
        )
        pagePresenter.start()
    }

    override fun showShareDialog(data: VoteData) {
        Util.sendShareIntent(requireContext(), data)
    }

    override fun showAuthorDetail(data: VoteData) {
        Util.sendPersonalDetailIntent(requireContext(), data)
    }

    override fun showCreateVote() {
        requireContext().startActivity(Intent(requireContext(), CreateVoteActivity::class.java))
    }

    override fun showVoteDetail(data: VoteData) {
        Util.startActivityToVoteDetail(requireContext(), data.voteCode)
    }

    override fun showIntroductionDialog() {
        val firstTimePref = Injection.provideFirstTimePref(requireActivity())
        if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_INTRODUTCION_QUICK_POLL, true)) {
            firstTimePref.edit().putBoolean(FirstTimePref.SP_FIRST_INTRODUTCION_QUICK_POLL, false).apply()
            val introductionDialog = Dialog(requireActivity())
            introductionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            introductionDialog.requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            introductionDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            introductionDialog.setCanceledOnTouchOutside(false)

            val data = VoteData().apply {
                authorName = getString(R.string.intro_vote_item_author_name)
                title = getString(R.string.intro_vote_item_title)
                option1Title = getString(R.string.intro_vote_item_option1)
                option2Title = getString(R.string.intro_vote_item_option2)
                pollCount = 30
                option1Count = 15
                option2Count = 15
                startTime = System.currentTimeMillis() - 86400000
                endTime = System.currentTimeMillis() + 864000000
            }

            val content = LayoutInflater.from(activity).inflate(R.layout.card_view_wall_item_intro, null)
            val txtAuthorName = content.findViewById<TextView>(R.id.txtAuthorName)
            val txtTitle = content.findViewById<TextView>(R.id.txtTitle)
            val txtOption1 = content.findViewById<TextView>(R.id.txtFirstOptionTitle)
            val txtOption2 = content.findViewById<TextView>(R.id.txtSecondOptionTitle)
            val txtPubTime = content.findViewById<TextView>(R.id.txtPubTime)
            val txtPollCount = content.findViewById<TextView>(R.id.txtBarPollCount)
            val txtFirstPollCountPercent = content.findViewById<TextView>(R.id.txtFirstPollCountPercent)
            val txtSecondPollCountPercent = content.findViewById<TextView>(R.id.txtSecondPollCountPercent)
            val progressFirstOption = content.findViewById<RoundCornerProgressBar>(R.id.progressFirstOption)
            val progressSecondOption = content.findViewById<RoundCornerProgressBar>(R.id.progressSecondOption)
            val btnThirdOption = content.findViewById<CardView>(R.id.btnThirdOption)
            val btnSecondOption = content.findViewById<CardView>(R.id.btnSecondOption)
            val btnFirstOption = content.findViewById<CardView>(R.id.btnFirstOption)
            val imgChampion1 = content.findViewById<ImageView>(R.id.imgChampion1)
            val imgChampion2 = content.findViewById<ImageView>(R.id.imgChampion2)
            val imgAuthorIcon = content.findViewById<ImageView>(R.id.imgAuthorIcon)

            val initialChar = if (!data.authorName.isNullOrEmpty()) data.authorName.substring(0, 1) else "V"
            val drawable = Util.createRoundTextDrawable(initialChar, ContextCompat.getColor(requireActivity(), R.color.primary_light), 36)
            imgAuthorIcon.setImageDrawable(drawable)

            btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_blue_100))
            btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_blue_100))
            btnThirdOption.visibility = View.GONE

            txtFirstPollCountPercent.visibility = View.GONE
            txtSecondPollCountPercent.visibility = View.GONE

            progressFirstOption.visibility = View.GONE
            progressSecondOption.visibility = View.GONE

            imgChampion1.visibility = View.GONE
            imgChampion2.visibility = View.GONE

            txtAuthorName.text = data.authorName
            txtTitle.text = data.title
            txtOption1.text = data.option1Title
            txtOption2.text = data.option2Title
            txtPubTime.text = (Util.getDate(data.startTime, "yyyy/MM/dd HH:mm")
                    + " ~ " + Util.getDate(data.endTime, "yyyy/MM/dd HH:mm"))
            txtPollCount.text = data.pollCount.toString()
            progressFirstOption.progressColor = ContextCompat.getColor(requireActivity(), R.color.md_blue_600)
            progressFirstOption.progressBackgroundColor = ContextCompat.getColor(requireActivity(), R.color.md_blue_200)
            btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_blue_100))
            progressSecondOption.progressColor = ContextCompat.getColor(requireActivity(), R.color.md_blue_600)
            progressSecondOption.progressBackgroundColor = ContextCompat.getColor(requireActivity(), R.color.md_blue_200)
            btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_blue_100))

            val dialogLongClick = View.OnLongClickListener { optionButton ->
                if (optionButton.id == R.id.btnFirstOption) {
                    progressFirstOption.progressColor = ContextCompat.getColor(requireActivity(), R.color.md_red_600)
                    progressFirstOption.progressBackgroundColor = ContextCompat.getColor(requireActivity(), R.color.md_red_200)
                    btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_red_100))
                    imgChampion1.visibility = View.VISIBLE
                    imgChampion2.visibility = View.INVISIBLE
                    data.option1Count = data.option1Count + 1
                } else {
                    progressSecondOption.progressColor = ContextCompat.getColor(requireActivity(), R.color.md_red_600)
                    progressSecondOption.progressBackgroundColor = ContextCompat.getColor(requireActivity(), R.color.md_red_200)
                    btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_red_100))
                    imgChampion2.visibility = View.VISIBLE
                    imgChampion1.visibility = View.INVISIBLE
                    data.option2Count = data.option2Count + 1
                }

                progressFirstOption.visibility = View.VISIBLE
                progressFirstOption.progress = data.option1Count.toFloat()

                progressSecondOption.visibility = View.VISIBLE
                progressSecondOption.progress = data.option2Count.toFloat()

                txtFirstPollCountPercent.visibility = View.VISIBLE
                txtSecondPollCountPercent.visibility = View.VISIBLE
                data.pollCount = data.pollCount + 1
                progressFirstOption.max = data.pollCount.toFloat()
                progressSecondOption.max = data.pollCount.toFloat()
                txtPollCount.text = data.pollCount.toString()

                val percent1: Double = if (data.pollCount == 0) 0.0 else data.option1Count.toDouble() / data.pollCount * 100
                val percent2: Double = if (data.pollCount == 0) 0.0 else data.option2Count.toDouble() / data.pollCount * 100
                txtFirstPollCountPercent.text = String.format("%3.1f%%", percent1)
                txtSecondPollCountPercent.text = String.format("%3.1f%%", percent2)
                Toast.makeText(activity, R.string.toast_network_connect_success_poll, Toast.LENGTH_SHORT).show()
                btnFirstOption.postDelayed({ introductionDialog.dismiss() }, 3000)
                false
            }
            btnFirstOption.setOnLongClickListener(dialogLongClick)
            btnSecondOption.setOnLongClickListener(dialogLongClick)

            introductionDialog.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            introductionDialog.setContentView(content, LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            introductionDialog.setCancelable(false)
            introductionDialog.show()
        }
    }

    override fun onStop() {
        super.onStop()
        _binding?.vpHeader?.stopAutoScroll()
    }

    override fun onStart() {
        super.onStart()
        pagePresenter.resetPromotion()
        _binding?.vpHeader?.startAutoScroll()
    }

    override fun onResume() {
        super.onResume()
        pagePresenter.refreshAllFragment()
    }

    override fun showLoadingCircle() {
        _binding?.let { b ->
            b.circleLoad.visibility = View.VISIBLE
            b.circleLoad.setText(getString(R.string.vote_detail_circle_loading))
            b.circleLoad.spin()
        }
    }

    override fun hideLoadingCircle() {
        _binding?.let { b ->
            b.circleLoad.stopSpinning()
            b.circleLoad.visibility = View.GONE
        }
    }

    override fun setupPromotionAdmob(promotionList: List<Promotion>, user: User) {
        val promotionTypeList = ArrayList<PromotionType>()
        for (i in promotionList.indices) {
            if (i == 0 && ENABLE_PROMOTION_ADMOB && Util.isNetworkConnected(requireContext())) {
                promotionTypeList.add(PromotionType(PromotionType.PROM0TION_TYPE_ADMOB, Promotion()))
            }
            promotionTypeList.add(PromotionType(PromotionType.PROMOTION_TYPE_FUNNY_VOTE, promotionList[i]))
        }
        _binding?.let { b ->
            b.vpHeader.adapter = HeaderAdapter(promotionTypeList, user)
            b.vpHeader.adapter?.notifyDataSetChanged()
            b.vpHeader.startAutoScroll()
        }
    }

    override fun setUpTabsAdapter(user: User) {
        _binding?.let { b ->
            tabsAdapter = TabsAdapter(childFragmentManager, user)
            val currentItem = b.vpMainPage.currentItem
            b.vpMainPage.adapter = tabsAdapter
            b.vpMainPage.currentItem = currentItem
        }
    }

    override fun setUpTabsAdapter(user: User, targetUser: User) {
        setUpTabsAdapter(user)
    }

    override fun showHintToast(res: Int, arg: Long) {
        Toast.makeText(context, getString(res, arg), Toast.LENGTH_SHORT).show()
    }

    override fun showPollPasswordDialog(data: VoteData, optionCode: String) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(R.layout.password_dialog)
        builder.setPositiveButton(requireActivity().resources.getString(R.string.vote_detail_dialog_password_input), null)
        builder.setNegativeButton(requireContext().applicationContext.resources.getString(R.string.account_dialog_cancel), null)
        builder.setTitle(requireActivity().getString(R.string.vote_detail_dialog_password_title))
        passwordDialog = builder.create()

        passwordDialog!!.setOnShowListener { dialogInterface ->
            val password = (dialogInterface as AlertDialog).findViewById<EditText>(R.id.edtEnterPassword)
            val ok = dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE)
            ok.setOnClickListener {
                Log.d(TAG, "showPollPasswordDialog PW:")
                pagePresenter.pollVote(data, optionCode, password?.text?.toString() ?: "")
                tracker?.send(
                    HitBuilders.EventBuilder()
                        .setCategory("msin")
                        .setAction(AnalyzticsTag.ACTION_QUICK_POLL_VOTE)
                        .setLabel(data.voteCode)
                        .build()
                )
            }
        }
        passwordDialog!!.show()
    }

    override fun hidePollPasswordDialog() {
        if (passwordDialog != null && passwordDialog!!.isShowing) {
            passwordDialog!!.dismiss()
        }
    }

    override fun shakePollPasswordDialog() {
        if (passwordDialog != null && passwordDialog!!.isShowing) {
            val password = passwordDialog!!.findViewById<EditText>(R.id.edtEnterPassword)
            password?.selectAll()
            val shake = AnimationUtils.loadAnimation(activity, R.anim.edittext_shake)
            password?.startAnimation(shake)
        }
    }

    private inner class HeaderAdapter(private val promotionTypeList: List<PromotionType>, private val user: User?) : PagerAdapter() {

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            when {
                promotionTypeList[position].promotionType == PromotionType.PROMOTION_TYPE_FUNNY_VOTE -> {
                    val headerItem = layoutInflater.inflate(R.layout.item_promotion_funny_vote, null)
                    val promotion = headerItem.findViewById<ImageView>(R.id.headerImage)
                    if (Util.isNetworkConnected(requireContext())) {
                        Glide.with(this@MainPageFragment)
                            .load(promotionTypeList[position].promotion.imageURL)
                            .override(resources.getDimension(R.dimen.promotion_image_width).toInt(), resources.getDimension(R.dimen.promotion_image_high).toInt())
                            .fitCenter()
                            .into(promotion)
                        val actionURL = promotionTypeList[position].promotion.actionURL
                        promotion.setOnClickListener {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(actionURL))
                            tracker?.send(
                                HitBuilders.EventBuilder()
                                    .setCategory(AnalyzticsTag.CATEGORY_PROMOTION)
                                    .setAction(AnalyzticsTag.ACTION_CLICK_PROMOTION)
                                    .setLabel(actionURL)
                                    .build()
                            )
                            startActivity(browserIntent)
                        }
                    } else {
                        Glide.with(this@MainPageFragment)
                            .load(R.drawable.main_topic)
                            .override(resources.getDimension(R.dimen.promotion_image_width).toInt(), resources.getDimension(R.dimen.promotion_image_high).toInt())
                            .fitCenter()
                            .into(promotion)
                        val actionURL = "https://play.google.com/store/apps/details?id=com.heaton.funnyvote"
                        promotion.setOnClickListener {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(actionURL))
                            tracker?.send(
                                HitBuilders.EventBuilder()
                                    .setCategory(AnalyzticsTag.CATEGORY_PROMOTION)
                                    .setAction(AnalyzticsTag.ACTION_CLICK_PROMOTION)
                                    .setLabel(actionURL)
                                    .build()
                            )
                            startActivity(browserIntent)
                        }
                    }
                    container.addView(headerItem)
                    return headerItem
                }
                promotionTypeList[position].promotionType == PromotionType.PROM0TION_TYPE_ADMOB -> {
                    if (promotionADMOB == null) {
                        promotionADMOB = layoutInflater.inflate(R.layout.item_promotion_admob, null)
                        val adview = promotionADMOB!!.findViewById<AdView>(R.id.adViewPromotion)
                        val adRequest = AdRequest.Builder().build()
                        adview.loadAd(adRequest)
                    }
                    container.addView(promotionADMOB)
                    return promotionADMOB as View
                }
                else -> return Unit
            }
        }

        override fun getCount(): Int {
            return promotionTypeList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }
    }

    private inner class TabsAdapter(fm: FragmentManager, private var user: User) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(i: Int): Fragment {
            return when (i) {
                0 -> {
                    if (hotsFragment == null) {
                        hotsFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_HOT, user)
                        hotsFragment!!.setPresenter(pagePresenter)
                    }
                    hotsFragment!!
                }
                else -> {
                    if (newsFragment == null) {
                        newsFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_NEW, user)
                        newsFragment!!.setPresenter(pagePresenter)
                    }
                    newsFragment!!
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> requireContext().getString(R.string.main_page_tab_hot)
                else -> requireContext().getString(R.string.main_page_tab_new)
            }
        }
    }

    abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {

        private var mCurrentState = State.IDLE

        enum class State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }

        override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
            when {
                i == 0 -> {
                    if (mCurrentState != State.EXPANDED) {
                        onStateChanged(appBarLayout, State.EXPANDED)
                    }
                    mCurrentState = State.EXPANDED
                }
                Math.abs(i) >= appBarLayout.totalScrollRange -> {
                    if (mCurrentState != State.COLLAPSED) {
                        onStateChanged(appBarLayout, State.COLLAPSED)
                    }
                    mCurrentState = State.COLLAPSED
                }
                else -> {
                    if (mCurrentState != State.IDLE) {
                        onStateChanged(appBarLayout, State.IDLE)
                    }
                    mCurrentState = State.IDLE
                }
            }
        }

        abstract fun onStateChanged(appBarLayout: AppBarLayout, state: State)
    }

    companion object {
        var TAG = MainPageFragment::class.java.simpleName
        var ENABLE_PROMOTION_ADMOB = true
    }
}
