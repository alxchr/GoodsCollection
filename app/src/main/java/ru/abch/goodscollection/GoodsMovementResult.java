package ru.abch.goodscollection;

public class GoodsMovementResult {
    public boolean success;
    public int counter, storeman;
    public GoodsMovement[] goodsMovements;
    public String timestamp;
    public GoodsMovementResult(boolean success, int counter, int storeman) {
        this.success = success;
        this.counter = counter;
        this.goodsMovements = null;
        this.timestamp = null;
        this.storeman = storeman;
    }
}
