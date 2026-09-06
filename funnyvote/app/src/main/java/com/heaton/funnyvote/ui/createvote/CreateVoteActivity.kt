package com.heaton.funnyvote.ui.createvote

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import at.grabner.circleprogress.TextMode
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.databinding.ActivityCteateVoteBinding
import com.heaton.funnyvote.utils.FileUtils
import com.heaton.funnyvote.utils.Util
import com.theartofdev.edmodo.cropper.CropImage

class CreateVoteActivity : AppCompatActivity(), CreateVoteContract.ActivityView {

    private lateinit var binding: ActivityCteateVoteBinding
    private var settingFragment: CreateVoteTabSettingFragment? = null
    private var optionFragment: CreateVoteTabOptionFragment? = null
    private var cropImageUri: Uri? = null
    private var tracker: Tracker? = null
    private lateinit var presenter: CreateVoteContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCteateVoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker

        binding.mainToolbar.title = getString(R.string.create_vote_toolbar_title)
        binding.mainToolbar.setTitleTextColor(Color.WHITE)
        binding.mainToolbar.elevation = 10f

        binding.circleLoad.setTextMode(TextMode.TEXT)
        binding.circleLoad.isShowTextWhileSpinning = true
        binding.circleLoad.setFillCircleColor(ContextCompat.getColor(this, R.color.md_amber_50))

        binding.mainToolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(binding.mainToolbar)

        tracker?.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE_OPTIONS)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
        binding.vpSubArea.adapter = TabsAdapter(supportFragmentManager)
        binding.vpSubArea.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    tracker?.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE_OPTIONS)
                } else if (position == 1) {
                    tracker?.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE_SETTINGS)
                }
                tracker?.send(HitBuilders.ScreenViewBuilder().build())
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.tabLayoutCreateVote.setupWithViewPager(binding.vpSubArea)
        presenter = CreateVoteActivityPresenter(
            Injection.provideVoteDataRepository(applicationContext),
            Injection.provideUserRepository(applicationContext),
            this,
            optionFragment,
            settingFragment
        )
        this.setPresenter(presenter)
        presenter.start()
    }

    override fun setPresenter(presenter: CreateVoteContract.Presenter) {
        this.presenter = presenter
    }

    override fun showHintToast(res: Int) {
        Toast.makeText(this, res, Toast.LENGTH_LONG).show()
    }

    override fun showHintToast(res: Int, arg: Long) {
        Toast.makeText(this, getString(res, arg), Toast.LENGTH_LONG).show()
    }

    override fun IntentToVoteDetail(voteData: VoteData) {
        Util.startActivityToVoteDetail(applicationContext, voteData.voteCode)
        binding.circleLoad.postDelayed({ Util.sendShareIntent(applicationContext, voteData) }, 1000)
        tracker?.send(
            HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_CREATE_VOTE)
                .setAction(AnalyzticsTag.ACTION_CREATE_VOTE)
                .setLabel(voteData.voteCode)
                .build()
        )
        finish()
    }

    private inner class TabsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = 2

        override fun getItem(i: Int): Fragment {
            return when (i) {
                0 -> {
                    if (optionFragment == null) {
                        optionFragment = CreateVoteTabOptionFragment.newTabFragment()
                        optionFragment!!.setPresenter(presenter)
                    }
                    optionFragment!!
                }
                else -> {
                    if (settingFragment == null) {
                        settingFragment = CreateVoteTabSettingFragment.newTabFragment()
                        settingFragment!!.setPresenter(presenter)
                    }
                    settingFragment!!
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> getString(R.string.create_vote_tab_options)
                else -> getString(R.string.create_vote_tab_settings)
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        tracker?.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE)
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_vote, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_submit -> {
                presenter.updateVoteTitle(binding.edtTitle.text.toString())
                presenter.submitCreateVote()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(this, data)
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                cropImageUri = imageUri
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE)
            } else {
                startCropImageActivity(imageUri)
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                cropImageUri = resultUri
                binding.vpSubArea.adapter = TabsAdapter(supportFragmentManager)
                val file = if (cropImageUri == null) null else FileUtils.getFile(this, cropImageUri)
                file?.let { presenter.updateVoteImage(it) }
                optionFragment?.setVoteImage(resultUri)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCropImageActivity(cropImageUri)
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (cropImageUri != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCropImageActivity(cropImageUri)
            } else {
                Toast.makeText(this, R.string.create_vote_toast_image_permission, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCropImageActivity(imageUri: Uri?) {
        if (imageUri == null) return
        CropImage.activity(imageUri)
            .setActivityTitle(getString(R.string.create_vote_error_crop_image_title))
            .setMaxCropResultSize(Util.convertDpToPixel((320 * 2).toFloat(), this).toInt(), Util.convertDpToPixel((150 * 2).toFloat(), this).toInt())
            .setMinCropResultSize(Util.convertDpToPixel(320f, this).toInt(), Util.convertDpToPixel(150f, this).toInt())
            .start(this)
    }

    override fun onBackPressed() {
        if (binding.edtTitle.text.toString().isNotEmpty()) {
            showExitCheckDialog()
        } else {
            super.onBackPressed()
        }
    }

    override fun showExitCheckDialog() {
        val exitDialog = AlertDialog.Builder(this)
        exitDialog.setTitle(R.string.create_vote_dialog_exit_title)
        exitDialog.setMessage(R.string.create_vote_dialog_exit_message)
        exitDialog.setNegativeButton(R.string.create_vote_dialog_exit_button_leave) { _, _ -> super.onBackPressed() }
        exitDialog.setPositiveButton(R.string.create_vote_dialog_exit_button_keep) { dialog, _ -> dialog.cancel() }
        exitDialog.show()
    }

    override fun showLoadingCircle() {
        binding.circleLoad.visibility = View.VISIBLE
        binding.circleLoad.setText(getString(R.string.vote_detail_circle_updating))
        binding.circleLoad.spin()
    }

    override fun hideLoadingCircle() {
        binding.circleLoad.stopSpinning()
        binding.circleLoad.visibility = View.GONE
    }

    override fun showCreateVoteError(errorMap: Map<String, Boolean>) {
        val sb = StringBuilder()
        Log.e(TAG, "ERROR CHECK MAP : $errorMap")
        var errorNumber = 0
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_ENDTIME_MORE_THAN_NOW)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_endtime_more_than_now) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MAX_SMALL_THAN_TOTAL)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_max_smaller_than_total) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MAX_SAMLL_THAN_MIN)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_max_smaller_than_min) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MIN_0)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_min_option_0) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MAX_0)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_max_option_0) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_USER_CODE_ERROR)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_error_user_code) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_TITLE_EMPTY)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_title_empty) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_DUPLICATE)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_title_duplicate) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_ENDTIME_MORE_THAN_MAX)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_endtime_more_than_max) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_FILL_ALL_OPTION)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_fill_all) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_PASSWORD_EMPTY)) {
            errorNumber++
            sb.append("$errorNumber. " + getString(R.string.create_vote_error_hint_password_empty) + "\n")
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.create_vote_dialog_error_title)
        builder.setMessage(sb.toString())
        builder.setPositiveButton(R.string.create_vote_dialog_error_done, null)
        builder.show()
    }

    companion object {
        var TAG = CreateVoteActivity::class.java.simpleName
    }
}
