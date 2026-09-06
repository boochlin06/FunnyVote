package com.heaton.funnyvote.ui.personal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.google.android.material.appbar.AppBarLayout
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.MainActivity
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.databinding.ActivityPersonalBinding
import com.heaton.funnyvote.notification.VoteNotificationManager
import com.heaton.funnyvote.ui.createvote.CreateVoteActivity
import com.heaton.funnyvote.ui.main.MainPageContract
import com.heaton.funnyvote.ui.main.MainPageTabFragment
import com.heaton.funnyvote.utils.Util

class UserActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener, PersonalContract.UserPageView {

    private lateinit var binding: ActivityPersonalBinding
    private var isAvatarShown = true
    private var maxScrollSize: Int = 0
    private var tabsAdapter: TabsAdapter? = null
    private var tracker: Tracker? = null
    private var isMainActivityNeedRestart = false
    private var passwordDialog: AlertDialog? = null
    private lateinit var presenter: MainPageContract.Presenter
    private var createFragment: MainPageTabFragment? = null
    private var participateFragment: MainPageTabFragment? = null
    private var favoriteFragment: MainPageTabFragment? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker

        binding.toolbarSub.setNavigationOnClickListener { onBackPressed() }

        binding.appBarMain.addOnOffsetChangedListener(this)
        maxScrollSize = binding.appBarMain.totalScrollRange

        tabsAdapter = TabsAdapter(supportFragmentManager, User())
        binding.vpMain.adapter = tabsAdapter
        binding.tabLayoutPersonal.setupWithViewPager(binding.vpMain)
        tracker?.setScreenName(AnalyzticsTag.SCREEN_BOX_CREATE)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
        binding.vpMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> tracker?.setScreenName(AnalyzticsTag.SCREEN_BOX_CREATE)
                    1 -> tracker?.setScreenName(AnalyzticsTag.SCREEN_BOX_PARTICIPATE)
                    2 -> tracker?.setScreenName(AnalyzticsTag.SCREEN_BOX_FAVORITE)
                }
                tracker?.send(HitBuilders.ScreenViewBuilder().build())
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        isMainActivityNeedRestart = VoteNotificationManager.ACTION_NOTIFICATION_USER_ACTIVITY_START == intent.action
        presenter = UserPresenter(
            Injection.provideVoteDataRepository(applicationContext),
            Injection.provideUserRepository(applicationContext),
            this
        )
        (presenter as UserPresenter).setTargetUser(User())
        presenter.start()
    }

    override fun onNewIntent(intent: Intent) {
        val action = intent.action
        isMainActivityNeedRestart = VoteNotificationManager.ACTION_NOTIFICATION_USER_ACTIVITY_START == action
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        presenter.refreshAllFragment()
        tracker?.setScreenName(AnalyzticsTag.SCREEN_BOX)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
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
            binding.imgUserIcon.animate().scaleY(0f).scaleX(0f).setDuration(200).start()
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !isAvatarShown) {
            isAvatarShown = true
            binding.imgUserIcon.animate().scaleY(1f).scaleX(1f).start()
        }
    }

    override fun setUpUserView(user: User) {
        binding.txtUserName.text = user.userName
        binding.txtSubTitle.text = User.getUserTypeString(user.type) + ":" + user.email
        if (user.userIcon.isNullOrEmpty()) {
            binding.imgUserIcon.setImageResource(R.drawable.user_avatar)
        } else {
            Glide.with(this)
                .load(user.userIcon)
                .override(
                    resources.getDimension(R.dimen.personal_image_width).toInt(),
                    resources.getDimension(R.dimen.personal_image_high).toInt()
                )
                .fitCenter()
                .into(binding.imgUserIcon)
        }
        tracker?.send(
            HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_USER)
                .setAction(AnalyzticsTag.ACTION_ENTER_USER_INFO)
                .setLabel(user.userCode ?: "").build()
        )
    }

    override fun showShareDialog(data: VoteData) {
        Util.sendShareIntent(this, data)
    }

    override fun showAuthorDetail(data: VoteData) {
        Util.sendPersonalDetailIntent(this, data)
    }

    override fun showCreateVote() {
        startActivity(Intent(this, CreateVoteActivity::class.java))
    }

    override fun showVoteDetail(data: VoteData) {
        Util.startActivityToVoteDetail(this, data.voteCode)
    }

    override fun showIntroductionDialog() {}
    override fun showLoadingCircle() {}
    override fun hideLoadingCircle() {}
    override fun setupPromotionAdmob(promotionList: List<Promotion>, user: User) {}

    override fun setUpTabsAdapter(user: User) {
        tabsAdapter = TabsAdapter(supportFragmentManager, user)
        val currentItem = binding.vpMain.currentItem
        binding.vpMain.adapter = tabsAdapter
        binding.vpMain.currentItem = currentItem
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
        builder.setPositiveButton(resources.getString(R.string.vote_detail_dialog_password_input), null)
        builder.setNegativeButton(applicationContext.resources.getString(R.string.account_dialog_cancel), null)
        builder.setTitle(getString(R.string.vote_detail_dialog_password_title))
        passwordDialog = builder.create()

        passwordDialog!!.setOnShowListener { dialogInterface ->
            val password = (dialogInterface as AlertDialog).findViewById<EditText>(R.id.edtEnterPassword)
            val ok = dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE)
            ok.setOnClickListener {
                Log.d(TAG, "showPollPasswordDialog PW:")
                presenter.pollVote(data, optionCode, password?.text?.toString() ?: "")
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
            val shake = AnimationUtils.loadAnimation(this, R.anim.edittext_shake)
            password?.startAnimation(shake)
        }
    }

    override val isPasswordDialogShowing: Boolean
        get() = passwordDialog != null && passwordDialog!!.isShowing

    override fun setPresenter(presenter: MainPageContract.Presenter) {
        this.presenter = presenter
    }

    private inner class TabsAdapter(fm: FragmentManager, private val user: User) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = 3

        override fun getItem(i: Int): Fragment {
            return when (i) {
                0 -> {
                    if (createFragment == null) {
                        createFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_CREATE, user)
                        createFragment!!.setPresenter(presenter)
                    }
                    createFragment!!
                }
                1 -> {
                    if (participateFragment == null) {
                        participateFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_PARTICIPATE, user)
                        participateFragment!!.setPresenter(presenter)
                    }
                    participateFragment!!
                }
                else -> {
                    if (favoriteFragment == null) {
                        favoriteFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_FAVORITE, user)
                        favoriteFragment!!.setPresenter(presenter)
                    }
                    favoriteFragment!!
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> getString(R.string.personal_tab_create)
                1 -> getString(R.string.personal_tab_participate)
                else -> getString(R.string.personal_tab_favorite)
            }
        }
    }

    companion object {
        private val TAG = UserActivity::class.java.simpleName
        private const val PERCENTAGE_TO_ANIMATE_AVATAR = 20
    }
}
