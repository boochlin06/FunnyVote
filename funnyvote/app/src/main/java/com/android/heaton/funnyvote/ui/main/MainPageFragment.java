package com.android.heaton.funnyvote.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.data.promotion.PromotionManager;
import com.android.heaton.funnyvote.data.user.UserManager;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.Promotion;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.viewpagerindicator.CirclePageIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;

/**
 * Created by heaton on 16/4/1.
 */
public class MainPageFragment extends android.support.v4.app.Fragment {

    public static String TAG = MainPageFragment.class.getSimpleName();
    public static boolean ENABLE_PROMOTION_ADMOB = true;
    private AutoScrollViewPager vpHeader;
    private AppBarLayout appBarMain;
    private List<Promotion> promotionList;
    private List<PromotionType> promotionTypeList;
    private View promotionADMOB;
    private PromotionManager promotionManager;
    private TabsAdapter tabsAdapter;
    private ViewPager vpMainPage;
    private UserManager userManager;
    private User user;

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

    private UserManager.GetUserCallback getUserCallback = new UserManager.GetUserCallback() {
        @Override
        public void onResponse(User user) {
            MainPageFragment.this.user = user;
            promotionManager.getPromotionList(user);
            tabsAdapter = new TabsAdapter(getChildFragmentManager());
            vpMainPage.setAdapter(tabsAdapter);
            Log.d(TAG, "getUserCallback user:" + user.getType());
        }

        @Override
        public void onFailure() {
            promotionManager.getPromotionList(user);
            tabsAdapter = new TabsAdapter(getChildFragmentManager());
            vpMainPage.setAdapter(tabsAdapter);

            Toast.makeText(getContext().getApplicationContext()
                    , R.string.toast_network_connect_error_get_list, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "getUserCallback user failure:" + user);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_page_top, null);
        initialHeaderView();

        vpHeader = (AutoScrollViewPager) view.findViewById(R.id.vpHeader);
        vpHeader.setAdapter(new HeaderAdapter());
        vpHeader.setCurrentItem(0);
        appBarMain = (AppBarLayout) view.findViewById(R.id.appBarMain);
        vpMainPage = (ViewPager) view.findViewById(R.id.vpMainPage);

        TabLayout tabMainPage = (TabLayout) view.findViewById(R.id.tabLayoutMainPage);
        tabMainPage.setupWithViewPager(vpMainPage);

        CirclePageIndicator titleIndicator = (CirclePageIndicator) view.findViewById(R.id.vpIndicator);
        titleIndicator.setViewPager(vpHeader);
        vpHeader.setInterval(100000);
        vpHeader.setScrollDurationFactor(5);
        vpHeader.startAutoScroll();
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

        ENABLE_PROMOTION_ADMOB = getResources().getBoolean(R.bool.enable_promotion_admob);
        promotionManager = PromotionManager.getInstance(getContext().getApplicationContext());
        userManager = UserManager.getInstance(getContext().getApplicationContext());
        userManager.getUser(getUserCallback);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        vpHeader.stopAutoScroll();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (user != null) {
            promotionManager.getPromotionList(user);
        }
        vpHeader.startAutoScroll();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUIChange(EventBusController.UIControlEvent event) {
        if (event.message.equals(EventBusController.UIControlEvent.SCROLL_TO_TOP)) {
            appBarMain.setExpanded(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteEvent(EventBusController.RemoteServiceEvent event) {
        if (event.message.equals(EventBusController.RemoteServiceEvent.GET_PROMOTION_LIST)) {
            if (event.success) {
                promotionList = event.promotionList;
                setupPromotionAdmob();
                vpHeader.getAdapter().notifyDataSetChanged();
                Log.d(TAG, "GET_PROMOTION_LIST:" + promotionList.size() + ",type list size:" + promotionTypeList.size());
            } else {
                Toast.makeText(getContext(), R.string.toast_network_connect_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initialHeaderView() {
        promotionList = DataLoader.getInstance(getContext()).queryAllPromotion();
        setupPromotionAdmob();
    }

    private void setupPromotionAdmob() {
        promotionTypeList = new ArrayList<>();
        for (int i = 0; i < promotionList.size(); i++) {
            if (i % 3 == 1 && ENABLE_PROMOTION_ADMOB) {
                promotionTypeList.add(new PromotionType(PromotionType.PROM0TION_TYPE_ADMOB, null));

            }
            promotionTypeList.add(new PromotionType(PromotionType.PROMOTION_TYPE_FUNNY_VOTE
                    , promotionList.get(i)));
        }
    }


    private class HeaderAdapter extends PagerAdapter {

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
                Glide.with(getContext())
                        .load(promotionTypeList.get(position).getPromotion().getImageURL())
                        .override(320, 180)
                        .fitCenter()
                        .crossFade()
                        .into(promotion);
                final String actionURL = promotionTypeList.get(position).getPromotion().getActionURL();
                promotion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW
                                , Uri.parse(actionURL));
                        startActivity(browserIntent);
                    }
                });
                container.addView(headerItem);
                return headerItem;
            } else if (promotionTypeList.get(position).getPromotionType() == PromotionType.PROM0TION_TYPE_ADMOB) {
                if (promotionADMOB == null) {
                    promotionADMOB = inflater.inflate(R.layout.item_promotion_admob, null);
                    NativeExpressAdView adview = (NativeExpressAdView) promotionADMOB.findViewById(R.id.adViewPromotion);
                    AdRequest adRequest = new AdRequest.Builder()
                            .setGender(user.getGender().equals(User.GENDER_MALE)?
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

    private class TabsAdapter extends FragmentPagerAdapter {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_HOT, user);
                case 1:
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_NEW, user);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.main_page_tab_hot);
                case 1:
                    return getString(R.string.main_page_tab_new);
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
