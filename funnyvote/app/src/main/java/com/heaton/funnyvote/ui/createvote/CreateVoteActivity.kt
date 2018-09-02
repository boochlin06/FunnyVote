package com.heaton.funnyvote.ui.createvote

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import at.grabner.circleprogress.TextMode
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.utils.FileUtils
import com.heaton.funnyvote.utils.Util
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_cteate_vote.*

//import butterknife.BindView
//import butterknife.ButterKnife

/**
 * Created by heaton on 16/1/10.
 */
class CreateVoteActivity : AppCompatActivity(), CreateVoteContract.ActivityView {

    private var settingFragment: CreateVoteTabSettingFragment? = null
    private var optionFragment: CreateVoteTabOptionFragment? = null
    private var cropImageUri: Uri? = null
    private var tracker: Tracker? = null
    private lateinit var presenter: CreateVoteContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cteate_vote)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker

        mainToolbar.title = getString(R.string.create_vote_toolbar_title)
        mainToolbar.setTitleTextColor(Color.WHITE)
        mainToolbar.elevation = 10f

        circleLoad.setTextMode(TextMode.TEXT)
        circleLoad.isShowTextWhileSpinning = true
        circleLoad.setFillCircleColor(ContextCompat.getColor(this, R.color.md_amber_50))

        mainToolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(mainToolbar)

        tracker!!.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE_OPTIONS)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
        vpSubArea.adapter = TabsAdapter(supportFragmentManager)
        vpSubArea.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    tracker!!.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE_OPTIONS)
                } else if (position == 1) {
                    tracker!!.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE_SETTINGS)
                }
                tracker!!.send(HitBuilders.ScreenViewBuilder().build())
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        tabLayoutCreateVote!!.setupWithViewPager(vpSubArea)
        presenter = CreateVoteActivityPresenter(
                Injection.provideVoteDataRepository(applicationContext)
                , Injection.provideUserRepository(applicationContext)
                , this, optionFragment, settingFragment)
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
        Toast.makeText(this, getString(res, *arrayOf<Any>(arg)), Toast.LENGTH_LONG).show()
    }

    override fun IntentToVoteDetail(voteData: VoteData) {
        Util.startActivityToVoteDetail(applicationContext, voteData.voteCode)
        circleLoad.postDelayed({ Util.sendShareIntent(applicationContext, voteData) }, 1000)
        tracker!!.send(HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_CREATE_VOTE)
                .setAction(AnalyzticsTag.ACTION_CREATE_VOTE)
                .setLabel(voteData.voteCode)
                .build())
        finish()
    }

    private inner class TabsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(i: Int): Fragment? {
            when (i) {
                0 -> {
                    if (optionFragment == null) {
                        optionFragment = CreateVoteTabOptionFragment.newTabFragment()
                        optionFragment!!.setPresenter(presenter)
                    }
                    return optionFragment
                }
                1 -> {
                    if (settingFragment == null) {
                        settingFragment = CreateVoteTabSettingFragment.newTabFragment()
                        settingFragment!!.setPresenter(presenter)
                    }
                    return settingFragment
                }
            }
            return null
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return getString(R.string.create_vote_tab_options)
                1 -> return getString(R.string.create_vote_tab_settings)
            }
            return ""
        }
    }

    public override fun onResume() {
        super.onResume()
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_create_vote, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.menu_submit) {
            presenter.updateVoteTitle(edtTitle!!.text.toString())
            presenter.submitCreateVote()
            return true
        } else if (id == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(this, data)

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                cropImageUri = imageUri
                Log.d(TAG, "onActivityResult PICK_IMAGE_CHOOSER_REQUEST_CODE:" + cropImageUri!!)
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE)
            } else {
                // no permissions required or already grunted, can start crop image activity
                Log.d(TAG, "onActivityResult PICK_IMAGE_CHOOSER_REQUEST_CODE no permission:$imageUri")
                startCropImageActivity(imageUri)

            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                Log.d(TAG, "CROP_IMAGE_ACTIVITY_REQUEST_CODE ok:$resultUri")
                cropImageUri = resultUri
                vpSubArea.adapter = TabsAdapter(supportFragmentManager)
                val file = if (cropImageUri == null) null else FileUtils.getFile(this, cropImageUri)
                presenter.updateVoteImage(file!!)
                optionFragment!!.setVoteImage(resultUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // val error = result.error
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCropImageActivity(cropImageUri)
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (cropImageUri != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(cropImageUri)
            } else {
                Toast.makeText(this, R.string.create_vote_toast_image_permission, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCropImageActivity(imageUri: Uri?) {
        CropImage.activity(imageUri!!)
                .setActivityTitle(getString(R.string.create_vote_error_crop_image_title))
                .setMaxCropResultSize(Util.convertDpToPixel((320 * 2).toFloat(), this).toInt(), Util.convertDpToPixel((150 * 2).toFloat(), this).toInt())
                .setMinCropResultSize(Util.convertDpToPixel(320f, this).toInt(), Util.convertDpToPixel(150f, this).toInt())
                .start(this)
    }

    override fun onBackPressed() {
        if (edtTitle!!.text.toString().isNotEmpty()) {
            showExitCheckDialog()
        } else {
            super.onBackPressed()
        }
    }

    override fun showExitCheckDialog() {
        val exitDialog = AlertDialog.Builder(this@CreateVoteActivity)
        exitDialog.setTitle(R.string.create_vote_dialog_exit_title)
        exitDialog.setMessage(R.string.create_vote_dialog_exit_message)
        exitDialog.setNegativeButton(R.string.create_vote_dialog_exit_button_leave) { _, _ -> super@CreateVoteActivity.onBackPressed() }
        exitDialog.setPositiveButton(R.string.create_vote_dialog_exit_button_keep) { dialog, _ -> dialog.cancel() }
        exitDialog.show()
    }

    override fun showLoadingCircle() {
        circleLoad!!.visibility = View.VISIBLE
        circleLoad!!.setText(getString(R.string.vote_detail_circle_updating))
        circleLoad!!.spin()
    }

    override fun hideLoadingCircle() {
        circleLoad!!.stopSpinning()
        circleLoad!!.visibility = View.GONE
    }

    override fun showCreateVoteError(errorMap: Map<String, Boolean>) {
        val sb = StringBuilder()
        Log.e(TAG, "ERROR CHECK MAP : $errorMap")
        var errorNumber = 0
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_ENDTIME_MORE_THAN_NOW)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_endtime_more_than_now) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MAX_SMALL_THAN_TOTAL)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_max_smaller_than_total) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MAX_SAMLL_THAN_MIN)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_max_smaller_than_min) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MIN_0)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_min_option_0) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MAX_0)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_max_option_0) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_USER_CODE_ERROR)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_error_user_code) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_TITLE_EMPTY)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_title_empty) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_DUPLICATE)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_title_duplicate) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_ENDTIME_MORE_THAN_MAX)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_endtime_more_than_max) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_FILL_ALL_OPTION)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_fill_all) + "\n")
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_PASSWORD_EMPTY)) {
            errorNumber++
            sb.append(errorNumber.toString() + ". " + getString(R.string.create_vote_error_hint_password_empty) + "\n")
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
