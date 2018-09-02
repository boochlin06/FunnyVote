package com.heaton.funnyvote

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.notification.VoteNotificationManager
import com.heaton.funnyvote.ui.about.AboutFragment
import com.heaton.funnyvote.ui.account.AccountFragment
import com.heaton.funnyvote.ui.createvote.CreateVoteActivity
import com.heaton.funnyvote.ui.main.MainPageFragment
import com.heaton.funnyvote.ui.main.MainPageTabFragment
import com.heaton.funnyvote.ui.personal.UserActivity
import com.heaton.funnyvote.ui.search.SearchFragment
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity(), MainPageContract.View {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar
    private lateinit var navigationView: NavigationView
    private var doubleBackToExitPressedOnce = false
    private lateinit var searchView: SearchView
    private var adView: AdView? = null
    private var tracker: Tracker? = null
    private lateinit var presenter: MainPageContract.Presenter

    private val queryListener = object : SearchView.OnQueryTextListener {

        override fun onQueryTextChange(newText: String): Boolean {
            return false
        }

        override fun onQueryTextSubmit(query: String): Boolean {
            Log.d(TAG, "onQueryTextSubmit:" + query + "  page:" + currentPage
                    + " search page:" + navigationView.menu.findItem(R.id.navigation_item_search).itemId)

            if (currentPage != navigationView.menu.findItem(R.id.navigation_item_search).itemId) {
                switchFragment(navigationView.menu.findItem(R.id.navigation_item_search), query)
                navigationView.menu.findItem(R.id.navigation_item_search).isChecked = true
            }
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker
        toolbar = findViewById<View>(R.id.toolbarMain) as Toolbar
        adView = findViewById<View>(R.id.adView) as AdView
        toolbar.title = getString(R.string.drawer_home)
        toolbar.setTitleTextColor(Color.WHITE)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,
                R.string.drawer_close)
        drawerToggle.syncState()

        ENABLE_ADMOB = resources.getBoolean(R.bool.enable_main_admob)

        navigationView = findViewById<View>(R.id.navigation_view) as NavigationView
        navigationView.menu.getItem(0).isChecked = true
        navigationView.getHeaderView(0).setOnClickListener {
            if (currentPage != navigationView.menu.findItem(R.id.navigation_item_account).itemId) {
                switchFragment(navigationView.menu.findItem(R.id.navigation_item_account))
            }
            drawerLayout.closeDrawers()
        }
        currentPage = R.id.navigation_item_main

        setupDrawerContent(navigationView)
        setUpAdmob()

        VoteNotificationManager.getInstance(applicationContext).startNotificationAlarm()
        presenter = MainPagePresenter(Injection.provideUserRepository(applicationContext), this)
        presenter.start()
    }

    private fun setUpAdmob() {
        if (ENABLE_ADMOB) {
            val adRequest = AdRequest.Builder()
                    .build()
            adView!!.loadAd(adRequest)
        } else {
            adView!!.visibility = View.GONE
        }
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        drawerLayout.addDrawerListener(drawerToggle)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {

            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })
        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (currentPage != menuItem.itemId) {
                switchFragment(menuItem)
            }
            drawerLayout.closeDrawers()
            true
        }

    }

    override fun onResume() {
        super.onResume()
        navigationView.setCheckedItem(currentPage)
    }

    private fun switchFragment(menuItem: MenuItem, searchKeyword: String = "") {
        val menuId = menuItem.itemId
        drawerLayout.postDelayed({
            if (Build.VERSION.SDK_INT > 21) {
                toolbar.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.color_primary))
            } else {
                toolbar.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.color_primary))
            }
            when (menuId) {
                R.id.navigation_item_main -> {
                    presenter.IntentToMainPage()
                    currentPage = menuItem.itemId
                }
                R.id.navigation_item_create_vote -> presenter.IntentToCreatePage()
                R.id.navigation_item_list_my_box -> presenter.IntentToUserPage()
                R.id.navigation_item_search -> {
                    presenter.IntentToSearchPage(searchKeyword)
                    currentPage = menuItem.itemId
                }
                R.id.navigation_item_account -> {
                    presenter.IntentToAccountPage()
                    currentPage = menuItem.itemId
                }
                R.id.navigation_item_about -> {
                    presenter.IntentToAboutPage()
                    currentPage = menuItem.itemId
                }
            }
            navigationView.setCheckedItem(currentPage)
            tracker!!.send(HitBuilders.ScreenViewBuilder().build())
        }, 500)
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        if (currentPage != navigationView.menu.getItem(0).itemId) {
            currentPage = navigationView.menu.getItem(0).itemId
            navigationView.menu.getItem(0).isChecked = true
            switchFragment(navigationView.menu.getItem(0))
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, R.string.wall_item_toast_double_click_to_exit, Toast.LENGTH_SHORT).show()

            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        searchView = menu.findItem(R.id.menu_search).actionView as SearchView
        searchView.queryHint = getString(R.string.vote_detail_menu_search_hint)
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(queryListener)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.menu_add) {
            presenter.IntentToCreatePage()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_content)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun showSearchPage(searchKeyword: String) {
        val fragment: Fragment
        val ft = supportFragmentManager.beginTransaction()
        val slide = Slide()
        slide.duration = 400
        slide.slideEdge = Gravity.RIGHT
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_SEARCH)
        fragment = SearchFragment()
        val searchArgument = Bundle()
        searchArgument.putString(SearchFragment.KEY_SEARCH_KEYWORD, searchKeyword)
        fragment.run {
            arguments = searchArgument
            enterTransition = slide
        }
        ft.replace(R.id.frame_content, fragment).commit()
        toolbar.setTitle(R.string.drawer_search)
    }

    override fun showCreatePage() {
        startActivity(Intent(this@MainActivity, CreateVoteActivity::class.java))
    }

    override fun showUserPage() {
        startActivity(Intent(this@MainActivity, UserActivity::class.java))
    }

    override fun showMainPage() {
        val fragment: Fragment
        val ft = supportFragmentManager.beginTransaction()
        val slide = Slide()
        slide.duration = 400
        slide.slideEdge = Gravity.RIGHT
        fragment = MainPageFragment()
        fragment.enterTransition =slide
        ft.replace(R.id.frame_content, fragment).commit()
        toolbar.title = getString(R.string.drawer_home)
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_MAIN)
    }

    override fun showAboutPage() {
        val ft = supportFragmentManager.beginTransaction()
        val slide = Slide()
        slide.duration = 400
        slide.slideEdge = Gravity.RIGHT
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_ABOUT)
        val aboutFragment = AboutFragment()
        aboutFragment.enterTransition = slide
        ft.replace(R.id.frame_content, aboutFragment).commit()
        toolbar.setTitle(R.string.drawer_about)
    }

    override fun showAccountPage() {
        val ft = supportFragmentManager.beginTransaction()
        val slide = Slide()
        slide.duration = 400
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_ACCOUNT)
        val accountFragment = AccountFragment()
        accountFragment.enterTransition = slide
        ft.replace(R.id.frame_content, accountFragment).commit()
        val bgColor = ContextCompat.getColor(applicationContext, R.color.md_light_blue_100)
        toolbar.setBackgroundColor(bgColor)
        toolbar.setTitle(R.string.drawer_account)
    }

    override fun updateUserView(user: User) {
        val header = navigationView.getHeaderView(0)
        val icon = header.findViewById<View>(R.id.imgUserIcon) as CircleImageView
        val name = header.findViewById<View>(R.id.txtUserName) as TextView
        name.text = user.userName
        Glide.with(this@MainActivity).load(user.userIcon).dontAnimate()
                .override(resources.getDimension(R.dimen.drawer_image_width).toInt(), resources.getDimension(R.dimen.drawer_image_high).toInt())
                .placeholder(R.drawable.ic_action_account_circle).into(icon)
    }

    override fun setPresenter(presenter: MainPageContract.Presenter) {
        this.presenter = presenter
    }

    companion object {

        var TAG = MainPageTabFragment::class.java.simpleName!!
        private val ANIM_DURATION_TOOLBAR = 300

        private var currentPage: Int = 0
        var ENABLE_ADMOB = true
    }
}
