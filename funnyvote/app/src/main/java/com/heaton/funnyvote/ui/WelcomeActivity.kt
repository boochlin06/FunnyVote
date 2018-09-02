package com.heaton.funnyvote.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.heaton.funnyvote.FirstTimePref
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.MainActivity
import com.heaton.funnyvote.R
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.PromotionDao
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.ui.introduction.IntroductionActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 * Created by heaton on 2016/10/26.
 */

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        val userDataRepository = Injection.provideUserRepository(applicationContext)

        doAsync {
            try {
                Thread.currentThread()
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val firstTimePref = Injection.provideFirstTimePref(this
                    .weakRef.get()!!.applicationContext)

            if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_MOCK_DATA, true)) {
                val promotionDao = (this.weakRef.get()!!.application as FunnyVoteApplication)
                        .daoSession.promotionDao
                val imageURL = weakRef.get()!!.resources.getStringArray(R.array.imageURL)
                val promotions = ArrayList<Promotion>()
                for (i in 0..0) {
                    val promotion = Promotion()
                    promotion.imageURL = imageURL[i % imageURL.size]
                    promotion.actionURL = "https://play.google.com/store/apps/details?id=com.heaton.funnyvote"
                    promotion.title = "title:$i"
                    promotions.add(promotion)
                }
                promotionDao.deleteAll()
                promotionDao.insertInTx(promotions)
                firstTimePref.edit().putBoolean(FirstTimePref.SP_FIRST_MOCK_DATA, false).apply()

            }

            uiThread {
                if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, true)) {
                    it.startActivity(
                            Intent(it, IntroductionActivity::class.java))
                } else {
                    it.startActivity(
                            Intent(it, MainActivity::class.java))
                }
                userDataRepository.getUser(object : UserDataSource.GetUserCallback {
                    override fun onResponse(user: User) {
                        finish()
                    }

                    override fun onFailure() {
                        finish()
                    }
                }, true)
            }
        }

    }
}
