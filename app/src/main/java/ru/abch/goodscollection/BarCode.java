package ru.abch.goodscollection;

class BarCode {
    String goods, barcode;
    int qnt;
    BarCode(String goods, String barcode, int qnt) {
        this.barcode = barcode;
        this.goods = goods;
        this.qnt = qnt;
    }
}
