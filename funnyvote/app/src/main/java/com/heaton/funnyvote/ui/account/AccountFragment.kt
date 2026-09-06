package com.heaton.funnyvote.ui.account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.databinding.FragmentAccountBinding

class AccountFragment : Fragment(), View.OnClickListener, AccountContract.View {

    companion object {
        private val TAG = AccountFragment::class.java.simpleName
        private const val RC_GOOGLE_SIGN_IN = 101
        private const val LOGIN_FB = 111
        private const val LOGIN_GOOGLE = 112
        private const val LOGIN_TWITTER = 113
    }

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private var tracker: Tracker? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var presenter: AccountContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val application = requireActivity().application as FunnyVoteApplication
        tracker = application.defaultTracker

        binding.fbLoginButton.setOnClickListener(this)
        binding.googleSignInButton.setOnClickListener(this)
        binding.twitterLogInButton.setOnClickListener(this)
        binding.signOutButton.setOnClickListener(this)
        binding.profileName.setOnClickListener(this)
        binding.editName.setOnClickListener(this)

        presenter = AccountPresenter(Injection.provideUserRepository(requireContext()), this)
        presenter?.start()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignIn(task)
        }
    }

    private fun handleGoogleSignIn(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                val name = account.displayName ?: "Google User"
                val googleID = account.id ?: ""
                val email = account.email ?: ""
                val picLink: Uri? = account.photoUrl
                val user = User().apply {
                    userName = name
                    userID = googleID
                    this.email = email
                    if (picLink != null) {
                        userIcon = picLink.toString()
                    }
                    type = User.TYPE_GOOGLE
                }
                presenter?.registerUser(user, getString(R.string.google_app_id))
            }
        } catch (e: ApiException) {
            Log.e(TAG, "signInResult:failed code=" + e.statusCode)
            Toast.makeText(context, R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show()
        }
    }

    override fun googleSignIn() {
        val signInIntent = googleSignInClient?.signInIntent
        if (signInIntent != null) {
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }
        tracker?.send(
            HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGIN)
                .setLabel("google").build()
        )
    }

    override fun googleSignOut() {
        googleSignInClient?.signOut()?.addOnCompleteListener(requireActivity()) {
            FirebaseAuth.getInstance().signOut()
            presenter?.unregisterUser()
            presenter?.updateUser()
        }
        tracker?.send(
            HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGOUT)
                .setLabel("google").build()
        )
    }

    override fun facebookLogin() {
        Toast.makeText(context, "Facebook 登入已停止服務，請使用 Google 登入", Toast.LENGTH_SHORT).show()
    }

    override fun facebookLogout() {
        presenter?.unregisterUser()
        presenter?.updateUser()
    }

    override fun twitterLogin() {
        Toast.makeText(context, "Twitter 登入已停止服務，請使用 Google 登入", Toast.LENGTH_SHORT).show()
    }

    override fun twitterlogout() {
        presenter?.unregisterUser()
        presenter?.updateUser()
    }

    override fun showUser(user: User) {
        _binding?.let { b ->
            b.profileName.text = user.userName
            loadProfileImage(user.userIcon)
            b.loadingProgressBar.visibility = View.GONE
            b.fbLoginButton.visibility = View.GONE
            b.googleSignInButton.visibility = View.GONE
            b.twitterLogInButton.visibility = View.GONE
            b.profileName.visibility = View.VISIBLE
            b.profilePicture.visibility = View.VISIBLE
            b.signOutButton.visibility = View.VISIBLE
        }
    }

    override fun showLoginView(guestName: String) {
        _binding?.let { b ->
            b.profileName.text = guestName
            b.profilePicture.setImageResource(R.drawable.ic_action_account_circle)
            b.loadingProgressBar.visibility = View.GONE
            b.signOutButton.visibility = View.GONE
            b.fbLoginButton.visibility = View.VISIBLE
            b.googleSignInButton.visibility = View.VISIBLE
            b.twitterLogInButton.visibility = View.VISIBLE
            b.profileName.visibility = View.VISIBLE
            b.profilePicture.visibility = View.VISIBLE
        }
    }

    override fun showNameEditDialog() {
        val b = _binding ?: return
        val builder = AlertDialog.Builder(requireContext())
        val input = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_account_name_edit, null)
        val editText = input.findViewById<EditText>(R.id.edtName)
        editText.setText(b.profileName.text.toString())
        builder.setView(input)
        builder.setTitle(getString(R.string.account_dialog_new_name_title))
        builder.setPositiveButton(R.string.account_dialog_ok) { _, _ ->
            val newName = editText.text.toString()
            tracker?.send(
                HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                    .setAction(AnalyzticsTag.ACTION_ACCOUNT_RENAME)
                    .setLabel(newName).build()
            )
            presenter?.changeCurrentUserName(newName)
        }
        builder.setNegativeButton(R.string.account_dialog_cancel, null)
        builder.show()
    }

    override fun showMergeOptionDialog(loginType: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.merge_option_msg)
        builder.setTitle(R.string.merge_option_title)
        builder.setPositiveButton(R.string.account_dialog_ok) { _, _ -> presenter?.login(loginType, true) }
        builder.setNegativeButton(R.string.account_dialog_no) { _, _ -> presenter?.login(loginType, false) }
        builder.show()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fb_login_button -> showMergeOptionDialog(LOGIN_FB)
            R.id.google_sign_in_button -> showMergeOptionDialog(LOGIN_GOOGLE)
            R.id.twitter_log_in_button -> showMergeOptionDialog(LOGIN_TWITTER)
            R.id.sign_out_button -> presenter?.logout()
            R.id.edit_name, R.id.profile_name -> showNameEditDialog()
        }
    }

    private fun loadProfileImage(link: String?) {
        _binding?.let { b ->
            if (!link.isNullOrEmpty()) {
                Glide.with(this).load(link).into(b.profilePicture)
            } else {
                b.profilePicture.setImageResource(R.drawable.ic_action_account_circle)
            }
        }
    }

    override fun setPresenter(presenter: AccountContract.Presenter) {
        this.presenter = presenter
    }
}
