package ru.abch.goodscollection;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class PickedAdapter extends BaseAdapter {
    Context ctx;
    String TAG= "PickedAdapter" ;
    LayoutInflater lInflater;
    ArrayList<GoodsMovement> goods;
    PickedAdapter(Context context, ArrayList<GoodsMovement> goods) {
        ctx = context;
        this.goods = goods;
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return goods.size();
    }

    @Override
    public Object getItem(int i) {
        return goods.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.picked_item, parent, false);
        }
        GoodsMovement gm = getGoods(position);
        ((TextView) view.findViewById(R.id.picked_item_article)).setText(gm.goods_article);
        ((TextView) view.findViewById(R.id.picked_item_name)).setText(gm.goods_descr);
        ((TextView) view.findViewById(R.id.picked_item_qty)).setText(gm.qnt + " " + gm.units);
        return view;
    }
    GoodsMovement getGoods(int position) {
        return ((GoodsMovement) getItem(position));
    }
}
