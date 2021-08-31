package ru.abch.goodscollection;

public class GoodsMovement {
    public String goods, goods_descr, cellOut, cellOut_descr, cellIn, cellIn_descr, cellOut_task,
            cellIn_task, mdoc, goods_article, goods_brand, units, zonein, zonein_descr, dctNum, url;
    public int iddocdef, qnt, distance;
    public long startTime, rowId;
    public String dest_id, dest_descr;
    public GoodsMovement(String goods, String goods_descr, String cellOut, String cellOut_descr, String cellIn, String cellIn_descr,
                         String cellOut_task, String cellIn_task, String mdoc, int iddocdef, int qnt, long startTime, String goods_article,
                         String goods_brand, String units, String zonein, String zonein_descr, String dest_id, String dest_descr) {
        this.goods = goods;
        this.goods_descr = goods_descr;
        this.cellOut = cellOut;
        this.cellOut_descr = cellOut_descr;
        this.cellIn = cellIn;
        this.cellIn_descr = cellIn_descr;
        this.cellOut_task = cellOut_task;
        this.cellIn_task = cellIn_task;
        this.mdoc = mdoc;
        this.iddocdef = iddocdef;
        this.qnt = qnt;
        this.startTime = startTime;
        this.goods_article = goods_article;
        this.goods_brand = goods_brand;
        this.units = units;
        this.zonein = zonein;
        this.zonein_descr = zonein_descr;
        this.url = null;
        this.dest_id = dest_id;
        this.dest_descr = dest_descr;
    }
    public int getDistance(int from) {
        int ret = (from > distance)? from - distance : distance - from;
        return ret;
    }
}