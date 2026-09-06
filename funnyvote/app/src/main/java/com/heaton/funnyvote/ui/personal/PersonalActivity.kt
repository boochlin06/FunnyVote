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
import com.google.android.material.appbar.AppBarLayout
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.databinding.ActivityPersonalBinding
import com.heaton.funnyvote.ui.createvote.CreateVoteActivity
import com.heaton.funnyvote.ui.main.MainPageContract
import com.heaton.funnyvote.ui.main.MainPageTabFragment
import com.heaton.funnyvote.utils.Util

class PersonalActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener, PersonalContract.UserPageView {

    private lateinit var binding: ActivityPersonalBinding
    private var personalCode: String? = null
    private var personalCodeType: String? = null
    private var personalName: String? = null
    private var personalIcon: String? = null
    private var isAvatarShown = true

    private var maxScrollSize: Int = 0
    private var tabsAdapter: TabsAdapter? = null

    private lateinit var presenter: MainPageContract.Presenter
    private var createFragment: MainPageTabFragment? = null
    private var favoriteFragment: MainPageTabFragment? = null

    private var tracker: Tracker? = null
    private var passwordDialog: AlertDialog? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker
        val targetUser = User()
        if (intent != null) {
            personalCode = intent.getStringExtra(EXTRA_PERSONAL_CODE)
            personalCodeType = intent.getStringExtra(EXTRA_PERSONAL_CODE_TYPE)
            personalName = intent.getStringExtra(EXTRA_PERSONAL_NAME)
            personalIcon = intent.getStringExtra(EXTRA_PERSONAL_ICON)
            targetUser.userCode = personalCode
            targetUser.personalTokenType = personalCodeType
            targetUser.userName = personalName
            targetUser.userIcon = personalIcon
        } else {
            finish()
        }
        tracker?.send(
            HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_PERSONAL)
                .setAction(AnalyzticsTag.ACTION_ENTER_PERSONAL_INFO)
                .setLabel(personalCode ?: "").build()
        )
        tabsAdapter = TabsAdapter(supportFragmentManager, User(), User())
        binding.toolbarSub.setNavigationOnClickListener { onBackPressed() }

        binding.appBarMain.addOnOffsetChangedListener(this)
        maxScrollSize = binding.appBarMain.totalScrollRange

        binding.tabLayoutPersonal.setupWithViewPager(binding.vpMain)
        tracker?.setScreenName(AnalyzticsTag.SCREEN_PERSONAL_CREATE)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
        binding.vpMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    tracker?.setScreenName(AnalyzticsTag.SCREEN_PERSONAL_CREATE)
                } else if (position == 1) {
                    tracker?.setScreenName(AnalyzticsTag.SCREEN_PERSONAL_FAVORITE)
                }
                tracker?.send(HitBuilders.ScreenViewBuilder().build())
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        presenter = UserPresenter(
            Injection.provideVoteDataRepository(applicationContext),
            Injection.provideUserRepository(applicationContext),
            this
        )
        (presenter as UserPresenter).setTargetUser(targetUser)
        presenter.start()
    }

    override fun onResume() {
        super.onResume()
        tracker?.setScreenName(AnalyzticsTag.SCREEN_PERSONAL)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
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
        binding.txtSubTitle.text = user.personalTokenType
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
        setUpTabsAdapter(user, User())
    }

    override fun setUpTabsAdapter(user: User, targetUser: User) {
        tabsAdapter = TabsAdapter(supportFragmentManager, user, targetUser)
        val currentItem = binding.vpMain.currentItem
        binding.vpMain.adapter = tabsAdapter
        binding.vpMain.currentItem = currentItem
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

    private inner class TabsAdapter(
        fm: FragmentManager,
        private val loginUser: User,
        private val targetUser: User
    ) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = 2

        override fun getItem(i: Int): Fragment {
            return when (i) {
                0 -> {
                    if (createFragment == null) {
                        createFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_CREATE, loginUser, targetUser)
                        createFragment!!.setPresenter(presenter)
                    }
                    createFragment!!
                }
                else -> {
                    if (favoriteFragment == null) {
                        favoriteFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_FAVORITE, loginUser, targetUser)
                        favoriteFragment!!.setPresenter(presenter)
                    }
                    favoriteFragment!!
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> getString(R.string.personal_tab_create)
                else -> getString(R.string.personal_tab_favorite)
            }
        }
    }

    companion object {
        private val TAG = PersonalActivity::class.java.simpleName
        private const val PERCENTAGE_TO_ANIMATE_AVATAR = 20
        const val EXTRA_PERSONAL_CODE = "personal_code"
        const val EXTRA_PERSONAL_CODE_TYPE = "personal_code_type"
        const val EXTRA_PERSONAL_NAME = "personal_name"
        const val EXTRA_PERSONAL_ICON = "personal_icon"
    }
}
