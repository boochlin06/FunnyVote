package com.heaton.funnyvote.ui.personal

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.bumptech.glide.Glide
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.MainActivity
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.notification.VoteNotificationManager
import com.heaton.funnyvote.ui.createvote.CreateVoteActivity
import com.heaton.funnyvote.ui.main.MainPageContract
import com.heaton.funnyvote.ui.main.MainPageTabFragment
import com.heaton.funnyvote.utils.Util

import kotlinx.android.synthetic.main.activity_personal.*

import de.hdodenhof.circleimageview.CircleImageView

class UserActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener, PersonalContract.UserPageView {
    private var isAvatarShown = true

//    private var imgUserIcon: CircleImageView? = null
//    private var txtUserName: TextView? = null
//    private var txtSubTitle: TextView? = null
    private var maxScrollSize: Int = 0
    private var tabsAdapter: TabsAdapter? = null
    //private var viewPager: ViewPager? = null
    private var tracker: Tracker? = null
    private var isMainActivityNeedRestart = false
    private var passwordDialog: AlertDialog? = null
    private lateinit var presenter: MainPageContract.Presenter
    private var createFragment: MainPageTabFragment? = null
    private var participateFragment: MainPageTabFragment? = null
    private var favoriteFragment: MainPageTabFragment? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker
        val tabLayout = findViewById<View>(R.id.tabLayoutPersonal) as TabLayout
        //viewPager = findViewById<View>(R.id.vpMain) as ViewPager
        //val appbarLayout = findViewById<View>(R.id.appBarMain) as AppBarLayout

        //val toolbar = findViewById<View>(R.id.toolbarSub) as Toolbar
        toolbarSub.setNavigationOnClickListener { onBackPressed() }

        appBarMain.addOnOffsetChangedListener(this)
        maxScrollSize = appBarMain.totalScrollRange

        tabsAdapter = TabsAdapter(supportFragmentManager, User())
        vpMain.adapter = tabsAdapter;
        tabLayout.setupWithViewPager(vpMain)
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_BOX_CREATE)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
        vpMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    tracker!!.setScreenName(AnalyzticsTag.SCREEN_BOX_CREATE)
                } else if (position == 1) {
                    tracker!!.setScreenName(AnalyzticsTag.SCREEN_BOX_PARTICIPATE)
                } else if (position == 2) {
                    tracker!!.setScreenName(AnalyzticsTag.SCREEN_BOX_FAVORITE)
                }
                tracker!!.send(HitBuilders.ScreenViewBuilder().build())
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        isMainActivityNeedRestart = VoteNotificationManager.ACTION_NOTIFICATION_USER_ACTIVITY_START == intent.action
        presenter = UserPresenter(Injection.provideVoteDataRepository(applicationContext)
                , Injection.provideUserRepository(applicationContext), this)
        presenter.setTargetUser(User())
        presenter.start()
    }

    override fun onNewIntent(intent: Intent) {
        val action = intent.action
        isMainActivityNeedRestart = VoteNotificationManager.ACTION_NOTIFICATION_USER_ACTIVITY_START == action
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        //presenter.start();
        presenter.refreshAllFragment()
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_BOX)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onBackPressed() {
        if (isMainActivityNeedRestart) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        super.onBackPressed()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
        if (maxScrollSize == 0)
            maxScrollSize = appBarLayout.totalScrollRange

        val percentage = Math.abs(i) * 100 / maxScrollSize

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && isAvatarShown) {
            isAvatarShown = false
            imgUserIcon!!.animate().scaleY(0f).scaleX(0f).setDuration(200).start()
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !isAvatarShown) {
            isAvatarShown = true
            imgUserIcon!!.animate()
                    .scaleY(1f).scaleX(1f)
                    .start()
        }
    }

    override fun setUpUserView(user: User) {
        txtUserName!!.text = user.userName
        txtSubTitle!!.text = User.getUserTypeString(user.type) + ":" + user.email
        if (user.userIcon == null || user.userIcon.isEmpty()) {
            imgUserIcon!!.setImageResource(R.drawable.user_avatar)
        } else {
            Glide.with(this)
                    .load(user.userIcon)
                    .override(resources.getDimension(R.dimen.personal_image_width).toInt(), resources.getDimension(R.dimen.personal_image_high).toInt())
                    .dontAnimate()
                    .fitCenter()
                    .crossFade()
                    .into(imgUserIcon!!)
        }
        tracker!!.send(HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_USER)
                .setAction(AnalyzticsTag.ACTION_ENTER_USER_INFO)
                .setLabel(user.userCode).build())
    }

    override fun showShareDialog(data: VoteData) {
        Util.sendShareIntent(this, data)
    }

    override fun showAuthorDetail(data: VoteData) {
        Util.sendPersonalDetailIntent(this, data)
    }

    override fun showCreateVote() {
        this.startActivity(Intent(this, CreateVoteActivity::class.java))
    }

    override fun showVoteDetail(data: VoteData) {
        Util.startActivityToVoteDetail(this, data.voteCode)
    }


    override fun showIntroductionDialog() {
        //nothing
    }

    override fun showLoadingCircle() {
        //        circleLoad.setVisibility(View.VISIBLE);
        //        circleLoad.setText(getString(R.string.vote_detail_circle_loading));
        //        circleLoad.spin();
    }

    override fun hideLoadingCircle() {
        //        circleLoad.stopSpinning();
        //        circleLoad.setVisibility(View.GONE);
    }

    override fun setupPromotionAdmob(promotionList: List<Promotion>, user: User) {
        //none
    }

    override fun setUpTabsAdapter(user: User) {
        tabsAdapter = TabsAdapter(this@UserActivity.supportFragmentManager, user)
        val currentItem = vpMain.currentItem
        vpMain.adapter = tabsAdapter
        vpMain.currentItem = currentItem
    }

    override fun setUpTabsAdapter(user: User, targetUser: User) {
        setUpTabsAdapter(user)
    }

    override fun showHintToast(res: Int, arg: Long) {
        Toast.makeText(this, getString(res, arg), Toast.LENGTH_SHORT).show()
    }

    override fun showPollPasswordDialog(data: VoteData, optionCode: String) {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.password_dialog)
        builder.setPositiveButton(this.resources
                .getString(R.string.vote_detail_dialog_password_input), null)
        builder.setNegativeButton(this.applicationContext.resources
                .getString(R.string.account_dialog_cancel), null)
        builder.setTitle(this.getString(R.string.vote_detail_dialog_password_title))
        passwordDialog = builder.create()

        passwordDialog!!.setOnShowListener { dialogInterface ->
            val password = (dialogInterface as AlertDialog).findViewById<View>(R.id.edtEnterPassword) as EditText?
            val ok = dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE)
            ok.setOnClickListener {
                Log.d(TAG, "showPollPasswordDialog PW:")
                presenter.pollVote(data, optionCode, password!!.text.toString())
                //                        tracker.send(new HitBuilders.EventBuilder()
                //                                .setCategory(tab)
                //                                .setAction(AnalyzticsTag.ACTION_QUICK_POLL_VOTE)
                //                                .setLabel(data.getVoteCode())
                //                                .build());
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
            val shake = AnimationUtils.loadAnimation(this, R.anim.edittext_shake)
            password.startAnimation(shake)
        }
    }

    override val isPasswordDialogShowing: Boolean
        get() = passwordDialog!!.isShowing

    override fun setPresenter(presenter: MainPageContract.Presenter) {
        this.presenter = presenter
    }

    private inner class TabsAdapter(fm: FragmentManager, private val user: User) : FragmentStatePagerAdapter(fm) {

        override fun getCount(): Int {
            return 3
        }

        override fun getItem(i: Int): Fragment? {
            val argument = Bundle()
            argument.putParcelable(MainPageTabFragment.KEY_LOGIN_USER, user)
            when (i) {
                0 -> {
                    if (createFragment == null) {
                        createFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_CREATE, user)
                        createFragment!!.setPresenter(presenter)
                    }
                    return createFragment
                }
                1 -> {
                    if (participateFragment == null) {
                        participateFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_PARTICIPATE, user)
                        participateFragment!!.setPresenter(presenter)
                    }
                    return participateFragment
                }
                2 -> {
                    if (favoriteFragment == null) {
                        favoriteFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_FAVORITE, user)
                        favoriteFragment!!.setPresenter(presenter)
                    }
                    return favoriteFragment
                }
            }
            return null
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return getString(R.string.personal_tab_create)
                1 -> return getString(R.string.personal_tab_participate)
                2 -> return getString(R.string.personal_tab_favorite)
            }
            return ""
        }
    }

    companion object {
        private val TAG = UserActivity::class.java.simpleName

        private val PERCENTAGE_TO_ANIMATE_AVATAR = 20
    }

}
