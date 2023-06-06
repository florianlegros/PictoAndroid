package com.epmi_edu.terreplurielle.MVC.Controllers.Adapters;
/**
 * Yacine BOURADA : 07 / 06 / 2018
 */

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.epmi_edu.terreplurielle.Utils.BitmapTools;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

public abstract class BasicRecyclerViewAdapter<T extends BasicRecyclerViewAdapter.ItemHolder>
        extends RecyclerView.Adapter<BasicRecyclerViewAdapter.ItemHolder> {

    protected View selectedItem;
    protected RecyclerView mRecyclerView;
    public BasicRecyclerViewAdapter(RecyclerView recyclerView, int orientation, String layoutType, int gridSpan,
                                    boolean addTouchListener) {
        mRecyclerView = recyclerView;
        //mRecyclerView.offsetChildrenHorizontal(1);
        mRecyclerView.setAdapter(this);
        //mRecyclerView.setItemViewCacheSize(20);

        Context context = mRecyclerView.getContext();
        RecyclerView.LayoutManager layoutManager = null;

        switch (layoutType) {
            case "linear":
                layoutManager = new LinearLayoutManager(context, orientation, false);
                break;

            case "grid":
                layoutManager = new GridLayoutManager(context, gridSpan);
        }

        if (layoutManager == null) return;

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (!addTouchListener) return;

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_MOVE)
                    rv.getParent().requestDisallowInterceptTouchEvent(true);

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    public View getSelectedItem() {
        return selectedItem;
    }

    public void selecteItem(View selectedItem) {
        this.selectedItem = selectedItem;
    }

    @Override
    public abstract ItemHolder onCreateViewHolder(ViewGroup parent, int viewType);

    public abstract void bindItem(BasicRecyclerViewAdapter.ItemHolder itemHolder, int position);

    @Override
    public void onBindViewHolder(BasicRecyclerViewAdapter.ItemHolder itemHolder, int position) {
        bindItem(itemHolder, position);
    }

    @Override
    public abstract int getItemCount();

    public void enableRecyclerView(boolean enable) {
        mRecyclerView.setEnabled(enable);
    }

    public abstract class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected View rVHolderView;
        private BasicRecyclerViewAdapter<ItemHolder> parentAdapter;
        private boolean mDrawSelectionRect;

        public ItemHolder(View view, BasicRecyclerViewAdapter<ItemHolder> parentAdapter, boolean drawSelectionRect) {
            super(view);
            this.parentAdapter = parentAdapter;
            this.mDrawSelectionRect = drawSelectionRect;
            this.rVHolderView = view;
            view.setOnClickListener(this);
        }

        protected abstract void itemClicked(View v, boolean newSelection);

        @Override
        public void onClick(View v) {
            View selectedItem = getSelectedItem();
            boolean newSelection = selectedItem == rVHolderView ? false : true;
            if (mDrawSelectionRect && newSelection) {
                if (selectedItem != null)
                    BitmapTools.setBackgroundFromDrawable(selectedItem, selectedItem.getContext(), android.R.color.transparent);

                BitmapTools.setBackgroundFromDrawable(rVHolderView, rVHolderView.getContext(), R.drawable.recycler_view_selection);
            }

            itemClicked(v, newSelection);

            parentAdapter.selecteItem(rVHolderView);
        }

        protected View getSelectedItem() {
            return parentAdapter.getSelectedItem();
        }

        public abstract void bind(Object item);
    }
}
