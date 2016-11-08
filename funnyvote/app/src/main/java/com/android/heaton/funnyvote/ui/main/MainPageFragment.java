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

/**
 * Created by heaton on 16/4/1.
 */
public class MainPageFragment extends android.support.v4.app.Fragment {

    private static final int NUM_PAGES = 5;

    private ViewPager vpHeader;
    private List<View> headerViewList;
    private AppBarLayout appBarMain;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_page_top, null);
        initialHeaderView();

        vpHeader = (ViewPager) view.findViewById(R.id.vpHeader);
        vpHeader.setAdapter(new HeaderAdapter(headerViewList));
        vpHeader.setCurrentItem(0);
        appBarMain = (AppBarLayout) view.findViewById(R.id.appBarMain);
        ViewPager vpMainPage = (ViewPager) view.findViewById(R.id.vpMainPage);
        vpMainPage.setAdapter(new TabsAdapter(getChildFragmentManager()));

        TabLayout tabMainPage = (TabLayout) view.findViewById(R.id.tabLayoutMainPage);
        tabMainPage.setupWithViewPager(vpMainPage);

        CirclePageIndicator titleIndicator = (CirclePageIndicator) view.findViewById(R.id.vpIndicator);
        titleIndicator.setViewPager(vpHeader);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUIChange(EventBusController.UIControlEvent event) {
        Log.d("test", "message:" + event.message);
        if (event.message.equals(EventBusController.UIControlEvent.SCROLL_TO_TOP)) {
            Log.d("test", "message:" + event.message);
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
            // TODO: only for test , use long as image id.
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
}
