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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.Promotion;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.bumptech.glide.Glide;
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

    private AutoScrollViewPager vpHeader;
    private List<View> headerViewList;
    private AppBarLayout appBarMain;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_page_top, null);
        initialHeaderView();

        vpHeader = (AutoScrollViewPager) view.findViewById(R.id.vpHeader);
        vpHeader.setAdapter(new HeaderAdapter(headerViewList));
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
        vpHeader.startAutoScroll();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUIChange(EventBusController.UIControlEvent event) {
        if (event.message.equals(EventBusController.UIControlEvent.SCROLL_TO_TOP)) {
            appBarMain.setExpanded(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initialHeaderView() {
        final LayoutInflater mInflater = getActivity().getLayoutInflater().from(getActivity());
        headerViewList = new ArrayList<>();
        final List<Promotion> promotionList = DataLoader.getInstance(getContext()).queryAllPromotion();
        for (int i = 0; i < promotionList.size(); i++) {
            View headerItem = mInflater.inflate(R.layout.main_page_header_item, null);
            ImageView promotion = (ImageView) headerItem.findViewById(R.id.headerImage);
            Glide.with(this)
                    .load(promotionList.get(i).getImageURL())
                    .override(320, 180)
                    .fitCenter()
                    .crossFade()
                    .into(promotion);
            final String actionURL = promotionList.get(i).getActionURL();
            promotion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW
                            , Uri.parse(actionURL));
                    startActivity(browserIntent);
                }
            });
            headerViewList.add(headerItem);
        }
    }


    private class HeaderAdapter extends PagerAdapter {
        private List<View> listView;

        public HeaderAdapter(List<View> listView) {
            this.listView = listView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = listView.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return listView.size();
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
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_HOT);
                case 1:
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_NEW);
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
