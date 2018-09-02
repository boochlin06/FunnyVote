package com.heaton.funnyvote.ui.account

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.twitter.CustomTwitterApiClient
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import org.json.JSONException
import retrofit2.Call
import retrofit2.Response
import java.util.*


/**
 * Created by chiu_mac on 2016/10/28.
 */

class AccountFragment : android.support.v4.app.Fragment()
        , GoogleApiClient.OnConnectionFailedListener
        , View.OnClickListener, AccountContract.View {

    //Facebook API
    private var callbackManager: CallbackManager? = null
    private var accessTokenTracker: AccessTokenTracker? = null
    private val facebookProfileCallback = GraphRequest.Callback { response ->
        try {
            val profile = response.jsonObject
            val name = profile.getString("name")
            val facebookID = profile.getString("id")
            val email = if (profile.has("email")) profile.getString("email") else ""
            val gender = if (profile.has("gender")) profile.getString("gender") else ""
            var min = -1
            var max = -1

            if (profile.has("age_range")) {
                val ageRange = profile.getJSONObject("age_range")
                min = if (ageRange.has("min")) ageRange.getInt("min") else -1
                max = if (ageRange.has("max")) ageRange.getInt("max") else -1
            }

            var link = ""
            if (profile.has("picture")) {
                val picture = profile.getJSONObject("picture")
                link = picture.getJSONObject("data").getString("url")
            }
            val user = User()
            user.userName = name
            user.userID = facebookID
            user.userIcon = link
            user.email = email
            user.gender = gender
            user.minAge = min
            user.maxAge = max
            user.type = User.TYPE_FACEBOOK
            presenter.registerUser(user, getString(R.string.facebook_app_id))
            //userManager.registerUser(user, mergeGuest, registerUserCallback);
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    private var tracker: Tracker? = null
    //Google Sign in API
    private var googleApiClient: GoogleApiClient? = null

    //UserManager
    private lateinit var presenter: AccountContract.Presenter

    //Views
    private lateinit var picImageView: ImageView
    private lateinit var editNameImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var googleSignInBtn: Button
    private lateinit var signoutBtn: Button
    private lateinit var facebookLoginBtn: Button
    private lateinit var twitterLoginBtn: Button
    private lateinit var loadingProgressBar: ProgressBar
    private var authClient: TwitterAuthClient? = null
    private val twitterLoginCallback = object : Callback<TwitterSession>() {
        override fun success(result: Result<TwitterSession>) {
            Log.d(TAG, "Twitter login success")
            val twitterSession = result.data
            val twitterApiClient = CustomTwitterApiClient(twitterSession)
            val userService = twitterApiClient.userService
            val twitterUserProfileCall = userService.show(twitterSession.userId)
            twitterUserProfileCall.enqueue(object : retrofit2.Callback<com.twitter.sdk.android.core.models.User> {
                override fun onResponse(call: Call<com.twitter.sdk.android.core.models.User>,
                                        response: Response<com.twitter.sdk.android.core.models.User>) {
                    Log.d(TAG, "Get twitter user profile:" + response.code())
                    if (response.isSuccessful) {
                        val user = response.body()
                        val newUser = User()
                        newUser.userName = user.name
                        newUser.userID = user.idStr
                        newUser.email = user.email
                        newUser.userIcon = user.profileImageUrl.replace("normal", "bigger")
                        newUser.type = User.TYPE_TWITTER
                        presenter.registerUser(newUser, getString(R.string.twitter_api_id))
                        //userManager.registerUser(newUser, mergeGuest, registerUserCallback);
                    } else {
                        Toast.makeText(context, R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<com.twitter.sdk.android.core.models.User>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(context, R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show()
                }
            })
        }

        override fun failure(exception: TwitterException) {
            exception.printStackTrace()
        }
    }

    private val twitterAuthClient: TwitterAuthClient
        get() {
            if (authClient == null) {
                synchronized(TwitterLoginButton::class.java) {
                    if (authClient == null) {
                        authClient = TwitterAuthClient()
                    }
                }
            }
            return authClient!!
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        googleApiClient = GoogleApiClient.Builder(requireContext())
                .enableAutoManage(requireActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val application = activity!!.application as FunnyVoteApplication
        tracker = application.defaultTracker
        val view = inflater.inflate(R.layout.fragment_account, null)

        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager!!, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "onSuccess")
                handleFacebookLogIn()
            }

            override fun onCancel() {
                Log.d(TAG, "onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "onError:" + error.message)
                Toast.makeText(context, R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show()
            }
        })

        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken, currentAccessToken: AccessToken?) {
                Log.d(TAG, "onCurrentAccessTokenChanged")
                if (currentAccessToken == null) {
                    //userManager.unregisterUser();
                    presenter.unregisterUser()
                    presenter.updateUser()
                    //updateUI();
                }
            }
        }

        //Views
        facebookLoginBtn = view.findViewById<View>(R.id.fb_login_button) as Button
        facebookLoginBtn.setOnClickListener(this)
        nameTextView = view.findViewById<View>(R.id.profile_name) as TextView
        nameTextView.setOnClickListener(this)
        picImageView = view.findViewById<View>(R.id.profile_picture) as ImageView
        googleSignInBtn = view.findViewById<View>(R.id.google_sign_in_button) as Button
        googleSignInBtn.setOnClickListener(this)
        signoutBtn = view.findViewById<View>(R.id.sign_out_button) as Button
        signoutBtn.setOnClickListener(this)
        loadingProgressBar = view.findViewById<View>(R.id.loading_progress_bar) as ProgressBar
        editNameImageView = view.findViewById<View>(R.id.edit_name) as ImageView
        editNameImageView.setOnClickListener(this)
        twitterLoginBtn = view.findViewById<View>(R.id.twitter_log_in_button) as Button
        twitterLoginBtn.setOnClickListener(this)


        presenter = AccountPresenter(Injection.provideUserRepository(context!!), this)
        presenter.start()

        return view
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        accessTokenTracker!!.stopTracking()
        googleApiClient!!.stopAutoManage(activity!!)
        googleApiClient!!.disconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult:$requestCode/$resultCode")
        callbackManager!!.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleGoogleSignIn(result)
        } else if (requestCode == twitterAuthClient.requestCode) {
            twitterAuthClient.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleFacebookLogIn() {
        val params = Bundle()
        params.putString("fields", "email,name,picture.type(large),gender,age_range")
        GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                params,
                HttpMethod.GET,
                facebookProfileCallback
        ).executeAsync()
    }

    private fun handleGoogleSignIn(result: GoogleSignInResult) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully
            val googleAccount = result.signInAccount
            val name = googleAccount!!.displayName
            val googleID = googleAccount.id
            val email = googleAccount.email
            val picLink = googleAccount.photoUrl
            val user = User()
            user.userName = name
            user.userID = googleID
            user.email = email
            if (picLink != null) {
                user.userIcon = picLink.toString()
            }
            user.type = User.TYPE_GOOGLE
            presenter.registerUser(user, getString(R.string.google_app_id))
            //userManager.registerUser(user, mergeGuest, registerUserCallback);
        } else {
            Toast.makeText(context, R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show()
        }
    }

    override fun googleSignIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        tracker!!.send(HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGIN)
                .setLabel("google").build())
    }

    override fun googleSignOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback { status ->
            Log.d(TAG, "status:" + status.isSuccess)
            if (status.isSuccess) {
                presenter.unregisterUser()
                presenter.updateUser()
                //userManager.unregisterUser();
                //updateUI();
            }
        }
        tracker!!.send(HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGOUT)
                .setLabel("google").build())
    }

    override fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this,
                Arrays.asList("public_profile", "email"))
        tracker!!.send(HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGIN)
                .setLabel("facebook").build())
    }

    override fun facebookLogout() {
        LoginManager.getInstance().logOut()
        tracker!!.send(HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGOUT)
                .setLabel("facebook").build())
    }

    override fun showUser(user: User) {
        nameTextView.text = user.userName
        loadProfileImage(user.userIcon)
        loadingProgressBar.visibility = View.GONE
        facebookLoginBtn.visibility = View.GONE
        googleSignInBtn.visibility = View.GONE
        twitterLoginBtn.visibility = View.GONE
        nameTextView.visibility = View.VISIBLE
        picImageView.visibility = View.VISIBLE
        signoutBtn.visibility = View.VISIBLE
    }

    override fun showLoginView(guestName: String) {
        nameTextView.text = guestName
        picImageView.setImageResource(R.drawable.ic_action_account_circle)
        loadingProgressBar.visibility = View.GONE
        signoutBtn.visibility = View.GONE
        facebookLoginBtn.visibility = View.VISIBLE
        googleSignInBtn.visibility = View.VISIBLE
        twitterLoginBtn.visibility = View.VISIBLE
        nameTextView.visibility = View.VISIBLE
        picImageView.visibility = View.VISIBLE
    }

    private fun showLoading() {
        loadingProgressBar.visibility = View.VISIBLE
        facebookLoginBtn.visibility = View.GONE
        googleSignInBtn.visibility = View.GONE
        nameTextView.visibility = View.GONE
        picImageView.visibility = View.GONE
        signoutBtn.visibility = View.GONE
    }

    override fun showNameEditDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val input = LayoutInflater.from(context).inflate(R.layout.dialog_account_name_edit, null)
        val editText = input.findViewById<View>(R.id.edtName) as EditText
        editText.setText(nameTextView.text.toString())
        builder.setView(input)
        builder.setTitle(getString(R.string.account_dialog_new_name_title))
        builder.setPositiveButton(R.string.account_dialog_ok) { _, _ ->
            tracker!!.send(HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                    .setAction(AnalyzticsTag.ACTION_ACCOUNT_RENAME)
                    .setLabel(editText.text.toString()).build())
            presenter.changeCurrentUserName(editText.text.toString())
            //userManager.changeCurrentUserName(editText.getText().toString(), changeUserNameCallback);
        }
        builder.setNegativeButton(R.string.account_dialog_cancel) { _, _ -> }
        builder.show()

    }

    override fun showMergeOptionDialog(loginType: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.merge_option_msg)
        builder.setTitle(R.string.merge_option_title)
        builder.setPositiveButton(R.string.account_dialog_ok) { _, _ -> presenter.login(loginType, true) }
        builder.setNegativeButton(R.string.account_dialog_no) { _, _ -> presenter.login(loginType, false) }
        builder.show()
    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "onConnectionFailed:$connectionResult")
        Toast.makeText(context, R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fb_login_button -> showMergeOptionDialog(LOGIN_FB)
            R.id.google_sign_in_button -> showMergeOptionDialog(LOGIN_GOOGLE)
            R.id.twitter_log_in_button -> {
                showMergeOptionDialog(LOGIN_TWITTER)
                presenter.logout()
            }
            R.id.sign_out_button -> presenter.logout()
            R.id.edit_name, R.id.profile_name -> showNameEditDialog()
        }
    }

    private fun loadProfileImage(link: String) {
        Glide.with(this).load(link).into(picImageView)
    }

    override fun twitterLogin() {
        twitterAuthClient.authorize(requireActivity(), twitterLoginCallback)
        tracker!!.send(HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGIN)
                .setLabel("twitter").build())
    }

    override fun twitterlogout() {
        Twitter.getSessionManager().clearActiveSession()
        presenter.unregisterUser()
        presenter.updateUser()
        //userManager.unregisterUser();
        //updateUI();
        tracker!!.send(HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGOUT)
                .setLabel("twitter").build())
    }

    override fun setPresenter(presenter: AccountContract.Presenter) {
        this.presenter = presenter
    }

    companion object {
        private val TAG = AccountFragment::class.java.simpleName
        private val RC_GOOGLE_SIGN_IN = 101
        private val LOGIN_FB = 111
        private val LOGIN_GOOGLE = 112
        private val LOGIN_TWITTER = 113
    }
}
