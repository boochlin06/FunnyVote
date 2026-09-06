package com.heaton.funnyvote.ui.votedetail

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import at.grabner.circleprogress.TextMode
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
import com.heaton.funnyvote.databinding.ActivityVoteDetailBinding
import com.heaton.funnyvote.databinding.DialogVoteDetailInfoBinding
import com.heaton.funnyvote.ui.HidingScrollListener
import com.heaton.funnyvote.utils.Util
import com.heaton.funnyvote.utils.Util.setupActionBar

class VoteDetailContentActivity : AppCompatActivity(), VoteDetailContract.View {

    companion object {
        private const val TITLE_EXTEND_MAX_LINE = 5
        private val TAG = VoteDetailContentActivity::class.java.simpleName
        var ENABLE_ADMOB = true
    }

    private lateinit var binding: ActivityVoteDetailBinding
    private var menu: Menu? = null
    private lateinit var searchView: SearchView
    private var newOptionPasswordDialog: AlertDialog? = null
    private var pollPasswordDialog: AlertDialog? = null
    private lateinit var optionItemAdapter: OptionItemAdapter
    private var data: VoteData = VoteData()
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
            tracker.send(
                HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(AnalyzticsTag.ACTION_SEARCH_OPTION)
                    .setLabel(newText).build()
            )
            return false
        }

        override fun onQueryTextSubmit(query: String): Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker

        binding.mainToolbar.mainToolbar.title = getString(R.string.vote_detail_title)
        binding.mainToolbar.mainToolbar.setTitleTextColor(Color.WHITE)
        binding.mainToolbar.mainToolbar.elevation = 10f
        setupActionBar(binding.mainToolbar.mainToolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        this.data.voteCode = if (Intent.ACTION_VIEW == intent.action) {
            val segment = intent.data?.lastPathSegment
            if (segment.isNullOrEmpty()) {
                Util.sendShareAppIntent(applicationContext)
                finish()
                ""
            } else {
                Log.d(TAG, "Link:${intent.data}, vote code:$segment")
                tracker.send(
                    HitBuilders.EventBuilder()
                        .setCategory("VOTE_DETAIL")
                        .setAction(AnalyzticsTag.ACTION_LINK_VOTE)
                        .setLabel(segment)
                        .build()
                )
                segment
            }
        } else {
            val code = intent.extras?.getString(Util.BUNDLE_KEY_VOTE_CODE) ?: ""
            if (code.isNotEmpty()) {
                Log.d(TAG, "Start activity vote code:$code")
                tracker.send(
                    HitBuilders.EventBuilder()
                        .setCategory("VOTE_DETAIL")
                        .setAction(AnalyzticsTag.ACTION_ENTER_VOTE)
                        .setLabel(code)
                        .build()
                )
            }
            code
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

        binding.circleLoad.setText(getString(R.string.vote_detail_circle_loading))
        binding.circleLoad.setTextMode(TextMode.TEXT)
        binding.circleLoad.isShowTextWhileSpinning = true
        binding.circleLoad.setFillCircleColor(ContextCompat.getColor(applicationContext, R.color.md_amber_50))
        ENABLE_ADMOB = resources.getBoolean(R.bool.enable_detail_admob)
        presenter = VoteDetailPresenter(
            this.data.voteCode,
            Injection.provideVoteDataRepository(this),
            Injection.provideUserRepository(this),
            this
        )
        presenter.start()
    }

    override fun onResume() {
        super.onResume()
        tracker.setScreenName(AnalyzticsTag.SCREEN_VOTE_DETAIL)
        tracker.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun setUpSubmit(optionType: Int) {
        menu?.let { m ->
            val submit = m.findItem(R.id.menu_submit)
            if (submit != null && optionType == OptionItemAdapter.OPTION_SHOW_RESULT) {
                submit.isVisible = false
            } else if (submit != null) {
                submit.isVisible = true
                val homeTarget = Target { ViewTarget(binding.mainToolbar.mainToolbar.findViewById(R.id.menu_submit)).point }
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
                            override fun onShowcaseViewDidHide(showcaseView: ShowcaseView) {}
                            override fun onShowcaseViewShow(showcaseView: ShowcaseView) {}
                            override fun onShowcaseViewTouchBlocked(motionEvent: MotionEvent) {}
                        })
                        .build()
                    showcaseView?.show()
                }
            }
        }
    }

    override fun setUpOptionAdapter(data: VoteData, optionType: Int, optionList: List<Option>) {
        optionItemAdapter = OptionItemAdapter(optionType, optionList, data, optionItemListener)
        binding.ryOptionArea.adapter = optionItemAdapter
    }

    override fun showHintToast(res: Int) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
    }

    override fun showMultiChoiceToast(max: Int, min: Int) {
        Toast.makeText(applicationContext, String.format(getString(R.string.vote_detail_dialog_multi_option), min, max), Toast.LENGTH_SHORT).show()
    }

    override fun showMultiChoiceAtLeast(min: Int) {
        Toast.makeText(applicationContext, String.format(getString(R.string.vote_detail_toast_option_at_least_min), min), Toast.LENGTH_LONG).show()
    }

    override fun showMultiChoiceOverMaxToast(max: Int) {
        Toast.makeText(applicationContext, String.format(getString(R.string.vote_detail_toast_option_over_max), max), Toast.LENGTH_SHORT).show()
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
        binding.ryOptionArea.smoothScrollToPosition(0)
        binding.appBarMain.setExpanded(true, true)
        tracker.send(
            HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                .setAction(AnalyzticsTag.ACTION_MOVE_TOP)
                .setLabel(data.voteCode).build()
        )
    }

    override fun updateSearchView(searchList: List<Option>, isSearchMode: Boolean) {
        optionItemAdapter.isSearchMode = isSearchMode
        optionItemAdapter.setSearchList(searchList)
        optionItemAdapter.notifyDataSetChanged()
        if (isSearchMode) {
            binding.appBarMain.setExpanded(false)
        } else {
            binding.appBarMain.setExpanded(true)
        }
    }

    fun onTitleExtendClick() {
        presenter.IntentToTitleDetail()
    }

    override fun showTitleDetailDialog(data: VoteData) {
        val titleDetail = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
        titleDetail.requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        titleDetail.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

        val content = LayoutInflater.from(applicationContext).inflate(R.layout.dialog_title_detail, null)
        val txtTitleDetail = content.findViewById<TextView>(R.id.txtTitleDetail)
        val txtAuthorName = content.findViewById<TextView>(R.id.txtAuthorName)
        val txtPubTime = content.findViewById<TextView>(R.id.txtPubTime)
        val imgAuthorIcon = content.findViewById<ImageView>(R.id.imgAuthorIcon)
        txtAuthorName.text = data.authorName
        txtPubTime.text = (Util.getDate(data.startTime, "yyyy/MM/dd HH:mm")
                + " ~ " + Util.getDate(data.endTime, "yyyy/MM/dd HH:mm"))
        if (data.authorIcon.isNullOrEmpty()) {
            if (!data.authorName.isNullOrEmpty()) {
                val size = resources.getDimension(R.dimen.vote_image_author_size).toInt()
                val drawable = Util.createRoundTextDrawable(data.authorName.substring(0, 1), ContextCompat.getColor(this, R.color.primary_light), size)
                imgAuthorIcon.setImageDrawable(drawable)
            } else {
                imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp)
            }
        } else {
            Glide.with(this)
                .load(data.authorIcon)
                .override(resources.getDimension(R.dimen.vote_image_author_size).toInt(), resources.getDimension(R.dimen.vote_image_author_size).toInt())
                .fitCenter()
                .into(imgAuthorIcon)
        }
        val imgCross = content.findViewById<ImageView>(R.id.imgCross)
        imgCross.setOnClickListener { titleDetail.dismiss() }
        txtTitleDetail.text = data.title
        titleDetail.setContentView(content)
        titleDetail.show()
    }

    override fun showCaseView() {
        val firstTimePref = Injection.provideFirstTimePref(this)
        if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_ENTER_UNPOLL_VOTE, true)) {
            val homeTarget = Target { ViewTarget(binding.mainToolbar.mainToolbar.findViewById(R.id.menu_submit)).point }
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
                    override fun onShowcaseViewDidHide(showcaseView: ShowcaseView) {}
                    override fun onShowcaseViewShow(showcaseView: ShowcaseView) {}
                    override fun onShowcaseViewTouchBlocked(motionEvent: MotionEvent) {}
                })
                .build()
            showcaseView?.show()
        }
    }

    override fun updateFavoriteView(isFavorite: Boolean) {
        binding.functionBar.imgBarFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp
        )
        tracker.send(
            HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                .setAction(if (isFavorite) AnalyzticsTag.ACTION_ADD_FAVORITE else AnalyzticsTag.ACTION_REMOVE_FAVORITE)
                .setLabel(data.voteCode)
                .build()
        )
    }

    override fun setUpAdMob(user: User) {
        if (ENABLE_ADMOB) {
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
        } else {
            binding.adView.visibility = View.GONE
        }
    }

    override fun setUpViews(voteData: VoteData, optionType: Int) {
        this.data = voteData
        binding.authorBar.txtAuthorName.text = voteData.authorName
        binding.authorBar.txtPubTime.text = (Util.getDate(voteData.startTime, "yyyy/MM/dd HH:mm")
                + " ~ " + Util.getDate(voteData.endTime, "yyyy/MM/dd HH:mm")
                + if (voteData.endTime < System.currentTimeMillis()) "  " + getString(R.string.wall_item_vote_end) else "")
        binding.txtTitle.text = voteData.title

        if (voteData.authorIcon.isNullOrEmpty()) {
            if (!voteData.authorName.isNullOrEmpty()) {
                val size = resources.getDimension(R.dimen.vote_image_author_size).toInt()
                val drawable = Util.createRoundTextDrawable(voteData.authorName.substring(0, 1), ContextCompat.getColor(this, R.color.primary_light), size)
                binding.authorBar.imgAuthorIcon.setImageDrawable(drawable)
            } else {
                binding.authorBar.imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp)
            }
        } else {
            Glide.with(this)
                .load(voteData.authorIcon)
                .override(resources.getDimension(R.dimen.vote_image_author_size).toInt(), resources.getDimension(R.dimen.vote_image_author_size).toInt())
                .fitCenter()
                .into(binding.authorBar.imgAuthorIcon)
        }
        if (VoteData.SECURITY_PUBLIC == voteData.security) {
            binding.authorBar.imgLock.visibility = View.INVISIBLE
        } else {
            binding.authorBar.imgLock.visibility = View.VISIBLE
        }
        binding.functionBar.txtBarPollCount.text = String.format(
            getString(R.string.wall_item_bar_vote_count), voteData.pollCount
        )

        binding.functionBar.imgBarFavorite.setImageResource(
            if (voteData.isFavorite) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp
        )

        Glide.with(this)
            .load(voteData.voteImage)
            .override(resources.getDimension(R.dimen.vote_detail_image_width).toInt(), resources.getDimension(R.dimen.vote_detail_image_high).toInt())
            .into(binding.imgMain)

        if (binding.txtTitle.lineCount >= TITLE_EXTEND_MAX_LINE) {
            binding.imgTitleExtend.visibility = View.VISIBLE
        } else {
            binding.imgTitleExtend.visibility = View.GONE
        }

        if (optionType == OptionItemAdapter.OPTION_SHOW_RESULT || !voteData.isCanPreviewResult) {
            binding.fabPreResult.visibility = View.GONE
        } else {
            binding.fabPreResult.visibility = View.VISIBLE
        }

        binding.ryOptionArea.addOnScrollListener(object : HidingScrollListener() {
            override fun onHide() {
                binding.famOther.collapse()
                binding.famOther.animate().translationY(
                    binding.famOther.height.toFloat()
                ).interpolator = AccelerateInterpolator(2f)
            }

            override fun onShow() {
                this.resetScrollDistance()
                binding.famOther.animate().translationY(0f).interpolator = DecelerateInterpolator(2f)
            }
        })
        binding.imgTitleExtend.setOnClickListener {
            onTitleExtendClick()
        }
        binding.functionBar.relBarFavorite.setOnClickListener {
            presenter.favoriteVote()
            tracker.send(
                HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(if (voteData.isFavorite) AnalyzticsTag.ACTION_ADD_FAVORITE else AnalyzticsTag.ACTION_REMOVE_FAVORITE)
                    .setLabel(voteData.voteCode)
                    .build()
            )
        }
        binding.functionBar.relBarShare.setOnClickListener {
            presenter.IntentToShareDialog()
        }
        binding.authorBar.imgAuthorIcon.setOnClickListener {
            presenter.IntentToAuthorDetail()
        }
        binding.authorBar.txtAuthorName.setOnClickListener {
            presenter.IntentToAuthorDetail()
        }
        binding.fabOptionSort.setOnClickListener {
            presenter.CheckSortOptionType()
            binding.famOther.collapse()
        }
        binding.fabTop.setOnClickListener {
            moveToTop()
            binding.famOther.collapse()
        }
        binding.fabPreResult.setOnClickListener {
            presenter.changeOptionType()
            tracker.send(
                HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(AnalyzticsTag.ACTION_CHANGE_MODE)
                    .setLabel(voteData.voteCode).build()
            )
            binding.famOther.collapse()
        }
    }

    override fun showResultOption(optionType: Int) {
        val currentFirstVisibleItem = (binding.ryOptionArea.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        binding.fabPreResult.title = getString(R.string.vote_detail_fab_return_poll)
        optionItemAdapter.setOptionType(optionType)
        binding.ryOptionArea.adapter = this.optionItemAdapter
        binding.ryOptionArea.scrollToPosition(currentFirstVisibleItem)
    }

    override fun showUnPollOption(optionType: Int) {
        val currentFirstVisibleItem = (binding.ryOptionArea.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        binding.fabPreResult.title = getString(R.string.vote_detail_fab_pre_result)
        optionItemAdapter.setOptionType(optionType)
        binding.ryOptionArea.adapter = this.optionItemAdapter
        binding.ryOptionArea.scrollToPosition(currentFirstVisibleItem)
    }

    override fun showLoadingCircle() {
        binding.circleLoad.visibility = View.VISIBLE
        binding.circleLoad.setText(getString(R.string.vote_detail_circle_loading))
        binding.circleLoad.spin()
    }

    override fun hideLoadingCircle() {
        binding.circleLoad.stopSpinning()
        binding.circleLoad.visibility = View.GONE
    }

    override fun showSortOptionDialog(data: VoteData) {
        val builder = AlertDialog.Builder(this)
        val allType: Array<String> = if (data.isCanPreviewResult) {
            arrayOf(
                getString(R.string.vote_detail_dialog_sort_default),
                getString(R.string.vote_detail_dialog_sort_alphabet),
                getString(R.string.vote_detail_dialog_sort_poll)
            )
        } else {
            arrayOf(
                getString(R.string.vote_detail_dialog_sort_default),
                getString(R.string.vote_detail_dialog_sort_alphabet)
            )
        }
        builder.setSingleChoiceItems(allType, this.sortType) { _, which -> sortType = which }
        builder.setPositiveButton(getString(R.string.vote_detail_dialog_sort_select)) { dialog, _ ->
            dialog.dismiss()
            presenter.sortOptions(sortType)
            tracker.send(
                HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(AnalyzticsTag.ACTION_SEARCH_OPTION)
                    .setLabel(allType[sortType]).build()
            )
        }
        builder.setTitle(getString(R.string.vote_detail_dialog_sort_option))
        builder.show()
    }

    override fun showPollPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.password_dialog)
        builder.setPositiveButton(applicationContext.resources.getString(R.string.vote_detail_dialog_password_input), null)
        builder.setNegativeButton(applicationContext.resources.getString(R.string.account_dialog_cancel), null)
        builder.setTitle(applicationContext.getString(R.string.vote_detail_dialog_password_title))
        pollPasswordDialog = builder.create()

        pollPasswordDialog!!.setOnShowListener { dialogInterface ->
            val password = (dialogInterface as AlertDialog).findViewById<EditText>(R.id.edtEnterPassword)
            val ok = dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE)
            ok.setOnClickListener {
                Log.d(TAG, "choice:${optionItemAdapter.choiceCodeList.size} vc:${data.voteCode} pw input:${password?.text}")
                presenter.pollVote(password?.text?.toString() ?: "")
                tracker.send(
                    HitBuilders.EventBuilder()
                        .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                        .setAction(AnalyzticsTag.ACTION_POLL_VOTE)
                        .setLabel(data.voteCode)
                        .build()
                )
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
            val password = pollPasswordDialog!!.findViewById<EditText>(R.id.edtEnterPassword)
            password?.selectAll()
            val shake = AnimationUtils.loadAnimation(applicationContext, R.anim.edittext_shake)
            password?.startAnimation(shake)
        }
    }

    override fun shakeAddNewOptionPasswordDialog() {
        if (newOptionPasswordDialog != null && newOptionPasswordDialog!!.isShowing) {
            val password = newOptionPasswordDialog!!.findViewById<EditText>(R.id.edtEnterPassword)
            password?.selectAll()
            val shake = AnimationUtils.loadAnimation(applicationContext, R.anim.edittext_shake)
            password?.startAnimation(shake)
        }
    }

    override fun showAddNewOptionPasswordDialog(newOptionText: String) {
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.password_dialog)
        builder.setPositiveButton(applicationContext.resources.getString(R.string.vote_detail_dialog_password_input), null)
        builder.setNegativeButton(applicationContext.resources.getString(R.string.account_dialog_cancel), null)
        builder.setTitle(applicationContext.getString(R.string.vote_detail_dialog_password_title))
        newOptionPasswordDialog = builder.create()

        newOptionPasswordDialog!!.setOnShowListener { dialogInterface ->
            val password = (dialogInterface as AlertDialog).findViewById<EditText>(R.id.edtEnterPassword)
            val ok = dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE)
            ok.setOnClickListener {
                Log.d(TAG, "New Option Text:$newOptionText vc:${data.voteCode} pw input:${password?.text}")
                presenter.addNewOptionCompleted(password?.text?.toString() ?: "", newOptionText)
                tracker.send(
                    HitBuilders.EventBuilder()
                        .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                        .setAction(AnalyzticsTag.ACTION_ADD_NEW_OPTION)
                        .setLabel(data.voteCode).build()
                )
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
        return when (item.itemId) {
            R.id.menu_submit -> {
                if (showcaseView != null && showcaseView!!.isShowing) {
                    showcaseView!!.hide()
                } else {
                    presenter.pollVote("")
                }
                true
            }
            R.id.menu_info -> {
                presenter.IntentToVoteInfo()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (binding.famOther.isExpanded) {
            binding.famOther.collapse()
        } else {
            if (::optionItemAdapter.isInitialized && optionItemAdapter.choiceList.isNotEmpty()) {
                showExitCheckDialog()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun showExitCheckDialog() {
        val exitDialog = AlertDialog.Builder(this)
        exitDialog.setTitle(R.string.vote_detail_dialog_exit_title)
        exitDialog.setMessage(R.string.vote_detail_dialog_exit_message)
        exitDialog.setNegativeButton(R.string.vote_detail_dialog_exit_button_leave) { _, _ -> super.onBackPressed() }
        exitDialog.setPositiveButton(R.string.vote_detail_dialog_exit_button_keep) { dialog, _ -> dialog.cancel() }
        exitDialog.show()
    }

    override fun showVoteInfoDialog(data: VoteData) {
        val infoBinding = DialogVoteDetailInfoBinding.inflate(LayoutInflater.from(this))
        if (!data.isMultiChoice) {
            infoBinding.txtOptionInfo.text = getString(R.string.vote_detail_dialog_single_option)
        } else {
            infoBinding.txtOptionInfo.text = String.format(getString(R.string.vote_detail_dialog_multi_option), data.minOption, data.maxOption)
        }
        val addOptionInfo = if (data.isUserCanAddOption) {
            "\n\n" + getString(R.string.vote_detail_dialog_can_add_option)
        } else {
            "\n\n" + getString(R.string.vote_detail_dialog_can_not_add_option)
        }
        infoBinding.txtOptionInfo.text = infoBinding.txtOptionInfo.text.toString() + addOptionInfo
        infoBinding.txtTime.text = (Util.getDate(data.startTime, "yyyy/MM/dd HH:mm")
                + " ~ " + Util.getDate(data.endTime, "yyyy/MM/dd HH:mm"))
        infoBinding.txtSecurity.text = VoteData.getSecurityString(applicationContext, data.security)

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.vote_detail_dialog_title_info))
        dialog.setView(infoBinding.root)
        dialog.setPositiveButton(getString(R.string.vote_detail_dialog_done)) { d, _ -> d.dismiss() }
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
}
