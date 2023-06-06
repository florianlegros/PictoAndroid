package com.epmi_edu.terreplurielle.MVC.Controllers.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.AudioAnalyzerActivity;
import com.epmi_edu.terreplurielle.Utils.Functions;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActionsRecyclerViewAdapter extends BasicRecyclerViewAdapter<ActionsRecyclerViewAdapter.ActionHolder> {
    List mIconIds;
    List mViews;
    public ActionsRecyclerViewAdapter(final RecyclerView recyclerView, int orientation, String layoutType,
                                      int gridSpan, List actionIds) {
        super(recyclerView, orientation, layoutType, gridSpan, false);
        mIconIds = actionIds;
        mViews = new ArrayList();
    }

    @Override
    public int getItemCount() {
        return mIconIds.size();
    }

    @Override
    public ActionsRecyclerViewAdapter.ActionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.action_view, parent, false);
        return new ActionsRecyclerViewAdapter.ActionHolder(view, this, context, mViews);
    }

    @Override
    public void bindItem(BasicRecyclerViewAdapter.ItemHolder itemHolder, int position) {
        itemHolder.bind(mIconIds.get(position));
    }

    public void enableButtons(boolean enable) {
        int buttonCount = mViews.size();
        for (int i = 0; i < buttonCount; i++) ((View) mViews.get(i)).setEnabled(enable);
    }

    public void onMessage(String message, final HashMap<String, Object> args) {
        switch (message) {
            case "audio-player-complete":
                String fragmentName = (String) args.get("fragment");
                int buttonIndex = -1, iconId;
                switch (fragmentName) {
                    case "TTSFragment":
                        buttonIndex = 0;
                        break;
                    case "TeacherFragment":
                        buttonIndex = 2;
                        break;
                    case "KidFragment":
                        buttonIndex = 4;
                }

                if (buttonIndex != -1) {
                    ActionHolder holder = (ActionHolder) mRecyclerView.findContainingViewHolder(mRecyclerView.getChildAt(buttonIndex));
                    if (holder != null) holder.switchItemIcon();
                }

                break;

            case "audio-record-max-time-reached":
                ActionHolder holder = (ActionHolder) mRecyclerView.findContainingViewHolder(mRecyclerView.getChildAt(1));
                if (holder != null) holder.switchItemIcon();
        }
    }

    public class ActionHolder extends BasicRecyclerViewAdapter.ItemHolder {
        private Context mContext;
        private int mIconId;
        private ImageView mActionImage;

        public ActionHolder(View view, ActionsRecyclerViewAdapter parentAdapter, Context context, List parentViewList) {
            super(view, parentAdapter, false);
            this.mContext = context;

            parentViewList.add(view);
        }

        @Override
        public void bind(Object item) {
            mIconId = (int) item;
            mActionImage = (ImageView) itemView.findViewById(R.id.image_view_action);
            mActionImage.setBackgroundResource(mIconId);
        }

        @Override
        protected void itemClicked(View v, boolean newSelection) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("iconId", mIconId);
            ((AudioAnalyzerActivity) mContext).onMessage("on-header-button", params);

            switchItemIcon();
            v.setEnabled(true);
        }

        public void switchItemIcon() {
            int newId = -1;
            if (mIconId == R.drawable.ic_record_teacher) newId = R.drawable.ic_stop_record_teacher;
            else if (mIconId == R.drawable.ic_stop_record_teacher)
                newId = R.drawable.ic_record_teacher;
            else if (mIconId == R.drawable.ic_play_teacher) newId = R.drawable.ic_stop_play_teacher;
            else if (mIconId == R.drawable.ic_stop_play_teacher) newId = R.drawable.ic_play_teacher;
            else if (mIconId == R.drawable.ic_record_kid) newId = R.drawable.ic_stop_record_kid;
            else if (mIconId == R.drawable.ic_stop_record_kid) newId = R.drawable.ic_record_kid;
            else if (mIconId == R.drawable.ic_play_kid) newId = R.drawable.ic_stop_play_kid;
            else if (mIconId == R.drawable.ic_stop_play_kid) newId = R.drawable.ic_play_kid;
            else if (mIconId == R.drawable.ic_play_tts) newId = R.drawable.ic_stop_play_tts;
            else if (mIconId == R.drawable.ic_stop_play_tts) newId = R.drawable.ic_play_tts;

            if (newId != -1) {
                mIconId = newId;
                Functions.HandleUIFromAnotherThread((Activity) mContext, new Runnable() {
                    public void run() {
                        mActionImage.setBackgroundResource(mIconId);
                    }
                });
            }
        }
    }
}
