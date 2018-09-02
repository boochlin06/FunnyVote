package com.heaton.funnyvote.ui.votedetail

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import at.grabner.circleprogress.TextMode
import butterknife.OnClick
import com.amulyakhare.textdrawable.TextDrawable
import com.bumptech.glide.Glide
import com.github.amlcurran.showcaseview.OnShowcaseEventListener
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.Target
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FirstTimePref
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.ui.HidingScrollListener
import com.heaton.funnyvote.utils.Util
import com.heaton.funnyvote.utils.Util.setupActionBar
import kotlinx.android.synthetic.main.activity_vote_detail.*
import kotlinx.android.synthetic.main.dialog_vote_detail_info.view.*
import kotlinx.android.synthetic.main.include_author.*
import kotlinx.android.synthetic.main.include_function_bar.*
import kotlinx.android.synthetic.main.include_toolbar.*
import org.jetbrains.anko.toast

/**
 * Created by heaton on 2016/8/21.
 */

class VoteDetailContentActivity : AppCompatActivity(), VoteDetailContract.View {

    private var menu: Menu? = null
    private lateinit var searchView: SearchView
    private var newOptionPasswordDialog: AlertDialog? = null
    private var pollPasswordDialog: AlertDialog? = null
    private lateinit var optionItemAdapter: OptionItemAdapter
    private var data: VoteData = VoteData()
    //private var context: Activity? = null
    private var sortType = 0
    private lateinit var tracker: Tracker
    private var showcaseView: ShowcaseView? = null

    private lateinit var presenter: VoteDetailContract.Presenter
    private lateinit var optionItemListener: OptionItemListener

    override val isPasswordDialogShowing: Boolean
        get() {
            if (newOptionPasswordDialog != null && newOptionPasswordDialog!!.isShowing) {
                return true
            } else if (pollPasswordDialog != null && pollPasswordDialog!!.isShowing) {
                return true
            }
            return false
        }

    private val queryListener = object : SearchView.OnQueryTextListener {

        override fun onQueryTextChange(newText: String): Boolean {
            presenter.searchOption(newText)
            tracker.send(HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(AnalyzticsTag.ACTION_SEARCH_OPTION)
                    .setLabel(newText).build())
            return false
        }

        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote_detail)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker
        //context = this

        mainToolbar.title = getString(R.string.vote_detail_title)
        mainToolbar.setTitleTextColor(Color.WHITE)
        mainToolbar.elevation = 10f
        setupActionBar(mainToolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        this.data.voteCode = if (Intent.ACTION_VIEW == intent.action) {
            intent.data!!.lastPathSegment.apply {
                if (this.isNullOrEmpty()) {
                    Util.sendShareAppIntent(applicationContext)
                    finish()
                } else {
                    Log.d(TAG, "Link:" + intent.data!!.toString()
                            + ",vote code:" + intent.data!!.lastPathSegment)
                    tracker.send(HitBuilders.EventBuilder()
                            .setCategory("VOTE_DETAIL")
                            .setAction(AnalyzticsTag.ACTION_LINK_VOTE)
                            .setLabel(this)
                            .build())
                }
            }
        } else {
            intent.extras!!.getString(Util.BUNDLE_KEY_VOTE_CODE).apply {
                if (!this.isNullOrEmpty()) {
                    Log.d(TAG, "Start activity vote code:$this")
                    tracker.send(HitBuilders.EventBuilder()
                            .setCategory("VOTE_DETAIL")
                            .setAction(AnalyzticsTag.ACTION_ENTER_VOTE)
                            .setLabel(this)
                            .build())
                }
            }
        }

        optionItemListener = object : OptionItemListener {
            override fun onOptionExpand(optionCode: String) {
                presenter.resetOptionExpandStatus(optionCode)
            }

            override fun onOptionQuickPoll(optionId: Long, optionCode: String) {
                presenter.resetOptionChoiceStatus(optionId, optionCode)
                presenter.pollVote("")
            }

            override fun onOptionChoice(optionId: Long, optionCode: String) {
                presenter.resetOptionChoiceStatus(optionId, optionCode)
            }

            override fun onOptionTextChange(optionId: Long, newOptionText: String) {
                presenter.addNewOptionContentRevise(optionId, newOptionText)
            }

            override fun onOptionAddNew() {
                presenter.addNewOptionStart()
            }

            override fun onOptionAddNewCheck(newOptionText: String) {
                presenter.addNewOptionCompleted("", newOptionText)
            }

            override fun onOptionRemove(optionId: Long) {
                presenter.removeOption(optionId)
            }
        }

        circleLoad.setText(getString(R.string.vote_detail_circle_loading))
        circleLoad.setTextMode(TextMode.TEXT)
        circleLoad.isShowTextWhileSpinning = true
        circleLoad.setFillCircleColor(ContextCompat.getColor(applicationContext, R.color.md_amber_50))
        ENABLE_ADMOB = resources.getBoolean(R.bool.enable_detail_admob)
        presenter = VoteDetailPresenter(this.data.voteCode, Injection.provideVoteDataRepository(this)
                , Injection.provideUserRepository(this), this)
        presenter.start()

    }

    override fun onResume() {
        super.onResume()
        tracker.setScreenName(AnalyzticsTag.SCREEN_VOTE_DETAIL)
        tracker.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun setUpSubmit(optionType: Int) {
        if (menu != null) {
            val submit = menu!!.findItem(R.id.menu_submit)
            if (submit != null && optionType == OptionItemAdapter.OPTION_SHOW_RESULT) {
                submit.isVisible = false
            } else {
                submit!!.isVisible = true
                val homeTarget = Target { ViewTarget(mainToolbar.findViewById(R.id.menu_submit)).point }
                val firstTimePref = Injection.provideFirstTimePref(this)

                if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_ENTER_UNPOLL_VOTE, true)) {
                    showcaseView = ShowcaseView.Builder(this)
                            .setTarget(homeTarget)
                            .withHoloShowcase()
                            .setStyle(R.style.CustomShowcaseTheme)
                            .setContentTitle(getString(R.string.vote_detail_case_view_title))
                            .setContentText(getString(R.string.vote_detail_case_view_content))
                            .setShowcaseEventListener(object : OnShowcaseEventListener {
                                override fun onShowcaseViewHide(showcaseView: ShowcaseView) {
                                    firstTimePref.edit().putBoolean(FirstTimePref.SP_FIRST_ENTER_UNPOLL_VOTE, false).apply()
                                }

                                override fun onShowcaseViewDidHide(showcaseView: ShowcaseView) {

                                }

                                override fun onShowcaseViewShow(showcaseView: ShowcaseView) {

                                }

                                override fun onShowcaseViewTouchBlocked(motionEvent: MotionEvent) {

                                }
                            })
                            .build()
                    showcaseView!!.show()
                }
            }
        }
    }

    override fun setUpOptionAdapter(data: VoteData, optionType: Int, optionList: List<Option>) {
        optionItemAdapter = OptionItemAdapter(optionType, optionList, data, optionItemListener)
        ryOptionArea.adapter = optionItemAdapter
    }

    override fun showHintToast(res: Int) {
        toast(res);
    }

    override fun showMultiChoiceToast(max: Int, min: Int) {
        Toast.makeText(applicationContext, String.format(getString(R.string.vote_detail_dialog_multi_option), min, max), Toast.LENGTH_SHORT).show()
    }

    override fun showMultiChoiceAtLeast(min: Int) {
        Toast.makeText(applicationContext, String.format(getString(R.string.vote_detail_toast_option_at_least_min), min), Toast.LENGTH_LONG).show()
    }

    override fun showMultiChoiceOverMaxToast(max: Int) {
        Toast.makeText(applicationContext, String.format(this.getString(R.string.vote_detail_toast_option_over_max), max), Toast.LENGTH_SHORT).show()
    }

    override fun refreshOptions() {
        optionItemAdapter.notifyDataSetChanged()
    }

    override fun updateChoiceOptions(choiceList: List<Long>) {
        optionItemAdapter.choiceList = choiceList
        optionItemAdapter.notifyDataSetChanged()
    }

    override fun updateExpandOptions(expandList: List<String>) {
        optionItemAdapter.setExpandOptionList(expandList)
        optionItemAdapter.notifyDataSetChanged()
    }

    override fun updateCurrentOptionsOrder(optionList: List<Option>) {
        optionItemAdapter.setOptionList(optionList)
        optionItemAdapter.notifyDataSetChanged()
    }

    override fun showShareDialog(data: VoteData) {
        Util.sendShareIntent(this, data)
    }

    override fun showAuthorDetail(data: VoteData) {
        Util.sendPersonalDetailIntent(this, data)
    }

    override fun moveToTop() {
        ryOptionArea.smoothScrollToPosition(0)
        appBarMain.setExpanded(true, true)
        tracker.send(HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                .setAction(AnalyzticsTag.ACTION_MOVE_TOP)
                .setLabel(data.voteCode).build())
    }

    override fun updateSearchView(searchList: List<Option>, isSearchMode: Boolean) {
        optionItemAdapter.isSearchMode = isSearchMode
        optionItemAdapter.setSearchList(searchList)
        optionItemAdapter.notifyDataSetChanged()
        if (isSearchMode) {
            appBarMain.setExpanded(false)
        } else {
            appBarMain.setExpanded(true)
        }
    }


    @OnClick(R.id.imgTitleExtend)
    fun onTitleExtendClick() {
        presenter.IntentToTitleDetail()
    }

    override fun showTitleDetailDialog(data: VoteData) {
        val titleDetail = Dialog(this@VoteDetailContentActivity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
        titleDetail.requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        titleDetail.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)

        val content = LayoutInflater.from(applicationContext).inflate(R.layout.dialog_title_detail, null)
        val txtTitleDetail = content.findViewById<View>(R.id.txtTitleDetail) as TextView
        val txtAuthorName = content.findViewById<View>(R.id.txtAuthorName) as TextView
        val txtPubTime = content.findViewById<View>(R.id.txtPubTime) as TextView
        val imgAuthorIcon = content.findViewById<View>(R.id.imgAuthorIcon) as ImageView
        txtAuthorName.text = data.authorName
        txtPubTime.text = (Util.getDate(data.startTime, "yyyy/MM/dd hh:mm")
                + " ~ " + Util.getDate(data.endTime, "yyyy/MM/dd hh:mm"))
        if (data.authorIcon == null || data.authorIcon.isEmpty()) {
            if (data.authorName != null && !data.authorName.isEmpty()) {
                val drawable = TextDrawable.builder().beginConfig()
                        .width(resources.getDimension(R.dimen.vote_image_author_size).toInt())
                        .height(resources.getDimension(R.dimen.vote_image_author_size).toInt()).endConfig()
                        .buildRound(data.authorName.substring(0, 1), R.color.primary_light)
                imgAuthorIcon.setImageDrawable(drawable)
            } else {
                imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp)
            }
        } else {
            Glide.with(this)
                    .load(data.authorIcon)
                    .override(resources.getDimension(R.dimen.vote_image_author_size).toInt(), resources.getDimension(R.dimen.vote_image_author_size).toInt())
                    .fitCenter()
                    .crossFade()
                    .into(imgAuthorIcon)
        }
        val imgCross = content.findViewById<View>(R.id.imgCross) as ImageView
        imgCross.setOnClickListener { titleDetail.dismiss() }
        txtTitleDetail.text = data.title
        titleDetail.setContentView(content)
        titleDetail.show()
    }

    override fun showCaseView() {
        val firstTimePref = Injection.provideFirstTimePref(this)

        if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_ENTER_UNPOLL_VOTE, true)) {
            val homeTarget = Target { ViewTarget(mainToolbar.findViewById(R.id.menu_submit)).point }
            showcaseView = ShowcaseView.Builder(this)
                    .setTarget(homeTarget)
                    .withHoloShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(getString(R.string.vote_detail_case_view_title))
                    .setContentText(getString(R.string.vote_detail_case_view_content))
                    .setShowcaseEventListener(object : OnShowcaseEventListener {
                        override fun onShowcaseViewHide(showcaseView: ShowcaseView) {
                            firstTimePref.edit().putBoolean(FirstTimePref.SP_FIRST_ENTER_UNPOLL_VOTE, false).apply()
                        }

                        override fun onShowcaseViewDidHide(showcaseView: ShowcaseView) {

                        }

                        override fun onShowcaseViewShow(showcaseView: ShowcaseView) {

                        }

                        override fun onShowcaseViewTouchBlocked(motionEvent: MotionEvent) {

                        }
                    })
                    .build()
            showcaseView!!.show()
        }
    }

    override fun updateFavoriteView(isFavorite: Boolean) {
        imgBarFavorite.setImageResource(if (isFavorite)
            R.drawable.ic_star_24dp
        else
            R.drawable.ic_star_border_24dp)
        tracker.send(HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                .setAction(if (data.isFavorite)
                    AnalyzticsTag.ACTION_ADD_FAVORITE
                else
                    AnalyzticsTag.ACTION_REMOVE_FAVORITE)
                .setLabel(data.voteCode)
                .build())
    }

    override fun setUpAdMob(user: User) {
        if (ENABLE_ADMOB) {
            val adRequest = AdRequest.Builder()
                    .setGender(if (user.userCode.isNotBlank() && User.GENDER_MALE == user.gender)
                        AdRequest.GENDER_MALE
                    else
                        AdRequest.GENDER_FEMALE)
                    .build()
            adView.loadAd(adRequest)
        } else {
            adView.visibility = View.GONE
        }
    }

    override fun setUpViews(voteData: VoteData, optionType: Int) {
        txtAuthorName.text = voteData.authorName
        txtPubTime.text = (Util.getDate(voteData.startTime, "yyyy/MM/dd hh:mm")
                + " ~ " + Util.getDate(voteData.endTime, "yyyy/MM/dd hh:mm")
                + if (voteData.endTime < System.currentTimeMillis()) "  " + getString(R.string.wall_item_vote_end) else "")
        txtTitle.text = voteData.title

        if (voteData.authorIcon == null || voteData.authorIcon.isEmpty()) {
            if (voteData.authorName != null && !voteData.authorName.isEmpty()) {
                val drawable = TextDrawable.builder().beginConfig()
                        .width(resources.getDimension(R.dimen.vote_image_author_size).toInt())
                        .height(resources.getDimension(R.dimen.vote_image_author_size).toInt()).endConfig()
                        .buildRound(voteData.authorName.substring(0, 1), R.color.primary_light)
                imgAuthorIcon.setImageDrawable(drawable)
            } else {
                imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp)
            }
        } else {
            Glide.with(this)
                    .load(voteData.authorIcon)
                    .override(resources.getDimension(R.dimen.vote_image_author_size).toInt(), resources.getDimension(R.dimen.vote_image_author_size).toInt())
                    .fitCenter()
                    .crossFade()
                    .into(imgAuthorIcon)
        }
        if (VoteData.SECURITY_PUBLIC == voteData.security) {
            imgLock.visibility = View.INVISIBLE
        } else {
            imgLock.visibility = View.VISIBLE
        }
        txtBarPollCount.text = String.format(this
                .getString(R.string.wall_item_bar_vote_count), voteData.pollCount)

        imgBarFavorite.setImageResource(if (voteData.isFavorite)
            R.drawable.ic_star_24dp
        else
            R.drawable.ic_star_border_24dp)

        Glide.with(this)
                .load(voteData.voteImage)
                .override(resources.getDimension(R.dimen.vote_detail_image_width).toInt()
                        , resources.getDimension(R.dimen.vote_detail_image_high).toInt())
                .dontAnimate()
                .crossFade()
                .into(imgMain)

        if (txtTitle.lineCount >= TITLE_EXTEND_MAX_LINE) {
            imgTitleExtend.visibility = View.VISIBLE
        } else {
            imgTitleExtend.visibility = View.GONE
        }

        if (optionType == OptionItemAdapter.OPTION_SHOW_RESULT || !voteData.isCanPreviewResult)
            fabPreResult.visibility = View.GONE
        else {
            fabPreResult.visibility = View.VISIBLE
        }

        ryOptionArea.addOnScrollListener(object : HidingScrollListener() {
            override fun onHide() {
                famOther.collapse()
                famOther.animate().translationY(
                        famOther.height.toFloat()).interpolator = AccelerateInterpolator(2f)
            }

            override fun onShow() {
                this.resetScrollDistance()
                famOther.animate().translationY(0f).interpolator = DecelerateInterpolator(2f)
            }
        })
        relBarFavorite.setOnClickListener {
            presenter.favoriteVote()
            tracker.send(HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(if (voteData.isFavorite)
                        AnalyzticsTag.ACTION_ADD_FAVORITE
                    else
                        AnalyzticsTag.ACTION_REMOVE_FAVORITE)
                    .setLabel(voteData.voteCode)
                    .build())
        }
        relBarShare.setOnClickListener {
            presenter.IntentToShareDialog()
        }
        imgAuthorIcon.setOnClickListener {
            presenter.IntentToAuthorDetail()
        }
        txtAuthorName.setOnClickListener {
            presenter.IntentToAuthorDetail()
        }
        fabOptionSort.setOnClickListener {
            presenter.CheckSortOptionType()
            famOther.collapse()
        }
        fabTop.setOnClickListener {
            moveToTop()
            famOther.collapse()
        }
        fabPreResult.setOnClickListener {
            presenter.changeOptionType()
            tracker.send(HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(AnalyzticsTag.ACTION_CHANGE_MODE)
                    .setLabel(voteData.voteCode).build())
            famOther.collapse()
        }
    }

    override fun showResultOption(optionType: Int) {
        val currentFirstVisibleItem = (ryOptionArea.layoutManager as LinearLayoutManager)
                .findFirstVisibleItemPosition()
        fabPreResult.title = getString(R.string.vote_detail_fab_return_poll)
        optionItemAdapter.setOptionType(optionType)
        ryOptionArea.adapter = this.optionItemAdapter
        ryOptionArea.scrollToPosition(currentFirstVisibleItem)
        // Todo: set animation to make transfer funny and smooth.
    }

    override fun showUnPollOption(optionType: Int) {
        val currentFirstVisibleItem = (ryOptionArea.layoutManager as LinearLayoutManager)
                .findFirstVisibleItemPosition()
        fabPreResult.title = getString(R.string.vote_detail_fab_pre_result)
        optionItemAdapter.setOptionType(optionType)
        ryOptionArea.adapter = this.optionItemAdapter
        ryOptionArea.scrollToPosition(currentFirstVisibleItem)
        // Todo: set animation to make transfer funny and smooth.
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

    override fun showSortOptionDialog(data: VoteData) {
        val builder = AlertDialog.Builder(this)
        val allType: Array<String> = if (data.isCanPreviewResult) {
            arrayOf(getString(R.string.vote_detail_dialog_sort_default)
                    , getString(R.string.vote_detail_dialog_sort_alphabet)
                    , getString(R.string.vote_detail_dialog_sort_poll))
        } else {
            arrayOf(getString(R.string.vote_detail_dialog_sort_default)
                    , getString(R.string.vote_detail_dialog_sort_alphabet))
        }
        builder.setSingleChoiceItems(allType, this.sortType) { _, which -> sortType = which }
        builder.setPositiveButton(getString(R.string.vote_detail_dialog_sort_select)) { dialog, _ ->
            dialog.dismiss()
            presenter.sortOptions(sortType)
            tracker.send(HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(AnalyzticsTag.ACTION_SEARCH_OPTION)
                    .setLabel(allType[sortType]).build())
        }
        builder.setTitle(getString(R.string.vote_detail_dialog_sort_option))
        builder.show()
    }

    override fun showPollPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.password_dialog)
        builder.setPositiveButton(applicationContext.resources
                .getString(R.string.vote_detail_dialog_password_input), null)
        builder.setNegativeButton(applicationContext.resources
                .getString(R.string.account_dialog_cancel), null)
        builder.setTitle(applicationContext.getString(R.string.vote_detail_dialog_password_title))
        pollPasswordDialog = builder.create()

        pollPasswordDialog!!.setOnShowListener { dialogInterface ->
            val password = (dialogInterface as AlertDialog).findViewById<View>(R.id.edtEnterPassword) as EditText?
            val ok = dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE)
            ok.setOnClickListener {
                Log.d(TAG, "choice:" + optionItemAdapter.choiceCodeList.size + " vc:" + data.voteCode
                        + " pw input:" + password!!.text.toString())
                presenter.pollVote(password.text.toString())
                tracker.send(HitBuilders.EventBuilder()
                        .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                        .setAction(AnalyzticsTag.ACTION_POLL_VOTE)
                        .setLabel(data.voteCode)
                        .build())
            }
        }
        pollPasswordDialog!!.show()
    }

    override fun hidePollPasswordDialog() {
        if (pollPasswordDialog != null && pollPasswordDialog!!.isShowing) {
            pollPasswordDialog!!.dismiss()
        }
    }

    override fun shakePollPasswordDialog() {
        if (pollPasswordDialog != null && pollPasswordDialog!!.isShowing) {
            val password = pollPasswordDialog!!.findViewById<View>(R.id.edtEnterPassword) as EditText?
            password!!.selectAll()
            val shake = AnimationUtils.loadAnimation(applicationContext, R.anim.edittext_shake)
            password.startAnimation(shake)
        }
    }

    override fun shakeAddNewOptionPasswordDialog() {
        if (newOptionPasswordDialog != null && newOptionPasswordDialog!!.isShowing) {
            val password = newOptionPasswordDialog!!.findViewById<View>(R.id.edtEnterPassword) as EditText?
            password!!.selectAll()
            val shake = AnimationUtils.loadAnimation(applicationContext, R.anim.edittext_shake)
            password.startAnimation(shake)
        }
    }

    override fun showAddNewOptionPasswordDialog(newOptionText: String) {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.password_dialog)
        builder.setPositiveButton(applicationContext.resources
                .getString(R.string.vote_detail_dialog_password_input), null)
        builder.setNegativeButton(applicationContext.resources
                .getString(R.string.account_dialog_cancel), null)
        builder.setTitle(applicationContext.getString(R.string.vote_detail_dialog_password_title) + "test")
        newOptionPasswordDialog = builder.create()

        newOptionPasswordDialog!!.setOnShowListener { dialogInterface ->
            val password = (dialogInterface as AlertDialog).findViewById<View>(R.id.edtEnterPassword) as EditText?
            val ok = dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE)
            ok.setOnClickListener {
                Log.d(TAG, "New Option Text:" + newOptionText + " vc:" + data.voteCode
                        + " pw input:" + password!!.text.toString())
                presenter.addNewOptionCompleted(password.text.toString(), newOptionText)
                tracker.send(HitBuilders.EventBuilder()
                        .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                        .setAction(AnalyzticsTag.ACTION_ADD_NEW_OPTION)
                        .setLabel(data.voteCode).build())
            }
        }
        newOptionPasswordDialog!!.show()
    }

    override fun hideAddNewOptionPasswordDialog() {
        if (newOptionPasswordDialog != null && newOptionPasswordDialog!!.isShowing) {
            newOptionPasswordDialog!!.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_content_detail, menu)
        this.menu = menu
        searchView = menu.findItem(R.id.menu_search).actionView as SearchView
        searchView.setIconifiedByDefault(true)
        searchView.queryHint = getString(R.string.vote_detail_menu_search_hint)
        searchView.setOnQueryTextListener(queryListener)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.menu_submit) {
            //Log.d(TAG, "SHOWCASEVIEW:" + (showcaseView == null) + ", showing:" + showcaseView.isShowing());
            if (showcaseView != null && showcaseView!!.isShowing) {
                showcaseView!!.hide()
            } else {
                presenter.pollVote("")
            }
            return true
        } else if (id == R.id.menu_info) {
            presenter.IntentToVoteInfo()
        } else if (id == android.R.id.home) {
            finish()
        }
        return true
    }

    override fun onBackPressed() = if (famOther.isExpanded) {
        famOther.collapse()
    } else {
        if (optionItemAdapter.choiceList.isNotEmpty()) {
            showExitCheckDialog()
        } else {
            super.onBackPressed()
        }
    }

    override fun showExitCheckDialog() {
        val exitDialog = AlertDialog.Builder(this@VoteDetailContentActivity)
        exitDialog.setTitle(R.string.vote_detail_dialog_exit_title)
        exitDialog.setMessage(R.string.vote_detail_dialog_exit_message)
        exitDialog.setNegativeButton(R.string.vote_detail_dialog_exit_button_leave) { _, _ -> super@VoteDetailContentActivity.onBackPressed() }
        exitDialog.setPositiveButton(R.string.vote_detail_dialog_exit_button_keep) { dialog, _ -> dialog.cancel() }
        exitDialog.show()
    }

    override fun showVoteInfoDialog(data: VoteData) {
        val content = LayoutInflater.from(this).inflate(R.layout.dialog_vote_detail_info, null)
        if (!data.isMultiChoice) {
            content.txtOptionInfo.text = getString(R.string.vote_detail_dialog_single_option)
        } else {
            val multi = String.format(getString(R.string.vote_detail_dialog_multi_option)
                    , data.minOption, data.maxOption)
            content.txtOptionInfo.text = multi
        }
        if (data.isUserCanAddOption) {
            content.txtOptionInfo.text = content.txtOptionInfo.text
            "\n\n" + getString(R.string.vote_detail_dialog_can_add_option)
        } else {
            content.txtOptionInfo.text = content.txtOptionInfo.text
            "\n\n" + getString(R.string.vote_detail_dialog_can_not_add_option)
        }
        content.txtTime.text = (Util.getDate(data.startTime, "yyyy/MM/dd hh:mm")
                + " ~ " + Util.getDate(data.endTime, "yyyy/MM/dd hh:mm"))
        content.txtSecurity.text = VoteData.getSecurityString(applicationContext, data.security)
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.vote_detail_dialog_title_info))
        dialog.setView(content)
        dialog.setPositiveButton(getString(R.string.vote_detail_dialog_done)
        ) { dialog, _ -> dialog.dismiss() }
        dialog.show()
    }

    override fun setPresenter(presenter: VoteDetailContract.Presenter) {
        this.presenter = presenter
    }

    interface OptionItemListener {
        fun onOptionExpand(optionCode: String)

        fun onOptionQuickPoll(optionId: Long, optionCode: String)

        fun onOptionChoice(optionId: Long, optionCode: String)

        fun onOptionTextChange(optionId: Long, newOptionText: String)

        fun onOptionAddNew()

        fun onOptionAddNewCheck(newOptionText: String)

        fun onOptionRemove(optionId: Long)
    }

    companion object {

        private val TITLE_EXTEND_MAX_LINE = 5
        private val TAG = VoteDetailContentActivity::class.java.simpleName
        var ENABLE_ADMOB = true
    }
}
