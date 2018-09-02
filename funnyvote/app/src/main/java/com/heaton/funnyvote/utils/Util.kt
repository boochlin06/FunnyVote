package com.heaton.funnyvote.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toolbar
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.retrofit.Server
import com.heaton.funnyvote.ui.ShareDialogActivity
import com.heaton.funnyvote.ui.personal.PersonalActivity
import com.heaton.funnyvote.ui.votedetail.VoteDetailContentActivity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by heaton on 2016/10/12.
 */

object Util {

    var BUNDLE_KEY_VOTE_CODE = "VOTE_ID"
    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @param dateFormat   Date format
     * @return String representing date in specified format
     */
    fun getDate(milliSeconds: Long, dateFormat: String): String {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    /**
     * Covert dp to px
     *
     * @param dp
     * @param context
     * @return pixel
     */
    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * getDensity(context)
    }

    /**
     * Covert px to dp
     *
     * @param px
     * @param context
     * @return dp
     */
    fun convertPixelToDp(px: Float, context: Context): Float {
        return px / getDensity(context)
    }

    /**
     * 120dpi = 0.75
     * 160dpi = 1 (default)
     * 240dpi = 1.5
     *
     * @param context
     * @return
     */
    fun getDensity(context: Context): Float {
        val metrics = context.resources.displayMetrics
        return metrics.density
    }

    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun textAsBitmap(context: Context, text: String, textSize: Float, textColor: Int): Drawable {
        val paint = Paint(ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = textColor
        paint.textAlign = Paint.Align.LEFT
        val baseline = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.0f).toInt() // round
        val height = (baseline + paint.descent() + 0.0f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(image)
        canvas.drawText(text, 0f, baseline, paint)
        return BitmapDrawable(context.resources, image)
    }

    fun randomUserName(context: Context): String {
        val area = context.resources.getStringArray(R.array.area)
        val name = context.resources.getStringArray(R.array.name)
        val randomArea = area[(Math.random() * area.size).toInt()]
        val randomName = name[(Math.random() * name.size).toInt()]
        return randomArea + randomName + Integer.toString((Math.random() * 1000).toInt())
    }

    fun startActivityToVoteDetail(context: Context, voteCode: String) {
        val intent = Intent(context, VoteDetailContentActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        val bundle = Bundle()
        bundle.putString(BUNDLE_KEY_VOTE_CODE, voteCode)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }


    fun sendShareIntent(context: Context, data: VoteData) {
        val shareDialog = Intent(context, ShareDialogActivity::class.java)
        shareDialog.putExtra(ShareDialogActivity.EXTRA_TITLE, data.title)
        shareDialog.putExtra(ShareDialogActivity.EXTRA_IMG_URL, data.voteImage)
        shareDialog.putExtra(ShareDialogActivity.EXTRA_VOTE_URL, Server.WEB_URL + data.voteCode)
        shareDialog.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareDialog)
    }

    fun sendPersonalDetailIntent(context: Context, data: VoteData) {
        val personalActivity = Intent(context, PersonalActivity::class.java)
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_CODE, data.authorCode)
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_CODE_TYPE, data.authorCodeType)
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_NAME, data.authorName)
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_ICON, data.authorIcon)
        personalActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(personalActivity)
    }

    fun sendShareAppIntent(context: Context) {
        val appPackageName = context.applicationContext.packageName
        val appURL = "https://play.google.com/store/apps/details?id=$appPackageName"
        val shareDialog = Intent(context, ShareDialogActivity::class.java)
        shareDialog.putExtra(ShareDialogActivity.EXTRA_VOTE_URL, appURL)
        shareDialog.putExtra(ShareDialogActivity.EXTRA_IS_SHARE_APP, true)
        shareDialog.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareDialog)
    }

    fun AppCompatActivity.setupActionBar(toolBar: android.support.v7.widget.Toolbar, action: ActionBar.() -> Unit) {
        setSupportActionBar(toolBar)
        supportActionBar?.run {
            action()
        }
    }
}
