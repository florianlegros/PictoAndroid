package com.epmi_edu.terreplurielle.MVC.Controllers.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.epmi_edu.terreplurielle.Utils.Functions;
import com.epmi_edu.terreplurielle.audioanalyzer.R;

public class BasicSpinnerAdapter extends BaseAdapter implements android.widget.SpinnerAdapter {
    private final Context activity;

    ;
    private String[] items;
    private CustomItemView customItemView;
    public BasicSpinnerAdapter(Context context, Spinner spinner, String[] items, CustomItemView customItemView,
                               int intSelection, String strSelection, final SpinnerSelectedItem itemSelectionCallback) {
        this.items = items;
        activity = context;
        this.customItemView = customItemView;

        spinner.setAdapter(this);
        spinner.setDropDownVerticalOffset(spinner.getLayoutParams().height);

        if (intSelection != -1) spinner.setSelection(intSelection);
        else if (strSelection != null && !strSelection.isEmpty()) {
            int len = items.length;
            for (int i = 0; i < len; i++) {
                if (items[i].equals(strSelection)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        if (itemSelectionCallback != null) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    itemSelectionCallback.onSelect(parent, view, position, id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    public int getCount() {
        return customItemView == null ? items.length : customItemView.itemCount();
    }

    public Object getItem(int i) {
        return items[i];
    }

    public long getItemId(int i) {
        return (long) i;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, parent, 0);
    }

    public View getView(int position, View view, ViewGroup parent) {
        return createItemView(position, parent, R.drawable.ic_down);
    }

    private View createItemView(int position, ViewGroup parent, int spinnerArrowId) {
        if (customItemView != null)
            return customItemView.createItemView(position, parent, spinnerArrowId);

        TextView txt = new TextView(activity);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        int margin = Functions.toScreenValue(2);
        txt.setPaddingRelative(Functions.toScreenValue(15), margin, margin, margin);
        txt.setTextSize(18);

        if (spinnerArrowId > 0) {
            txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, spinnerArrowId, 0);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            txt.setGravity(/*Gravity.FILL_HORIZONTAL |*/ Gravity.CENTER_HORIZONTAL | Gravity.CLIP_HORIZONTAL);
            txt.setLayoutParams(layoutParams);

            txt.setBackground(ContextCompat.getDrawable(activity, R.drawable.bkg_spinner));
        } else
            txt.setBackground(ContextCompat.getDrawable(activity, R.drawable.bkg_spinner_dropdown));

        txt.setText(items[position]);
        txt.setTextColor(ContextCompat.getColor(activity, R.color.spinner_color));

        Typeface typeface = Typeface.create("@font/timesbi", Typeface.NORMAL);
        txt.setTypeface(typeface);

        txt.setTypeface(Typeface.create("font/timesbi", Typeface.NORMAL));
        return txt;
    }

    public interface SpinnerSelectedItem {
        void onSelect(AdapterView<?> parent, View view, int position, long id);
    }

    public interface CustomItemView {
        View createItemView(int position, ViewGroup parent, int spinnerArrowId);

        int itemCount();
    }
}