package com.android.heaton.easyvote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 16/4/1.
 */
public class MainPageFragment extends android.support.v4.app.Fragment {

    private static final int NUM_PAGES = 5;

    private ViewPager vpHeader;
    private List<View> headerViewList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_page_top, null);
        initialHeaderView();

        vpHeader = (ViewPager) view.findViewById(R.id.vpHeader);
        vpHeader.setAdapter(new HeaderAdapter(headerViewList));
        vpHeader.setCurrentItem(0);

        ViewPager vpMainPage  = (ViewPager) view.findViewById(R.id.vpMainPage);
        vpMainPage.setAdapter(new TabsAdapter(getChildFragmentManager()));

        TabLayout tabMainPage = (TabLayout) view.findViewById(R.id.tabLayoutMainPage);
        tabMainPage.setupWithViewPager(vpMainPage);

        CirclePageIndicator titleIndicator = (CirclePageIndicator) view.findViewById(R.id.vpIndicator);
        titleIndicator.setViewPager(vpHeader);

        return view;
    }

    @Override
    public void onDestroyView() {
        Log.d("heaton1","MainPageFragment onDestroyView :");
        super.onDestroyView();
    }

    private void initialHeaderView() {
        final LayoutInflater mInflater = getActivity().getLayoutInflater().from(getActivity());
        headerViewList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            View headerItem = mInflater.inflate(R.layout.main_page_header_item, null);
            ImageView promotion = (ImageView) headerItem.findViewById(R.id.headerImage);
            int imageOrder = (int)(Math.random()*5);
            int imageId = R.mipmap.ballot_box;
            switch (imageOrder) {
                case 0:
                    imageId = R.mipmap.ballot_box;
                    break;
                case 1:
                    imageId = R.mipmap.handsup;
                    break;
                case 2:
                    imageId = R.mipmap.vote_banner;
                    break;
                case 3:
                    imageId = R.mipmap.vote_box;
                    break;
                case 4:
                    imageId = R.mipmap.vote_finger;

            }
            promotion.setImageResource(imageId);
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
            Log.d("heaton1","MainPageFragment TabsAdapter:");
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            Log.d("heaton1","MainPageFragment getItem:");
            switch(i) {
                case 0: return MainPageTabFragment.newInstance();
                case 1: return MainPageTabFragment.newInstance();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0: return getString(R.string.main_page_tab_hot);
                case 1: return getString(R.string.main_page_tab_new);
            }
            return "";
        }
    }
}
