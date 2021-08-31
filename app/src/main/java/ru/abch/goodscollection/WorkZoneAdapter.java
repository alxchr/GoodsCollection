package ru.abch.goodscollection;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WorkZoneAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Zone> workZones;
    String TAG = "ZoneAdapter";
//    CheckBox workZone;
    public WorkZoneAdapter(Context context, ArrayList<Zone> workZones) {
        ctx = context;
        this.workZones = workZones;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return workZones.size();
    }

    @Override
    public Object getItem(int i) {
        return workZones.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder holder;
        CheckBox workZone;
        Zone zone = getZone(position);
        if (view == null) {
            view = lInflater.inflate(R.layout.work_zone_item, parent, false);
            workZone= view.findViewById(R.id.cb_work_zone);
            holder = new ViewHolder();
            holder.cb = workZone;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.cb.setText(zone.zonein_descr);
        holder.cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (holder.cb.isChecked()) {
                    Log.d(TAG, "Checked " + position + " position");
                    zone.checked = true;
                }  else  {
                    Log.d(TAG, "Unchecked " + position + " position");
                    zone.checked = false;
                }
            }
        });
        return view;
    }
    Zone getZone(int position) {
        return ((Zone) getItem(position));
    }
    private class ViewHolder {
        CheckBox cb;
    }
}
