package ru.abch.goodscollection;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

public class ZoneAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Zone> zones;
    long urgencyGroup;
    int countGroup, timeGroup;
    String TAG = "WorkZoneAdapter";
    public ZoneAdapter(Context context, ArrayList<Zone> zones) {
        ctx = context;
        this.zones = zones;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return zones.size();
    }

    @Override
    public Object getItem(int i) {
        return zones.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.zone_item, parent, false);
        }
        Zone zone = getZone(position);
        Button button = view.findViewById(R.id.bt_zone);
        String text = zone.zonein_descr;
        urgencyGroup = (Config.toJavaTime(Database.getUrgency(zone.zonein)) - System.currentTimeMillis())/1000;
        countGroup = (MainActivity.clientId == null)?
                Database.countPositions(zone.zonein)
                : Database.countPositions(zone.zonein, MainActivity.clientId)
        ;
        timeGroup = (urgencyGroup > 0)? (int)(urgencyGroup/60) : 0;
        text += "\r\n" + countGroup + ctx.getResources().getString(R.string.positions) + timeGroup + ctx.getResources().getString(R.string.mins);
        button.setText(text);
        button.setTag(zone.zonein);
        if(countGroup > 0) {
            if (urgencyGroup < 1800) {
                button.setBackgroundColor(ctx.getResources().getColor(R.color.red));
            } else if (urgencyGroup < 3600) {
                button.setBackgroundColor(ctx.getResources().getColor(R.color.yellow));
            } else {
                button.setBackgroundColor(ctx.getResources().getColor(R.color.green));
            }
        }
        if(countGroup > 0) button.setOnClickListener(view1 -> {
            App.zonein = zone.zonein;
            App.zonein_descr = zone.zonein_descr;
            ((MainActivity) ctx).gotoTimingFragment(zone.zonein);
        });
        return view;
    }
    Zone getZone(int position) {
        return ((Zone) getItem(position));
    }
}
