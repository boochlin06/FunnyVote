package com.heaton.funnyvote.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FirstTimePref;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.Injection;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.CirclePageIndicator;
import com.heaton.funnyvote.ui.createvote.CreateVoteActivity;
import com.heaton.funnyvote.utils.Util;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;

/**
 * Created by heaton on 16/4/1.
 */
public class MainPageFragment extends android.support.v4.app.Fragment
        implements MainPageContract.MainPageView {

    public static String TAG = MainPageFragment.class.getSimpleName();
    public static boolean ENABLE_PROMOTION_ADMOB = true;
    private AutoScrollViewPager vpHeader;
    private AppBarLayout appBarMain;
    private View promotionADMOB;
    private TabsAdapter tabsAdapter;
    private ViewPager vpMainPage;
    private Activity context;
    private CircleProgressView circleLoad;
    private Tracker tracker;
    private MainPageContract.Presenter pagePresenter;
    private MainPageTabFragment hotsFragment, newsFragment;
    private AlertDialog passwordDialog;

    @Override
    public void setPresenter(MainPageContract.Presenter presenter) {
        this.pagePresenter = pagePresenter;
    }

    private class PromotionType {
        public static final int PROM0TION_TYPE_ADMOB = 0;
        public static final int PROMOTION_TYPE_FUNNY_VOTE = 1;
        private int promotionType;
        private Promotion promotion;

        public PromotionType(int promotionType, Promotion promotion) {
            this.promotion = promotion;
            this.promotionType = promotionType;
        }

        public int getPromotionType() {
            return this.promotionType;
        }

        public Promotion getPromotion() {
            return this.promotion;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pagePresenter = new MainPagePresenter(Injection.provideVoteDataRepository(context)
                , Injection.provideUserRepository(context)
                , Injection.providePromotionRepository(context)
                , this
                , Injection.provideSchedulerProvider());

        pagePresenter.subscribe();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_page_top, null);

        FunnyVoteApplication application = (FunnyVoteApplication) getActivity().getApplication();
        tracker = application.getDefaultTracker();
        context = this.getActivity();

        circleLoad = (CircleProgressView) view.findViewById(R.id.circleLoad);
        circleLoad.setTextMode(TextMode.TEXT);
        circleLoad.setShowTextWhileSpinning(true);
        circleLoad.setFillCircleColor(getResources().getColor(R.color.md_amber_50));

        circleLoad.setText(getString(R.string.vote_detail_circle_loading));
        vpHeader = (AutoScrollViewPager) view.findViewById(R.id.vpHeader);
        vpHeader.setAdapter(new HeaderAdapter(new ArrayList<PromotionType>(), null));
        vpHeader.setCurrentItem(0);
        appBarMain = (AppBarLayout) view.findViewById(R.id.appBarMain);
        vpMainPage = (ViewPager) view.findViewById(R.id.vpMainPage);

        TabLayout tabMainPage = (TabLayout) view.findViewById(R.id.tabLayoutMainPage);
        tabMainPage.setupWithViewPager(vpMainPage);

        CirclePageIndicator titleIndicator = (CirclePageIndicator) view.findViewById(R.id.vpIndicator);
        titleIndicator.setViewPager(vpHeader);
        vpHeader.setInterval(100000);
        vpHeader.setScrollDurationFactor(5);

        appBarMain.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.EXPANDED) {
                    vpHeader.startAutoScroll();
                } else if (state == State.COLLAPSED) {
                    vpHeader.stopAutoScroll();
                }
            }
        });
        tracker.setScreenName(AnalyzticsTag.SCREEN_MAIN_HOT);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        vpMainPage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    tracker.setScreenName(AnalyzticsTag.SCREEN_MAIN_HOT);
                } else if (position == 1) {
                    tracker.setScreenName(AnalyzticsTag.SCREEN_MAIN_NEW);
                }
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ENABLE_PROMOTION_ADMOB = getResources().getBoolean(R.bool.enable_promotion_admob);

        return view;
    }

    @Override
    public void showShareDialog(VoteData data) {
        Util.sendShareIntent(getContext(), data);
    }

    @Override
    public void showAuthorDetail(VoteData data) {
        Util.sendPersonalDetailIntent(getContext(), data);
    }

    @Override
    public void showCreateVote() {
        getActivity().startActivity(new Intent(getContext(), CreateVoteActivity.class));
    }

    @Override
    public void showVoteDetail(VoteData data) {
        Util.startActivityToVoteDetail(getContext(), data.getVoteCode());
    }

    @Override
    public void showIntroductionDialog() {
        SharedPreferences firstTimePref = Injection.provideFirstTimePref(getActivity());
        if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_INTRODUTCION_QUICK_POLL, true)) {
            firstTimePref.edit().putBoolean(FirstTimePref.SP_FIRST_INTRODUTCION_QUICK_POLL, false).apply();
            final Dialog introductionDialog = new Dialog(getActivity());
            introductionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            introductionDialog.requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            introductionDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            introductionDialog.setCanceledOnTouchOutside(false);

            final VoteData data = new VoteData();
            data.setAuthorName(getString(R.string.intro_vote_item_author_name));
            data.setTitle(getString(R.string.intro_vote_item_title));
            data.setOption1Title(getString(R.string.intro_vote_item_option1));
            data.setOption2Title(getString(R.string.intro_vote_item_option2));
            data.setPollCount(30);
            data.setOption1Count(15);
            data.setOption2Count(15);
            data.setStartTime(System.currentTimeMillis() - 86400000);
            data.setEndTime(System.currentTimeMillis() + 864000000);

            View content = LayoutInflater.from(getActivity()).inflate(R.layout.card_view_wall_item_intro, null);
            TextView txtAuthorName = (TextView) content.findViewById(R.id.txtAuthorName);
            TextView txtTitle = (TextView) content.findViewById(R.id.txtTitle);
            TextView txtOption1 = (TextView) content.findViewById(R.id.txtFirstOptionTitle);
            TextView txtOption2 = (TextView) content.findViewById(R.id.txtSecondOptionTitle);
            TextView txtPubTime = (TextView) content.findViewById(R.id.txtPubTime);
            final TextView txtPollCount = (TextView) content.findViewById(R.id.txtBarPollCount);
            final TextView txtFirstPollCountPercent = (TextView) content.findViewById(R.id.txtFirstPollCountPercent);
            final TextView txtSecondPollCountPercent = (TextView) content.findViewById(R.id.txtSecondPollCountPercent);
            final RoundCornerProgressBar progressFirstOption = (RoundCornerProgressBar) content.findViewById(R.id.progressFirstOption);
            final RoundCornerProgressBar progressSecondOption = (RoundCornerProgressBar) content.findViewById(R.id.progressSecondOption);
            CardView btnThirdOption = (CardView) content.findViewById(R.id.btnThirdOption);
            final CardView btnSecondOption = (CardView) content.findViewById(R.id.btnSecondOption);
            final CardView btnFirstOption = (CardView) content.findViewById(R.id.btnFirstOption);
            final ImageView imgChampion1 = (ImageView) content.findViewById(R.id.imgChampion1);
            final ImageView imgChampion2 = (ImageView) content.findViewById(R.id.imgChampion2);

            ImageView imgAuthorIcon = (ImageView) content.findViewById(R.id.imgAuthorIcon);

            TextDrawable drawable = TextDrawable.builder().beginConfig().width(36).height(36).endConfig()
                    .buildRound(data.getAuthorName().substring(0, 1), R.color.primary_light);
            imgAuthorIcon.setImageDrawable(drawable);

            btnFirstOption.setCardBackgroundColor(getResources().getColor(R.color.md_blue_100));
            btnSecondOption.setCardBackgroundColor(getResources().getColor(R.color.md_blue_100));
            btnThirdOption.setVisibility(View.GONE);

            txtFirstPollCountPercent.setVisibility(View.GONE);
            txtSecondPollCountPercent.setVisibility(View.GONE);

            progressFirstOption.setVisibility(View.GONE);
            progressSecondOption.setVisibility(View.GONE);

            imgChampion1.setVisibility(View.GONE);
            imgChampion2.setVisibility(View.GONE);

            txtAuthorName.setText(data.getAuthorName());
            txtTitle.setText(data.getTitle());
            txtOption1.setText(data.getOption1Title());
            txtOption2.setText(data.getOption2Title());
            txtPubTime.setText(Util.getDate(data.getStartTime(), "yyyy/MM/dd hh:mm")
                    + " ~ " + Util.getDate(data.getEndTime(), "yyyy/MM/dd hh:mm"));
            txtPollCount.setText(Integer.toString(data.getPollCount()));
            progressFirstOption.setProgressColor(getResources().getColor(R.color.md_blue_600));
            progressFirstOption.setProgressBackgroundColor(getResources().getColor(R.color.md_blue_200));
            btnFirstOption.setCardBackgroundColor(getResources().getColor(R.color.md_blue_100));
            progressSecondOption.setProgressColor(getResources().getColor(R.color.md_blue_600));
            progressSecondOption.setProgressBackgroundColor(getResources().getColor(R.color.md_blue_200));
            btnSecondOption.setCardBackgroundColor(getResources().getColor(R.color.md_blue_100));

            View.OnLongClickListener dialogLongClick = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View optionButton) {
                    if (optionButton.getId() == R.id.btnFirstOption) {
                        progressFirstOption.setProgressColor(getResources().getColor(R.color.md_red_600));
                        progressFirstOption.setProgressBackgroundColor(getResources().getColor(R.color.md_red_200));
                        btnFirstOption.setCardBackgroundColor(getResources().getColor(R.color.md_red_100));
                        imgChampion1.setVisibility(View.VISIBLE);
                        imgChampion2.setVisibility(View.INVISIBLE);
                        data.setOption1Count(data.getOption1Count() + 1);
                    } else {
                        progressSecondOption.setProgressColor(getResources().getColor(R.color.md_red_600));
                        progressSecondOption.setProgressBackgroundColor(getResources().getColor(R.color.md_red_200));
                        btnSecondOption.setCardBackgroundColor(getResources().getColor(R.color.md_red_100));
                        imgChampion2.setVisibility(View.VISIBLE);
                        imgChampion1.setVisibility(View.INVISIBLE);
                        data.setOption2Count(data.getOption2Count() + 1);
                    }

                    progressFirstOption.setVisibility(View.VISIBLE);
                    progressFirstOption.setProgress(data.getOption1Count());

                    progressSecondOption.setVisibility(View.VISIBLE);
                    progressSecondOption.setProgress(data.getOption2Count());

                    txtFirstPollCountPercent.setVisibility(View.VISIBLE);
                    txtSecondPollCountPercent.setVisibility(View.VISIBLE);
                    data.setPollCount(data.getPollCount() + 1);
                    progressFirstOption.setMax(data.getPollCount());
                    progressSecondOption.setMax(data.getPollCount());
                    txtPollCount.setText(Integer.toString(data.getPollCount()));

                    double percent1 = data.getPollCount() == 0 ? 0
                            : (double) data.getOption1Count() / data.getPollCount() * 100;
                    double percent2 = data.getPollCount() == 0 ? 0
                            : (double) data.getOption2Count() / data.getPollCount() * 100;
                    txtFirstPollCountPercent.setText(String.format("%3.1f%%", percent1));
                    txtSecondPollCountPercent.setText(String.format("%3.1f%%", percent2));
                    Toast.makeText(getActivity(), R.string.toast_network_connect_success_poll
                            , Toast.LENGTH_SHORT).show();
                    btnFirstOption.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            introductionDialog.dismiss();
                        }
                    }, 3000);
                    return false;
                }
            };
            btnFirstOption.setOnLongClickListener(dialogLongClick);
            btnSecondOption.setOnLongClickListener(dialogLongClick);

            introductionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            introductionDialog.setContentView(content, new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            introductionDialog.setCancelable(false);
            introductionDialog.show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        vpHeader.stopAutoScroll();
    }

    @Override
    public void onStart() {
        super.onStart();
        pagePresenter.resetPromotion();
        vpHeader.startAutoScroll();
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO,WHY NO RESPONSE ON HERE
        pagePresenter.refreshAllFragment();
        //pagePresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        pagePresenter.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void showLoadingCircle() {
        circleLoad.setVisibility(View.VISIBLE);
        circleLoad.setText(getString(R.string.vote_detail_circle_loading));
        circleLoad.spin();
    }

    @Override
    public void hideLoadingCircle() {
        circleLoad.stopSpinning();
        circleLoad.setVisibility(View.GONE);
    }

    @Override
    public void setupPromotionAdmob(List<Promotion> promotionList, User user) {
        List<PromotionType> promotionTypeList = new ArrayList<>();
        for (int i = 0; i < promotionList.size(); i++) {
            if (i == 0 && ENABLE_PROMOTION_ADMOB && Util.isNetworkConnected(getContext())) {
                promotionTypeList.add(new PromotionType(PromotionType.PROM0TION_TYPE_ADMOB
                        , null));
            }
            promotionTypeList.add(new PromotionType(PromotionType.PROMOTION_TYPE_FUNNY_VOTE
                    , promotionList.get(i)));
        }
        vpHeader.setAdapter(new HeaderAdapter(promotionTypeList, user));
        vpHeader.getAdapter().notifyDataSetChanged();
        vpHeader.startAutoScroll();
    }

    @Override
    public void setUpTabsAdapter(User user) {
        tabsAdapter = new TabsAdapter(getChildFragmentManager(), user);
        int currentItem = vpMainPage.getCurrentItem();
        vpMainPage.setAdapter(tabsAdapter);
        vpMainPage.setCurrentItem(currentItem);
    }

    @Override
    public void setUpTabsAdapter(User user, User targetUser) {
        setUpTabsAdapter(user);
    }

    @Override
    public void showHintToast(int res, long arg) {
        Toast.makeText(context, getString(res, arg), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPollPasswordDialog(final VoteData data, final String optionCode) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.password_dialog);
        builder.setPositiveButton(getActivity().getResources()
                .getString(R.string.vote_detail_dialog_password_input), null);
        builder.setNegativeButton(getContext().getApplicationContext().getResources()
                .getString(R.string.account_dialog_cancel), null);
        builder.setTitle(getActivity().getString(R.string.vote_detail_dialog_password_title));
        passwordDialog = builder.create();

        passwordDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final EditText password = (EditText) ((AlertDialog) dialogInterface).findViewById(R.id.edtEnterPassword);
                Button ok = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        Log.d(TAG, "showPollPasswordDialog PW:");
                        pagePresenter.pollVote(data, optionCode, password.getText().toString());
//                        tracker.send(new HitBuilders.EventBuilder()
//                                .setCategory(tab)
//                                .setAction(AnalyzticsTag.ACTION_QUICK_POLL_VOTE)
//                                .setLabel(data.getVoteCode())
//                                .build());
                    }
                });
            }
        });
        passwordDialog.show();
    }

    @Override
    public void hidePollPasswordDialog() {
        if (passwordDialog != null && passwordDialog.isShowing()) {
            passwordDialog.dismiss();
        }
    }

    @Override
    public void shakePollPasswordDialog() {
        if (passwordDialog != null && passwordDialog.isShowing()) {
            final EditText password = (EditText) passwordDialog.findViewById(R.id.edtEnterPassword);
            password.selectAll();
            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.edittext_shake);
            password.startAnimation(shake);
        }
    }

    @Override
    public boolean isPasswordDialogShowing() {
        return passwordDialog != null && passwordDialog.isShowing();
    }


    private class HeaderAdapter extends PagerAdapter {
        private List<PromotionType> promotionTypeList;
        private User user;

        public HeaderAdapter(List<PromotionType> promotionTypeList, User user) {
            this.promotionTypeList = promotionTypeList;
            this.user = user;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = getActivity().getLayoutInflater().from(getActivity());
            if (promotionTypeList.get(position).getPromotionType() == PromotionType.PROMOTION_TYPE_FUNNY_VOTE) {
                View headerItem = inflater.inflate(R.layout.item_promotion_funny_vote, null);
                ImageView promotion = (ImageView) headerItem.findViewById(R.id.headerImage);
                if (Util.isNetworkConnected(getContext())) {
                    Glide.with(getContext())
                            .load(promotionTypeList.get(position).getPromotion().getImageURL())
                            .override((int) getResources().getDimension(R.dimen.promotion_image_width)
                                    , (int) getResources().getDimension(R.dimen.promotion_image_high))
                            .fitCenter()
                            .crossFade()
                            .into(promotion);
                    final String actionURL = promotionTypeList.get(position).getPromotion().getActionURL();
                    promotion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW
                                    , Uri.parse(actionURL));
                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory(AnalyzticsTag.CATEGORY_PROMOTION)
                                    .setAction(AnalyzticsTag.ACTION_CLICK_PROMOTION)
                                    .setLabel(actionURL)
                                    .build());
                            startActivity(browserIntent);
                        }
                    });
                } else {
                    Glide.with(getContext())
                            .load(R.drawable.main_topic)
                            .override((int) getResources().getDimension(R.dimen.promotion_image_width)
                                    , (int) getResources().getDimension(R.dimen.promotion_image_high))
                            .fitCenter()
                            .crossFade()
                            .into(promotion);
                    final String actionURL = "https://play.google.com/store/apps/details?id=com.heaton.funnyvote";
                    promotion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW
                                    , Uri.parse(actionURL));
                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory(AnalyzticsTag.CATEGORY_PROMOTION)
                                    .setAction(AnalyzticsTag.ACTION_CLICK_PROMOTION)
                                    .setLabel(actionURL)
                                    .build());
                            startActivity(browserIntent);
                        }
                    });
                }
                container.addView(headerItem);
                return headerItem;
            } else if (promotionTypeList.get(position).getPromotionType() == PromotionType.PROM0TION_TYPE_ADMOB) {
                if (promotionADMOB == null) {
                    promotionADMOB = inflater.inflate(R.layout.item_promotion_admob, null);
                    NativeExpressAdView adview = (NativeExpressAdView) promotionADMOB.findViewById(R.id.adViewPromotion);
                    AdRequest adRequest = new AdRequest.Builder()
                            .setGender(user != null && User.GENDER_MALE.equals(user.getGender()) ?
                                    AdRequest.GENDER_MALE : AdRequest.GENDER_FEMALE)
                            .build();
                    adview.loadAd(adRequest);
                }
                container.addView(promotionADMOB);
                return promotionADMOB;
            } else {
                return null;
            }

        }

        @Override
        public int getCount() {
            return promotionTypeList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private class TabsAdapter extends FragmentStatePagerAdapter {
        private User user;

        public TabsAdapter(FragmentManager fm, User user) {
            super(fm);
            this.user = user;
        }

        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            // todo: work around : NullPointerException in FragmentStatePagerAdapter
            //super.restoreState(state, loader);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    if (hotsFragment == null) {
                        hotsFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_HOT, user);
                        hotsFragment.setPresenter(pagePresenter);
                        //pagePresenter.setHotsFragmentView(hotsFragment);
                    }
                    return hotsFragment;
                case 1:
                    if (newsFragment == null) {
                        newsFragment = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_NEW, user);
                        newsFragment.setPresenter(pagePresenter);
                        //pagePresenter.setNewsFragmentView(newsFragment);
                    }
                    return newsFragment;
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return context.getString(R.string.main_page_tab_hot);
                case 1:
                    return context.getString(R.string.main_page_tab_new);
            }
            return "";
        }

    }

    public abstract static class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

        public enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        public abstract void onStateChanged(AppBarLayout appBarLayout, State state);
    }


}
