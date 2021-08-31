package ru.abch.goodscollection;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class GoodsPickingViewModel extends ViewModel {
    ArrayList<GoodsMovement> goodsMovements;
    public void setGoodsMovements(ArrayList<GoodsMovement> gms) {
        goodsMovements = gms;
    }
    public ArrayList<GoodsMovement> getGoodsMovements() {
        return goodsMovements;
    }
    public GoodsPickingViewModel() {

    }
}