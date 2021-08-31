package ru.abch.goodscollection;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class TimingAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Timing> timings;
    TimingAdapter(Context context, ArrayList<Timing> timings) {
        ctx = context;
        this.timings = timings;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return timings.size();
    }

    @Override
    public Object getItem(int i) {
        return timings.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.timing_item, parent, false);
        }
        Timing timing = getTiming(position);
        ((TextView) view.findViewById(R.id.timing_item_name)).setText(timing.name);
        ((TextView) view.findViewById(R.id.timing_item_qty)).setText(timing.qty + ctx.getResources().getString(R.string.positions));
        if(position == 0) {
            ((TextView) view.findViewById(R.id.timing_item_name)).setTypeface(Typeface.DEFAULT_BOLD);
            ((TextView) view.findViewById(R.id.timing_item_qty)).setTypeface(Typeface.DEFAULT_BOLD);
        }
        return view;
    }
    Timing getTiming(int position) {
        return ((Timing) getItem(position));
    }
}
