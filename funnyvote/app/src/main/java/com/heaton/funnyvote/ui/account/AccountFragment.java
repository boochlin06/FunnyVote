package com.heaton.funnyvote.ui.account;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.Injection;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment
        implements View.OnClickListener, AccountContract.View {
    private static final String TAG = AccountFragment.class.getSimpleName();
    private static final int RC_GOOGLE_SIGN_IN = 101;
    private static final int LOGIN_FB = 111;
    private static final int LOGIN_GOOGLE = 112;
    private static final int LOGIN_TWITTER = 113;

    private FragmentAccountBinding binding;
    private Tracker tracker;
    private GoogleSignInClient googleSignInClient;
    private AccountContract.Presenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        FunnyVoteApplication application = (FunnyVoteApplication) requireActivity().getApplication();
        tracker = application.getDefaultTracker();

        binding.fbLoginButton.setOnClickListener(this);
        binding.googleSignInButton.setOnClickListener(this);
        binding.twitterLogInButton.setOnClickListener(this);
        binding.signOutButton.setOnClickListener(this);
        binding.profileName.setOnClickListener(this);
        binding.editName.setOnClickListener(this);

        presenter = new AccountPresenter(Injection.provideUserRepository(requireContext()), this, Injection.provideSchedulerProvider());
        presenter.subscribe();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignIn(task);
        }
    }

    private void handleGoogleSignIn(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String name = account.getDisplayName();
                String googleID = account.getId();
                String email = account.getEmail();
                Uri picLink = account.getPhotoUrl();
                User user = new User();
                user.setUserName(name);
                user.setUserID(googleID);
                user.setEmail(email);
                if (picLink != null) {
                    user.setUserIcon(picLink.toString());
                }
                user.setType(User.TYPE_GOOGLE);
                presenter.registerUser(user, getString(R.string.google_app_id));
            }
        } catch (ApiException e) {
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(getContext(), R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        if (tracker != null) {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                    .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGIN)
                    .setLabel("google").build());
        }
    }

    @Override
    public void googleSignOut() {
        googleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            FirebaseAuth.getInstance().signOut();
            presenter.unregisterUser();
            presenter.updateUser();
        });
        if (tracker != null) {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                    .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGOUT)
                    .setLabel("google").build());
        }
    }

    @Override
    public void facebookLogin() {
        Toast.makeText(getContext(), "Facebook 登入已停止服務，請使用 Google 登入", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void facebookLogout() {
        presenter.unregisterUser();
        presenter.updateUser();
    }

    @Override
    public void twitterLogin() {
        Toast.makeText(getContext(), "Twitter 登入已停止服務，請使用 Google 登入", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void twitterlogout() {
        presenter.unregisterUser();
        presenter.updateUser();
    }

    @Override
    public void showUser(User user) {
        if (binding == null) return;
        binding.profileName.setText(user.getUserName());
        loadProfileImage(user.getUserIcon());
        binding.loadingProgressBar.setVisibility(View.GONE);
        binding.fbLoginButton.setVisibility(View.GONE);
        binding.googleSignInButton.setVisibility(View.GONE);
        binding.twitterLogInButton.setVisibility(View.GONE);
        binding.profileName.setVisibility(View.VISIBLE);
        binding.profilePicture.setVisibility(View.VISIBLE);
        binding.signOutButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoginView(String guestName) {
        if (binding == null) return;
        binding.profileName.setText(guestName);
        binding.profilePicture.setImageResource(R.drawable.ic_action_account_circle);
        binding.loadingProgressBar.setVisibility(View.GONE);
        binding.signOutButton.setVisibility(View.GONE);
        binding.fbLoginButton.setVisibility(View.VISIBLE);
        binding.googleSignInButton.setVisibility(View.VISIBLE);
        binding.twitterLogInButton.setVisibility(View.VISIBLE);
        binding.profileName.setVisibility(View.VISIBLE);
        binding.profilePicture.setVisibility(View.VISIBLE);
    }

    public void showNameEditDialog() {
        if (binding == null) return;
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View input = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_account_name_edit, null);
        final EditText editText = input.findViewById(R.id.edtName);
        editText.setText(binding.profileName.getText().toString());
        builder.setView(input);
        builder.setTitle(getString(R.string.account_dialog_new_name_title));
        builder.setPositiveButton(R.string.account_dialog_ok, (dialogInterface, i) -> {
            if (tracker != null) {
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                        .setAction(AnalyzticsTag.ACTION_ACCOUNT_RENAME)
                        .setLabel(editText.getText().toString()).build());
            }
            presenter.changeCurrentUserName(editText.getText().toString());
        });
        builder.setNegativeButton(R.string.account_dialog_cancel, null);
        builder.show();
    }

    public void showMergeOptionDialog(final int loginType) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(R.string.merge_option_msg);
        builder.setTitle(R.string.merge_option_title);
        builder.setPositiveButton(R.string.account_dialog_ok, (dialogInterface, i) -> presenter.login(loginType, true));
        builder.setNegativeButton(R.string.account_dialog_no, (dialogInterface, i) -> presenter.login(loginType, false));
        builder.show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fb_login_button) {
            showMergeOptionDialog(LOGIN_FB);
        } else if (id == R.id.google_sign_in_button) {
            showMergeOptionDialog(LOGIN_GOOGLE);
        } else if (id == R.id.twitter_log_in_button) {
            showMergeOptionDialog(LOGIN_TWITTER);
        } else if (id == R.id.sign_out_button) {
            presenter.logout();
        } else if (id == R.id.edit_name || id == R.id.profile_name) {
            showNameEditDialog();
        }
    }

    private void loadProfileImage(String link) {
        if (binding == null) return;
        Glide.with(this).load(link).into(binding.profilePicture);
    }

    @Override
    public void setPresenter(AccountContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
