package com.heaton.funnyvote.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.databinding.DialogShareBinding

class ShareDialogActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: DialogShareBinding
    private lateinit var voteURL: String
    private var title: String = ""
    private var isShareApp: Boolean = false
    private var tracker: Tracker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker

        if (intent != null) {
            voteURL = intent.getStringExtra(EXTRA_VOTE_URL) ?: ""
            isShareApp = intent.getBooleanExtra(EXTRA_IS_SHARE_APP, false)
            if (!isShareApp) {
                title = intent.getStringExtra(EXTRA_TITLE) ?: ""
                if (title.length > 80) {
                    title = title.substring(0, 80) + " ..."
                }
            }
            initShareOptions()
        } else {
            finish()
        }

        if (isShareApp) {
            binding.shareTo.setText(R.string.vote_share_app_via)
        } else {
            binding.shareTo.setText(R.string.vote_share_vote_via)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isShareApp) {
            tracker?.setScreenName(AnalyzticsTag.SCREEN_ABOUT_SHARE_APP)
        } else {
            tracker?.setScreenName(AnalyzticsTag.SCREEN_SHARE_VOTE)
        }
        tracker?.send(HitBuilders.ScreenViewBuilder().build())
    }

    private fun initShareOptions() {
        val pm = packageManager
        for (i in APPS.indices) {
            val componentName = ComponentName(APPS[i][0], APPS[i][1])
            try {
                val info = pm.getActivityInfo(componentName, PackageManager.GET_META_DATA)
                val view = layoutInflater.inflate(R.layout.btn_share, null)
                val imageView = view.findViewById<ImageView>(R.id.app_share_icon)
                imageView.setImageDrawable(info.loadIcon(pm))
                val labelTextView = view.findViewById<TextView>(R.id.app_label)
                labelTextView.text = info.loadLabel(pm)
                view.tag = componentName
                view.setOnClickListener(this)
                binding.shareOptions.addView(view)
            } catch (e: PackageManager.NameNotFoundException) {
                // app not installed
            }
        }

        // Copy link
        val copy = layoutInflater.inflate(R.layout.btn_share, null)
        val copyImage = copy.findViewById<ImageView>(R.id.app_share_icon)
        copyImage.setImageResource(R.drawable.ic_shortcut_content_copy)
        val copyLabel = copy.findViewById<TextView>(R.id.app_label)
        copyLabel.setText(R.string.vote_share_copy_url)
        copy.setOnClickListener { onCopyLinkClicked() }
        binding.shareOptions.addView(copy)

        // More
        val more = layoutInflater.inflate(R.layout.btn_share, null)
        val moreImg = more.findViewById<ImageView>(R.id.app_share_icon)
        moreImg.setImageResource(R.drawable.ic_navigation_more_horiz)
        val moreLabel = more.findViewById<TextView>(R.id.app_label)
        moreLabel.setText(R.string.vote_share_more)
        more.setOnClickListener { onOtherShareClicked() }
        binding.shareOptions.addView(more)
    }

    private fun onCopyLinkClicked() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Link Copied", voteURL)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(applicationContext, R.string.vote_share_copied_msg, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onOtherShareClicked() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
        }
        if (isShareApp) {
            sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_funny_vote_app), voteURL))
            startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.vote_share_app_via)))
        } else {
            sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.vote_share_msg), title, voteURL))
            startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.vote_share_vote_via)))
        }
        finish()
    }

    override fun onClick(view: View) {
        val tag = view.tag
        if (tag != null && tag is ComponentName) {
            val send = Intent().apply {
                component = tag
                action = Intent.ACTION_SEND
                type = "text/plain"
            }
            if (isShareApp) {
                send.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_funny_vote_app), voteURL))
            } else {
                send.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.vote_share_msg), title, voteURL))
            }
            tracker?.send(
                HitBuilders.EventBuilder()
                    .setCategory(send.component!!.packageName)
                    .setAction(if (isShareApp) AnalyzticsTag.ACTION_SHARE_APP else AnalyzticsTag.ACTION_SHARE_VOTE)
                    .setLabel(voteURL)
                    .build()
            )
            startActivity(send)
            finish()
        }
    }

    companion object {
        private val APPS = arrayOf(
            arrayOf("com.facebook.katana", "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias"),
            arrayOf("jp.naver.line.android", "jp.naver.line.android.activity.selectchat.SelectChatActivity"),
            arrayOf("com.twitter.android", "com.twitter.android.composer.ComposerActivity"),
            arrayOf("com.google.android.apps.plus", "com.google.android.libraries.social.gateway.GatewayActivity")
        )
        const val EXTRA_VOTE_URL = "vote_url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_IMG_URL = "image_url"
        const val EXTRA_IS_SHARE_APP = "is_share_app"
    }
}
