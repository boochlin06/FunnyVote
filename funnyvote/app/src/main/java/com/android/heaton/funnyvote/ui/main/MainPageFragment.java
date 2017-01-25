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
import com.viewpagerindicator.CirclePageIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;

/**
 * Created by heaton on 16/4/1.
 */
public class MainPageFragment extends android.support.v4.app.Fragment {

    public static String TAG = MainPageFragment.class.getSimpleName();
    private AutoScrollViewPager vpHeader;
    private AppBarLayout appBarMain;
    private List<Promotion> promotionList;
    private PromotionManager promotionManager;
    private UserManager userManager;
    private User user;
    private UserManager.GetUserCallback getUserCallback = new UserManager.GetUserCallback() {
        @Override
        public void onResponse(User user) {
            MainPageFragment.this.user = user;
            promotionManager.getPromotionList(user);
        }

        @Override
        public void onFailure() {

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        promotionManager = PromotionManager.getInstance(getContext().getApplicationContext());
        userManager = UserManager.getInstance(getContext().getApplicationContext());
        userManager.getUser(getUserCallback);

        View view = inflater.inflate(R.layout.fragment_main_page_top, null);
        initialHeaderView();

        vpHeader = (AutoScrollViewPager) view.findViewById(R.id.vpHeader);
        vpHeader.setAdapter(new HeaderAdapter());
        vpHeader.setCurrentItem(0);
        appBarMain = (AppBarLayout) view.findViewById(R.id.appBarMain);
        ViewPager vpMainPage = (ViewPager) view.findViewById(R.id.vpMainPage);
        vpMainPage.setAdapter(new TabsAdapter(getChildFragmentManager()));

        TabLayout tabMainPage = (TabLayout) view.findViewById(R.id.tabLayoutMainPage);
        tabMainPage.setupWithViewPager(vpMainPage);

        CirclePageIndicator titleIndicator = (CirclePageIndicator) view.findViewById(R.id.vpIndicator);
        titleIndicator.setViewPager(vpHeader);
        vpHeader.setInterval(5000);
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
                vpHeader.getAdapter().notifyDataSetChanged();
                Log.d(TAG, "GET_PROMOTION_LIST:" + promotionList.size());
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
    }


    private class HeaderAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater mInflater = getActivity().getLayoutInflater().from(getActivity());
            View headerItem = mInflater.inflate(R.layout.main_page_header_item, null);
            ImageView promotion = (ImageView) headerItem.findViewById(R.id.headerImage);
            Glide.with(getContext())
                    .load(promotionList.get(position).getImageURL())
                    .override(320, 180)
                    .fitCenter()
                    .crossFade()
                    .into(promotion);
            final String actionURL = promotionList.get(position).getActionURL();
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
        }

        @Override
        public int getCount() {
            return promotionList.size();
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
