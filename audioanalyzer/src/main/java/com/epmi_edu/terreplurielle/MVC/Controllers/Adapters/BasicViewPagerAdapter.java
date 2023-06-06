package com.epmi_edu.terreplurielle.MVC.Controllers.Adapters;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.epmi_edu.terreplurielle.MVC.Controllers.Fragments.BasicFragment;
import com.epmi_edu.terreplurielle.Utils.ErrorReporting;
import com.epmi_edu.terreplurielle.Utils.Functions;
import com.google.android.material.tabs.TabLayout;


import java.util.ArrayList;
import java.util.List;

public abstract class BasicViewPagerAdapter extends FragmentStatePagerAdapter {
    protected ViewPager mViewPager;
    protected List<BasicViewPagerFragment> mFragmentList;
    private Activity mActivity;
    private TabLayout mTabLayout;
    private Class[] mTabList;
    private Object mItemData;
    private int FRAGMENT_COUNT = 0;
    public BasicViewPagerAdapter(FragmentManager fm, Activity activity, int viewPagerId,
                                 int tabResourseId, Class[] fragmentClassNames, ITabCustomView ITabCustomViewView) {
        super(fm);

        try {
            FRAGMENT_COUNT = fragmentClassNames.length;
            mTabList = fragmentClassNames;

            mActivity = activity;

            mViewPager = (ViewPager) mActivity.findViewById(viewPagerId);
            mViewPager.setOffscreenPageLimit(Math.min(5, FRAGMENT_COUNT - 1));
            mViewPager.setAdapter(this);

            mTabLayout = (TabLayout) mActivity.findViewById(tabResourseId);
            mFragmentList = new ArrayList<>();
            for (int i = 0; i < FRAGMENT_COUNT; i++)
                addFragment(i, fragmentClassNames[i], ITabCustomViewView);

            mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
            mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    BasicViewPagerFragment fragment = mFragmentList.get(position);
                    if (fragment != null) fragment.loadUI(mItemData);
                    mItemData = null;
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        } catch (Exception e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    public int getCount() {
        return FRAGMENT_COUNT;
    }

    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public int visibleIndex() {
        return mViewPager.getCurrentItem();
    }

    private void addFragment(int position, Class framentClassName, ITabCustomView ITabCustomViewView) throws Exception {
        try {
            BasicViewPagerFragment fragment = (BasicViewPagerFragment) Functions.createClassInstance(framentClassName.getName());
            mFragmentList.add(fragment);
            fragment.setViewPager(mViewPager);

            TabLayout.Tab tab = mTabLayout.newTab();
            String tabName = mTabList[position].getSimpleName().replace("Fragment", "");
            int tabIconId = Functions.getResourceId(mActivity, "ic_" + tabName.toLowerCase(),
                    "drawable");

            String tabLabel = Functions.getStringResourceByName(tabName);
            if (tabLabel == null || tabLabel.isEmpty()) tabLabel = tabName;

            if (ITabCustomViewView == null) {
                tab.setText(tabLabel);
                if (tabIconId > 0) tab.setIcon(tabIconId);
            } else tab.setCustomView(ITabCustomViewView.getView(position, tabLabel, tabIconId));

            mTabLayout.addTab(tab);
        } catch (Exception e) {
            new ErrorReporting(e, this.getClass().getName());
        }
    }

    public void enableView(boolean enable) {
        mViewPager.setEnabled(enable);
        mTabLayout.setEnabled(enable);

        for (int i = 0; i < FRAGMENT_COUNT; i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.getCustomView().setEnabled(enable);
            mFragmentList.get(i).getView().setEnabled(enable);
        }
    }

    public interface ITabCustomView {
        View getView(int position, String label, int iconId);
    }

    public abstract static class BasicViewPagerFragment extends BasicFragment {
        protected Context context;
        protected BasicViewPagerAdapter mViewPagerAdapter;
        protected ViewPager mViewPager;

        public abstract void loadUI(Object data);

        public void setViewPager(ViewPager viewPager) {
            mViewPager = viewPager;
            mViewPagerAdapter = (BasicViewPagerAdapter) mViewPager.getAdapter();
        }

        @TargetApi(23)
        public void onAttach(Context context) {
            super.onAttach(context);
            this.context = context;
        }

        public void onAttach(Activity activity) {
            super.onAttach(activity);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                this.context = activity.getBaseContext();
        }

        public void onDetach() {
            super.onDetach();
        }
    }
}