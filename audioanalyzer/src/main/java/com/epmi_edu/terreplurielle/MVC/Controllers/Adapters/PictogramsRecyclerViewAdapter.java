package com.epmi_edu.terreplurielle.MVC.Controllers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.epmi_edu.terreplurielle.MVC.Controllers.Activities.AudioAnalyzerActivity;
import com.epmi_edu.terreplurielle.Utils.BitmapTools;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PictogramsRecyclerViewAdapter extends BasicRecyclerViewAdapter<PictogramsRecyclerViewAdapter.PictoHolder> {
    List mPictograms;

    public PictogramsRecyclerViewAdapter(final RecyclerView recyclerView, int orientation, String layoutType,
                                         int gridSpan, List pictograms) {
        super(recyclerView, orientation, layoutType, gridSpan, false);
        mPictograms = pictograms;
    }

    @Override
    public int getItemCount() {
        return mPictograms.size();
    }

    @Override
    public PictoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.pictogram_view, parent, false);
        return new PictoHolder(view, this, context);
    }

    @Override
    public void bindItem(BasicRecyclerViewAdapter.ItemHolder itemHolder, int position) {
        itemHolder.bind(mPictograms.get(position));
    }

    public class PictoHolder extends BasicRecyclerViewAdapter.ItemHolder {
        private Context mContext;
        private String mPictogramTitle;

        public PictoHolder(View view, PictogramsRecyclerViewAdapter parentAdapter, Context context) {
            super(view, parentAdapter, false);

            this.mContext = context;
        }

        @Override
        public void bind(Object item) {
            HashMap<String, Integer> pictogram = (HashMap<String, Integer>) item;
            int pictoIconId = 0;
            for (final Map.Entry<String, Integer> entry : pictogram.entrySet()) {
                mPictogramTitle = entry.getKey();
                pictoIconId = entry.getValue();
            }

            ImageView pictoImageView = (ImageView) itemView.findViewById(R.id.image_view_pictogram);
            BitmapTools.setBackgroundFromDrawable(pictoImageView, mContext, pictoIconId);

            TextView titleTextView = (TextView) itemView.findViewById(R.id.text_view_title);
            titleTextView.setText(mPictogramTitle);
        }

        @Override
        protected void itemClicked(View v, boolean newSelection) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("text", mPictogramTitle);
            ((AudioAnalyzerActivity) mContext).onMessage("tts-pictogram-speak", params);
        }
    }
}