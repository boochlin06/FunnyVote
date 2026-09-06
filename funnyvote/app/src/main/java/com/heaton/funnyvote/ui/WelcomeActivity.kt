package com.heaton.funnyvote.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.heaton.funnyvote.FirstTimePref
import com.heaton.funnyvote.MainActivity
import com.heaton.funnyvote.R
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.data.local.AppDatabase
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.databinding.ActivityWelcomeBinding
import com.heaton.funnyvote.ui.introduction.IntroductionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userDataRepository = Injection.provideUserRepository(applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            val firstTimePref = Injection.provideFirstTimePref(applicationContext)

            if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_MOCK_DATA, true)) {
                val promotionDao = AppDatabase.getInstance(applicationContext).promotionDao()
                val imageURL = resources.getStringArray(R.array.imageURL)
                val promotions = ArrayList<Promotion>()
                for (i in 0..0) {
                    val promotion = Promotion().apply {
                        this.imageURL = imageURL[i % imageURL.size]
                        actionURL = "https://play.google.com/store/apps/details?id=com.heaton.funnyvote"
                        title = "title:$i"
                    }
                    promotions.add(promotion)
                }
                promotionDao.deleteAll()
                promotionDao.insertAll(promotions)
                firstTimePref.edit().putBoolean(FirstTimePref.SP_FIRST_MOCK_DATA, false).apply()
            }

            withContext(Dispatchers.Main) {
                if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, true)) {
                    startActivity(Intent(this@WelcomeActivity, IntroductionActivity::class.java))
                } else {
                    startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
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
