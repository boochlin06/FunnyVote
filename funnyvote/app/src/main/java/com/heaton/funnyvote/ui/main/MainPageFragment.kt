package com.heaton.funnyvote.ui.main

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutCompat
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import at.grabner.circleprogress.TextMode
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar
import com.amulyakhare.textdrawable.TextDrawable
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.NativeExpressAdView
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FirstTimePref
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.ui.CirclePageIndicator
import com.heaton.funnyvote.ui.createvote.CreateVoteActivity
import com.heaton.funnyvote.utils.Util
import kotlinx.android.synthetic.main.fragment_main_page_top.*
import java.lang.Integer.toString
import java.util.*

/**
 * Created by heaton on 16/4/1.
 */
class MainPageFragment : android.support.v4.app.Fragment(), MainPageContract.MainPageView {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val application = activity!!.application as FunnyVoteApplication
        tracker = application.defaultTracker

        circleLoad.setTextMode(TextMode.TEXT)
        circleLoad.isShowTextWhileSpinning = true
        circleLoad.setFillCircleColor(ContextCompat.getColor(this.activity!!, R.color.md_amber_50))

        circleLoad.setText(getString(R.string.vote_detail_circle_loading))
        vpHeader.adapter = HeaderAdapter(ArrayList(), User())
        vpHeader.currentItem = 0

        val tabMainPage = view.findViewById<View>(R.id.tabLayoutMainPage) as TabLayout
        tabMainPage.setupWithViewPager(vpMainPage)

        val titleIndicator = view.findViewById<View>(R.id.vpIndicator) as CirclePageIndicator
        titleIndicator.setViewPager(vpHeader)
        vpHeader.interval = 100000
        vpHeader.setScrollDurationFactor(5.0)

        appBarMain.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: AppBarStateChangeListener.State) {
                if (state == MainPageFragment.AppBarStateChangeListener.State.EXPANDED) {
                    vpHeader.startAutoScroll()
                } else if (state == MainPageFragment.AppBarStateChangeListener.State.COLLAPSED) {
                    vpHeader.stopAutoScroll()
                }
            }
        })
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_MAIN_HOT)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
        vpMainPage.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    tracker!!.setScreenName(AnalyzticsTag.SCREEN_MAIN_HOT)
                } else if (position == 1) {
                    tracker!!.setScreenName(AnalyzticsTag.SCREEN_MAIN_NEW)
                }
                tracker!!.send(HitBuilders.ScreenViewBuilder().build())
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        ENABLE_PROMOTION_ADMOB = resources.getBoolean(R.bool.enable_promotion_admob)
        pagePresenter = MainPagePresenter(Injection.provideVoteDataRepository(requireContext()), Injection.provideUserRepository(requireContext()), Injection.providePromotionRepository(requireContext()), this)
        pagePresenter.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_page_top, null)
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
        val firstTimePref = Injection.provideFirstTimePref(activity!!)
        if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_INTRODUTCION_QUICK_POLL, true)) {
            firstTimePref.edit().putBoolean(FirstTimePref.SP_FIRST_INTRODUTCION_QUICK_POLL, false).apply()
            val introductionDialog = Dialog(activity!!)
            introductionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            introductionDialog.requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            introductionDialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT)
            introductionDialog.setCanceledOnTouchOutside(false)

            val data = VoteData()
            data.authorName = getString(R.string.intro_vote_item_author_name)
            data.title = getString(R.string.intro_vote_item_title)
            data.option1Title = getString(R.string.intro_vote_item_option1)
            data.option2Title = getString(R.string.intro_vote_item_option2)
            data.pollCount = 30
            data.option1Count = 15
            data.option2Count = 15
            data.startTime = System.currentTimeMillis() - 86400000
            data.endTime = System.currentTimeMillis() + 864000000

            val content = LayoutInflater.from(activity).inflate(R.layout.card_view_wall_item_intro, null)
            val txtAuthorName = content.findViewById<View>(R.id.txtAuthorName) as TextView
            val txtTitle = content.findViewById<View>(R.id.txtTitle) as TextView
            val txtOption1 = content.findViewById<View>(R.id.txtFirstOptionTitle) as TextView
            val txtOption2 = content.findViewById<View>(R.id.txtSecondOptionTitle) as TextView
            val txtPubTime = content.findViewById<View>(R.id.txtPubTime) as TextView
            val txtPollCount = content.findViewById<View>(R.id.txtBarPollCount) as TextView
            val txtFirstPollCountPercent = content.findViewById<View>(R.id.txtFirstPollCountPercent) as TextView
            val txtSecondPollCountPercent = content.findViewById<View>(R.id.txtSecondPollCountPercent) as TextView
            val progressFirstOption = content.findViewById<View>(R.id.progressFirstOption) as RoundCornerProgressBar
            val progressSecondOption = content.findViewById<View>(R.id.progressSecondOption) as RoundCornerProgressBar
            val btnThirdOption = content.findViewById<View>(R.id.btnThirdOption) as CardView
            val btnSecondOption = content.findViewById<View>(R.id.btnSecondOption) as CardView
            val btnFirstOption = content.findViewById<View>(R.id.btnFirstOption) as CardView
            val imgChampion1 = content.findViewById<View>(R.id.imgChampion1) as ImageView
            val imgChampion2 = content.findViewById<View>(R.id.imgChampion2) as ImageView

            val imgAuthorIcon = content.findViewById<View>(R.id.imgAuthorIcon) as ImageView

            val drawable = TextDrawable.builder().beginConfig().width(36).height(36).endConfig()
                    .buildRound(data.authorName.substring(0, 1), R.color.primary_light)
            imgAuthorIcon.setImageDrawable(drawable)

            btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(activity!!, R.color.md_blue_100))
            btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(activity!!, R.color.md_blue_100))
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
            txtPubTime.text = (Util.getDate(data.startTime, "yyyy/MM/dd hh:mm")
                    + " ~ " + Util.getDate(data.endTime, "yyyy/MM/dd hh:mm"))
            txtPollCount.text = toString(data.pollCount)
            progressFirstOption.progressColor = ContextCompat.getColor(activity!!, R.color.md_blue_600)
            progressFirstOption.progressBackgroundColor = ContextCompat.getColor(activity!!, R.color.md_blue_200)
            btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(activity!!, R.color.md_blue_100))
            progressSecondOption.progressColor = ContextCompat.getColor(activity!!, R.color.md_blue_600)
            progressSecondOption.progressBackgroundColor = ContextCompat.getColor(activity!!, R.color.md_blue_200)
            btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(activity!!, R.color.md_blue_100))

            val dialogLongClick = View.OnLongClickListener { optionButton ->
                if (optionButton.id == R.id.btnFirstOption) {
                    progressFirstOption.progressColor = ContextCompat.getColor(activity!!, R.color.md_red_600)
                    progressFirstOption.progressBackgroundColor = ContextCompat.getColor(activity!!, R.color.md_red_200)
                    btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(activity!!, R.color.md_red_100))
                    imgChampion1.visibility = View.VISIBLE
                    imgChampion2.visibility = View.INVISIBLE
                    data.option1Count = data.option1Count + 1
                } else {
                    progressSecondOption.progressColor = ContextCompat.getColor(activity!!, R.color.md_red_600)
                    progressSecondOption.progressBackgroundColor = ContextCompat.getColor(activity!!, R.color.md_red_200)
                    btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(activity!!, R.color.md_red_100))
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

                val percent1: Double = if (data.pollCount == 0)
                    0.0
                else
                    data.option1Count.toDouble() / data.pollCount * 100
                val percent2: Double = if (data.pollCount == 0)
                    0.0
                else
                    data.option2Count.toDouble() / data.pollCount * 100
                txtFirstPollCountPercent.text = String.format("%3.1f%%", percent1)
                txtSecondPollCountPercent.text = String.format("%3.1f%%", percent2)
                Toast.makeText(activity, R.string.toast_network_connect_success_poll, Toast.LENGTH_SHORT).show()
                btnFirstOption.postDelayed({ introductionDialog.dismiss() }, 3000)
                false
            }
            btnFirstOption.setOnLongClickListener(dialogLongClick)
            btnSecondOption.setOnLongClickListener(dialogLongClick)

            introductionDialog.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            introductionDialog.setContentView(content, LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT))
            introductionDialog.setCancelable(false)
            introductionDialog.show()
        }
    }

    override fun onStop() {
        super.onStop()
        vpHeader.stopAutoScroll()
    }

    override fun onStart() {
        super.onStart()
        pagePresenter.resetPromotion()
        vpHeader.startAutoScroll()
    }

    override fun onResume() {
        super.onResume()
        //TODO,WHY NO RESPONSE ON HERE
        pagePresenter.refreshAllFragment()
    }

    override fun showLoadingCircle() {
        circleLoad.visibility = View.VISIBLE
        circleLoad.setText(getString(R.string.vote_detail_circle_loading))
        circleLoad.spin()
    }

    override fun hideLoadingCircle() {
        circleLoad.stopSpinning()
        circleLoad.visibility = View.GONE
    }

    override fun setupPromotionAdmob(promotionList: List<Promotion>, user: User) {
        val promotionTypeList = ArrayList<PromotionType>()
        for (i in promotionList.indices) {
            if (i == 0 && ENABLE_PROMOTION_ADMOB && Util.isNetworkConnected(requireContext())) {
                promotionTypeList.add(PromotionType(PromotionType.PROM0TION_TYPE_ADMOB, Promotion()))
            }
            promotionTypeList.add(PromotionType(PromotionType.PROMOTION_TYPE_FUNNY_VOTE, promotionList[i]))
        }
        vpHeader.adapter = HeaderAdapter(promotionTypeList, user)
        vpHeader.adapter!!.notifyDataSetChanged()
        vpHeader.startAutoScroll()
    }

    override fun setUpTabsAdapter(user: User) {
        tabsAdapter = TabsAdapter(childFragmentManager, user)
        val currentItem = vpMainPage.currentItem
        vpMainPage.adapter = tabsAdapter
        vpMainPage.currentItem = currentItem
    }

    override fun setUpTabsAdapter(user: User, targetUser: User) {
        setUpTabsAdapter(user)
    }

    override fun showHintToast(res: Int, arg: Long) {
        Toast.makeText(context, getString(res, arg), Toast.LENGTH_SHORT).show()
    }

    override fun showPollPasswordDialog(data: VoteData, optionCode: String) {
        val builder = AlertDialog.Builder(activity!!)
        builder.setView(R.layout.password_dialog)
        builder.setPositiveButton(activity!!.resources
                .getString(R.string.vote_detail_dialog_password_input), null)
        builder.setNegativeButton(requireContext().applicationContext.resources
                .getString(R.string.account_dialog_cancel), null)
        builder.setTitle(activity!!.getString(R.string.vote_detail_dialog_password_title))
        passwordDialog = builder.create()

        passwordDialog!!.setOnShowListener { dialogInterface ->
            val password = (dialogInterface as AlertDialog).findViewById<View>(R.id.edtEnterPassword) as EditText?
            val ok = dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE)
            ok.setOnClickListener {
                Log.d(TAG, "showPollPasswordDialog PW:")
                pagePresenter.pollVote(data, optionCode, password!!.text.toString())
                tracker!!.send(HitBuilders.EventBuilder()
                        .setCategory("msin")
                        .setAction(AnalyzticsTag.ACTION_QUICK_POLL_VOTE)
                        .setLabel(data.voteCode)
                        .build())
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
            val password = passwordDialog!!.findViewById<View>(R.id.edtEnterPassword) as EditText?
            password!!.selectAll()
            val shake = AnimationUtils.loadAnimation(activity, R.anim.edittext_shake)
            password.startAnimation(shake)
        }
    }


    private inner class HeaderAdapter(private val promotionTypeList: List<PromotionType>, private val user: User?) : PagerAdapter() {

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            //val inflater = activity!!.layoutInflater.inflate(activity)
            when {
                promotionTypeList[position].promotionType == PromotionType.PROMOTION_TYPE_FUNNY_VOTE -> {
                    val headerItem = layoutInflater.inflate(R.layout.item_promotion_funny_vote, null)
                    val promotion = headerItem.findViewById(R.id.headerImage) as ImageView
                    if (Util.isNetworkConnected(requireContext())) {
                        Glide.with(context)
                                .load(promotionTypeList[position].promotion.imageURL)
                                .override(resources.getDimension(R.dimen.promotion_image_width).toInt(), resources.getDimension(R.dimen.promotion_image_high).toInt())
                                .fitCenter()
                                .crossFade()
                                .into(promotion)
                        val actionURL = promotionTypeList[position].promotion.actionURL
                        promotion.setOnClickListener {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(actionURL))
                            tracker!!.send(HitBuilders.EventBuilder()
                                    .setCategory(AnalyzticsTag.CATEGORY_PROMOTION)
                                    .setAction(AnalyzticsTag.ACTION_CLICK_PROMOTION)
                                    .setLabel(actionURL)
                                    .build())
                            startActivity(browserIntent)
                        }
                    } else {
                        Glide.with(context)
                                .load(R.drawable.main_topic)
                                .override(resources.getDimension(R.dimen.promotion_image_width).toInt(), resources.getDimension(R.dimen.promotion_image_high).toInt())
                                .fitCenter()
                                .crossFade()
                                .into(promotion)
                        val actionURL = "https://play.google.com/store/apps/details?id=com.heaton.funnyvote"
                        promotion.setOnClickListener {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(actionURL))
                            tracker!!.send(HitBuilders.EventBuilder()
                                    .setCategory(AnalyzticsTag.CATEGORY_PROMOTION)
                                    .setAction(AnalyzticsTag.ACTION_CLICK_PROMOTION)
                                    .setLabel(actionURL)
                                    .build())
                            startActivity(browserIntent)
                        }
                    }
                    container.addView(headerItem)
                    return headerItem
                }
                promotionTypeList[position].promotionType == PromotionType.PROM0TION_TYPE_ADMOB -> {
                    if (promotionADMOB == null) {
                        promotionADMOB = layoutInflater.inflate(R.layout.item_promotion_admob, null)
                        val adview = promotionADMOB!!.findViewById<View>(R.id.adViewPromotion) as NativeExpressAdView
                        val adRequest = AdRequest.Builder()
                                .setGender(if (user != null && User.GENDER_MALE == user.gender)
                                    AdRequest.GENDER_MALE
                                else
                                    AdRequest.GENDER_FEMALE)
                                .build()
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

    private inner class TabsAdapter(fm: FragmentManager, private var user: User) : FragmentStatePagerAdapter(fm) {

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(i: Int): Fragment? {
            when (i) {
                0 -> {
                    if (hotsFragment == null) {
                        hotsFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_HOT, user)
                        hotsFragment!!.setPresenter(pagePresenter)
                        //pagePresenter.setHotsFragmentView(hotsFragment);
                    }
                    return hotsFragment
                }
                1 -> {
                    if (newsFragment == null) {
                        newsFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_NEW, user)
                        newsFragment!!.setPresenter(pagePresenter)
                        //pagePresenter.setNewsFragmentView(newsFragment);
                    }
                    return newsFragment
                }
            }
            return null
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return requireContext().getString(R.string.main_page_tab_hot)
                1 -> return requireContext().getString(R.string.main_page_tab_new)
            }
            return ""
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

        var TAG = MainPageFragment::class.java.simpleName!!
        var ENABLE_PROMOTION_ADMOB = true
    }


}
