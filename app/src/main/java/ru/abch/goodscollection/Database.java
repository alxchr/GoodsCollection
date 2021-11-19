package ru.abch.goodscollection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bosphere.filelogger.FL;

import java.util.ArrayList;
import java.util.Arrays;

public class Database {
    private static final String TAG = "Database";
    private static final String DB_NAME = "goodsdb";
    private static final int DB_VERSION = 25;
    private static final String DB_TABLE_MOVEGOODS = "movegoods";
    private static final String DB_TABLE_BARCODES = "barcodes";
    private static final String DB_TABLE_GOODSMOVEMENT = "goods_movement";
    private static final String DB_TABLE_ADDGOODS = "add_goods";
    private static final String DB_TABLE_SKIPGOODS = "skip_goods";
    private static final String DB_TABLE_CELLS = "cells";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_WAREHOUSE_CODE = "wh_code";
    private static final String COLUMN_LOCK = "lock";
    private static final String COLUMN_PICK = "pick";
    private static final String COLUMN_SENT = "sent";
//    private static final String COLUMN_MOVEGOODS_ID = "movegoods_id";
    private static final String COLUMN_GOODS_ID = "goods_id";
    private static final String COLUMN_INPUT_CELL = "input_cell";
    private static final String COLUMN_OUTPUT_CELL  = "output_cell";
    private static final String COLUMN_QNT  = "qnt";
    private static final String COLUMN_MOVEGOODS_SCAN_TIME = "scan_time";
    private static final String COLUMN_BARCODE  = "barcode";
    private static final String COLUMN_GOODS_DESC = "goods_desc";
    private static final String COLUMN_GOODS_ARTICLE = "goods_article";
    private static final String COLUMN_CELL_ID = "cell_id";
    private static final String COLUMN_CELL_NAME = "cell_name";
    private static final String COLUMN_CELL_DESCR = "cell_descr";
    private static final String COLUMN_CELL_TYPE = "cell_type";
    private static final String COLUMN_CELL_DISTANCE = "cell_distance";
    private static final String COLUMN_CELL_IN = "cell_in";
    private static final String COLUMN_CELL_OUT = "cell_out";
    private static final String COLUMN_MDOC = "mdoc";
    private static final String COLUMN_IDDOCDEF = "iddocdef";
    private static final String COLUMN_STARTTIME = "starttime";
    private static final String COLUMN_CELL_IN_TASK = "cell_in_task";
    private static final String COLUMN_CELL_OUT_TASK = "cell_out_task";
    private static final String COLUMN_CELL_IN_DESCR = "cell_in_descr";
    private static final String COLUMN_CELL_OUT_DESCR = "cell_out_descr";
    private static final String COLUMN_GOODS_BRAND = "goods_brand";
    private static final String COLUMN_GOODS_UNITS = "goods_units";
    private static final String COLUMN_CELL_EMPTYSIZE = "cell_emptysize";
    private static final int FREE = 0, PICKED = 1, DUMPED = 2, DEFICIENCY = 3;
    private static final String COLUMN_ZONEIN = "zonein";
    private static final String COLUMN_ZONEIN_DESCR = "zonein_descr";
    private static final String COLUMN_DEST_ID = "dest_id";
    private static final String COLUMN_DEST_DESCR = "dest_descr";
    private static final String COLUMN_GOODS_URL = "goods_url";
    private static final String DB_CREATE_BARCODES =
                    "create table " + DB_TABLE_BARCODES + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_ID + " text not null, " +
                    COLUMN_BARCODE + " text not null, " +
                    COLUMN_QNT + " integer " +
                    ");";
    private static final String DB_CREATE_CELLS =
            "create table " + DB_TABLE_CELLS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_CELL_ID + " text not null, " +
                    COLUMN_CELL_NAME + " text not null, " +
                    COLUMN_CELL_DESCR + " text not null, " +
                    COLUMN_CELL_TYPE + " integer, " +
                    COLUMN_CELL_DISTANCE + " integer, " +
                    COLUMN_ZONEIN + " text, " +
                    COLUMN_ZONEIN_DESCR + " text," +
                    COLUMN_CELL_EMPTYSIZE + " integer " +
                    ");";
    private static final String DB_CREATE_GOODSMOVEMENT =
            "create table " + DB_TABLE_GOODSMOVEMENT + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_ID + " text not null, " +
                    COLUMN_GOODS_DESC + " text not null, " +
                    COLUMN_CELL_IN + " text, " +
                    COLUMN_CELL_OUT + " text, " +
                    COLUMN_CELL_IN_TASK + " text, " +
                    COLUMN_CELL_OUT_TASK + " text, " +
                    COLUMN_CELL_IN_DESCR + " text, " +
                    COLUMN_CELL_OUT_DESCR + " text, " +
                    COLUMN_MDOC + " text, " +
                    COLUMN_IDDOCDEF + " integer, " +
                    COLUMN_QNT + " integer, " +
                    COLUMN_STARTTIME + " integer, " +
                    COLUMN_GOODS_ARTICLE + " text, " +
                    COLUMN_GOODS_BRAND + " text, " +
                    COLUMN_GOODS_UNITS + " text, " +
                    COLUMN_PICK + " integer, " +
                    COLUMN_SENT + " integer, " +
                    COLUMN_ZONEIN + " text," +
                    COLUMN_ZONEIN_DESCR + " text, " +
                    COLUMN_GOODS_URL + " text, " +
                    COLUMN_LOCK + " integer, " +
                    COLUMN_DEST_ID + " text," +
                    COLUMN_DEST_DESCR + " text " +
                    ");";
    private static final String DB_CREATE_ADDGOODS =
            "create table " + DB_TABLE_ADDGOODS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_ID + " text not null, " +
                    COLUMN_CELL_IN_TASK + " text, " +
                    COLUMN_CELL_OUT_TASK + " text, " +
                    COLUMN_QNT + " integer, " +
                    COLUMN_MDOC + " text not null, " +
                    COLUMN_GOODS_DESC + " text not null, " +
                    COLUMN_GOODS_UNITS + " text, " +
                    COLUMN_GOODS_ARTICLE + " text " +
                    ");";
    private static final String DB_CREATE_SKIPGOODS =
            "create table " + DB_TABLE_SKIPGOODS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_ID + " text not null, " +
                    COLUMN_CELL_IN_TASK + " text, " +
                    COLUMN_CELL_OUT_TASK + " text, " +
                    COLUMN_QNT + " integer, " +
                    COLUMN_MDOC + " text not null, " +
                    COLUMN_GOODS_DESC + " text not null, " +
                    COLUMN_GOODS_UNITS + " text " +
                    ");";
    private final Context mCtx;
    private DBHelper mDBHelper;
    private static SQLiteDatabase mDB;
    Database(Context ctx) {
        mCtx = ctx;
    }

    void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        try {
            mDB = mDBHelper.getWritableDatabase();
//            connectionClass = new ConnectionClass();
        } catch (SQLException s) {
            new Exception("Error with DB Open");
        }
    }
    void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_CELLS);
            db.execSQL(DB_CREATE_BARCODES);
            db.execSQL(DB_CREATE_GOODSMOVEMENT);
            db.execSQL(DB_CREATE_ADDGOODS);
            db.execSQL(DB_CREATE_SKIPGOODS);
            Log.d(TAG, "onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrade DB from " + oldVersion + " to " + newVersion);
            String dropGoods = "drop table if exists " + DB_TABLE_GOODSMOVEMENT;
            String dropBarcodes = "drop table if exists " + DB_TABLE_BARCODES;
            String dropCells = "drop table if exists " + DB_TABLE_CELLS;
            String dropAddGoods = "drop table if exists " + DB_TABLE_ADDGOODS;
            String dropSkipGoods = "drop table if exists " + DB_TABLE_SKIPGOODS;
            if (newVersion > 1) {
                db.execSQL(dropGoods);
                db.execSQL(dropBarcodes);
                db.execSQL(dropCells);
                db.execSQL(dropAddGoods);
                db.execSQL(dropSkipGoods);
                db.execSQL(DB_CREATE_BARCODES);
                db.execSQL(DB_CREATE_GOODSMOVEMENT);
                db.execSQL(DB_CREATE_CELLS);
                db.execSQL(DB_CREATE_ADDGOODS);
                db.execSQL(DB_CREATE_SKIPGOODS);
            }
        }
    }
    public static void clearGoodsMovements() {
        int rows = mDB.delete(DB_TABLE_GOODSMOVEMENT, COLUMN_LOCK + "=0", null);
        FL.d(TAG,"clearGoodsMovements() deleted " + rows + " rows");
    }
    public static void purgeGoodsMovements() {
        int rows = mDB.delete(DB_TABLE_GOODSMOVEMENT, null, null);
        FL.d(TAG,"clearGoodsMovements() deleted " + rows + " rows");
    }
    public static void unlockGoodsMovements() {
        String table = DB_TABLE_GOODSMOVEMENT;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LOCK, 0);
        mDB.update(table, cv, null, null);
        FL.d(TAG, "Unlock all");
    }
    /*
    public static void clearPicked() {
        int rows = mDB.delete(DB_TABLE_GOODSMOVEMENT, COLUMN_PICK + " = 1 ", null);
        FL.d(TAG,"clearPicked() deleted " + rows + " rows");
    }

     */
    public static void clearPicked() {
        String table = DB_TABLE_ADDGOODS;
        int rows = mDB.delete(table, null, null);
        FL.d(TAG,"clearPicked() deleted " + rows + " rows");
    }
    public static void clearSkipped() {
        String table = DB_TABLE_SKIPGOODS;
        int rows = mDB.delete(table, null, null);
        FL.d(TAG,"clearSkipped() deleted " + rows + " rows");
    }
    static void clearData() {
        mDB.delete(DB_TABLE_BARCODES, null, null);
        mDB.delete(DB_TABLE_GOODSMOVEMENT, null, null);
//        mDB.delete(DB_TABLE_CELLS, null, null);
        FL.d(TAG,"Clear tables");
    }
    static void clearCells() {
        mDB.delete(DB_TABLE_CELLS, null, null);
        FL.d(TAG,"Clear table cells");
    }
    public static long addBarCode(String goodsCode, String barCode, int qnt) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOODS_ID, goodsCode);
        cv.put(COLUMN_BARCODE, barCode);
        cv.put(COLUMN_QNT, qnt);
        try {
            ret = mDB.insert(DB_TABLE_BARCODES, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }

    static void beginTr() {
        mDB.beginTransaction();
    }
    static void endTr() {
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    public static long addCell(String cellId, String name, String descr, int type, int distance, String zonein, String zonein_descr, int emptySize) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CELL_ID, cellId);
        cv.put(COLUMN_CELL_NAME, name);
        cv.put(COLUMN_CELL_TYPE, type);
        cv.put(COLUMN_CELL_DESCR, descr);
        cv.put(COLUMN_CELL_DISTANCE, distance);
        cv.put(COLUMN_ZONEIN, zonein);
        cv.put(COLUMN_ZONEIN_DESCR, zonein_descr);
        cv.put(COLUMN_CELL_EMPTYSIZE, emptySize);
        try {
            ret = mDB.insert(DB_TABLE_CELLS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }
    public static int countPicked(String goods, String mdoc,String cellOut_task, String cellIn_task) {
        int ret = 0;
        String table = DB_TABLE_ADDGOODS, selectedValue = "SUM(" + COLUMN_QNT + ")";
        try {
            Cursor c = mDB.query(table, new String[] {selectedValue}, COLUMN_QNT + " >0 and " + COLUMN_CELL_IN_TASK + " =? and " +
                            COLUMN_CELL_OUT_TASK + " =? and " + COLUMN_MDOC + " =? and " + COLUMN_GOODS_ID + " = ?", new String[] {cellIn_task, cellOut_task, mdoc, goods},
                    null, null, null);
            if(c.getCount() > 0) {
                c.moveToNext();
                ret = c.getInt(0);
            }
            c.close();
        } catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }
    public static int setLock(String goods, String mdoc,String cellOut_task, String cellIn_task, int lock) {
        String table = DB_TABLE_GOODSMOVEMENT;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LOCK, lock);
        int rows = mDB.update(table, cv, COLUMN_CELL_IN_TASK + " =? and " +
                        COLUMN_CELL_OUT_TASK + " =? and " + COLUMN_MDOC + " =? and " + COLUMN_GOODS_ID + " = ?",
                new String[]{cellIn_task, cellOut_task, mdoc, goods});
        FL.d(TAG, "Lock row for " + rows + " rows lock " + lock);
        if (rows > 1) {
            Cursor c = mDB.query(table, null, COLUMN_CELL_IN_TASK + " =? and " +
                            COLUMN_CELL_OUT_TASK + " =? and " + COLUMN_MDOC + " =? and " + COLUMN_GOODS_ID + " = ?",
                    new String[]{cellIn_task, cellOut_task, mdoc, goods}, null, null, null);
            c.moveToNext(); //skip 1st
            mDB.beginTransaction();
            while (c.moveToNext()) {
                long row = c.getLong(0);
                mDB.delete(table, COLUMN_ID + "=?", new String[]{String.valueOf(row)});
                FL.e(TAG, "Delete duplicate row " + row);
            }
            mDB.endTransaction();
            c.close();
        }
        return rows;
    }
    public static int checkLock(String goods, String mdoc,String cellOut_task, String cellIn_task) {
        String table = DB_TABLE_GOODSMOVEMENT;
        int ret = 0;
        Cursor c = mDB.query(table, new String[] {COLUMN_LOCK}, COLUMN_CELL_IN_TASK + " =? and " +
                        COLUMN_CELL_OUT_TASK + " =? and " + COLUMN_MDOC + " =? and " + COLUMN_GOODS_ID + " = ?",
                new String[] {cellIn_task, cellOut_task, mdoc, goods}, null,null, null);
        if(c.moveToNext()) ret = c.getInt(0);
        c.close();
        if(ret > 0) {
            FL.d(TAG,"Locked goods " + goods + " mdoc " + mdoc + " cell out " + cellOut_task + " cell in " + cellIn_task);
        }
        return ret;
    }

    public static long addGoodsMovement(String goods, String goods_descr, String cellOut, String cellOut_descr, String cellIn, String cellIn_descr,
                                        String cellOut_task, String cellIn_task, String mdoc, int iddocdef, int qnt, long startTime, String goods_article,
                                        String goods_brand, String units, String zonein, String zonein_descr, String url,
                                        String dest_id, String dest_descr) {
        long ret = 0;
        String table = DB_TABLE_GOODSMOVEMENT;
        try {
            /*
            Cursor c = mDB.query(DB_TABLE_ADDGOODS, new String[] {COLUMN_QNT}, COLUMN_QNT + " >0 and " + COLUMN_CELL_IN_TASK + " =? and " +
                    COLUMN_CELL_OUT_TASK + " =? and " + COLUMN_MDOC + " =? and " + COLUMN_GOODS_ID + " = ?", new String[] {cellIn_task, cellOut_task, mdoc, goods},
                    null, null, null);

             */
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_GOODS_ID, goods);
            cv.put(COLUMN_GOODS_DESC, goods_descr);
            cv.put(COLUMN_CELL_IN, cellIn);
            cv.put(COLUMN_CELL_OUT, cellOut);
            cv.put(COLUMN_CELL_IN_TASK, cellIn_task);
            cv.put(COLUMN_CELL_OUT_TASK, cellOut_task);
            cv.put(COLUMN_CELL_IN_DESCR, cellIn_descr);
            cv.put(COLUMN_CELL_OUT_DESCR, cellOut_descr);
            cv.put(COLUMN_MDOC, mdoc);
            cv.put(COLUMN_IDDOCDEF, iddocdef);
            cv.put(COLUMN_STARTTIME, startTime);
            cv.put(COLUMN_PICK, FREE);
            cv.put(COLUMN_SENT, 0);
            cv.put(COLUMN_GOODS_ARTICLE, goods_article);
            cv.put(COLUMN_GOODS_BRAND, goods_brand);
            cv.put(COLUMN_GOODS_UNITS, units);
            cv.put(COLUMN_ZONEIN, zonein);
            cv.put(COLUMN_ZONEIN_DESCR, zonein_descr);
            cv.put(COLUMN_GOODS_URL, url);
            cv.put(COLUMN_LOCK, 0);
            cv.put(COLUMN_DEST_ID, dest_id);
            cv.put(COLUMN_DEST_DESCR, dest_descr);
            /*
            if(c.getCount() > 0) {
                c.moveToNext();
                int picked = c.getInt(0);
                FL.d(TAG, "Update goods " + goods_article + " id " + goods + " mdoc " + mdoc +
                        " cell out " + cellOut_descr + " cell in " + cellIn_descr + " qnt " + qnt);
                if (qnt > picked) {
                    cv.put(COLUMN_QNT, qnt - picked);
                    ret = mDB.insert(table, null, cv);
                }
            } else {

             */
                cv.put(COLUMN_QNT, qnt);
                ret = mDB.insert(table, null, cv);
//            }
//            c.close();
        } catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }

    public static int updateGoodsQuantity(String goods,String cellOut_task, String mdoc, int qnt) {
        int ret = 0;
        int newQnt;
        String table = DB_TABLE_GOODSMOVEMENT;
        try {
            Cursor c = mDB.query(table, new String[] {COLUMN_QNT},COLUMN_GOODS_ID + " =? and " + COLUMN_MDOC + " =? and " +
                    COLUMN_CELL_OUT_TASK + " =?", new String[] {goods, mdoc, cellOut_task},null,null,null);
            if(c.getCount() > 0) {
                c.moveToNext();
                newQnt = c.getInt(0) - qnt;
                if (newQnt < 0) newQnt = 0;
                ContentValues cvUpdate = new ContentValues();
                cvUpdate.put(COLUMN_QNT, newQnt);
                ret = mDB.update(table, cvUpdate, COLUMN_GOODS_ID + " =? and " + COLUMN_MDOC + " =? and " +
                        COLUMN_CELL_OUT_TASK + " =?", new String[] {goods, mdoc, cellOut_task});
                FL.d(TAG, "Update qnt rows " + ret + " " + goods + " " + newQnt
                        + " mdoc " + mdoc + " cellOutTask " + cellOut_task);
            }
            c.close();
        } catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }

    public static long getUrgency(String  zonein) {
        long ret = 0;
        String table = DB_TABLE_GOODSMOVEMENT;
        Cursor c = mDB.query(table, new String[] {COLUMN_STARTTIME}, COLUMN_ZONEIN + "=? and "  + COLUMN_PICK + " =? ",
                new String[] {zonein,String.valueOf(FREE)},null,null, COLUMN_STARTTIME,"1");
        if(c.getCount() > 0) {
            c.moveToNext();
            ret = c.getLong(0);
            Log.d(TAG, "Found urgency " + c.getCount() + " time " + ret + " zonein " + zonein);
        }
        c.close();
        return ret;
    }

    public static int countPositions(String zonein) {
        String table = DB_TABLE_GOODSMOVEMENT;
        Cursor c =
//                (MainActivity.clientId == null)?
                        mDB.query(table, null, COLUMN_ZONEIN + " =? and "  + COLUMN_PICK + " =0 ",
                new String[] {zonein},null,null,null,null)
//                        :
//                mDB.query(table, null, COLUMN_ZONEIN + " =? and "  + COLUMN_PICK + " =0 and " + COLUMN_DEST_ID + " =?",
//                        new String[] {zonein, MainActivity.clientId},null,null,null,null)
                ;
        int ret = c.getCount();
        c.close();
        return ret;
    }

    public static int countPositions(String zonein, String clientId) {
        String table = DB_TABLE_GOODSMOVEMENT;
        Cursor c = mDB.query(table, null, COLUMN_ZONEIN + " =? and "  +  COLUMN_PICK + " =0 and " + COLUMN_DEST_ID + " =?",
                        new String[] {zonein, clientId},null,null,null,null);
        int ret = c.getCount();
        c.close();
        return ret;
    }

    public static int countPositions(String zonein, long after) {
        String table = DB_TABLE_GOODSMOVEMENT;
        Cursor c =
//                (MainActivity.clientId == null)?
                        mDB.query(table, null, COLUMN_ZONEIN + " =? and " + COLUMN_STARTTIME + " >? and "  + COLUMN_PICK + " =? ",
                new String[] {zonein,String.valueOf(after),String.valueOf(FREE)},null,null,null,null)
//                        : mDB.query(table, null,
//                COLUMN_ZONEIN + " =? and " + COLUMN_STARTTIME + " >? and "  + COLUMN_PICK + " =0 and " + COLUMN_DEST_ID + " =? ",
//                new String[] {zonein,String.valueOf(after),MainActivity.clientId},
//                null,null,null,null)
                ;
        int ret = c.getCount();
        c.close();
        return ret;
    }

    public static int countPositions(String zonein, long after, long before) {
        String table = DB_TABLE_GOODSMOVEMENT;
        Cursor c =
//                (MainActivity.clientId == null)?
                        mDB.query(table, null, COLUMN_ZONEIN + " =? and " + COLUMN_STARTTIME + " >? and " + COLUMN_STARTTIME + " <=? and " +
                        COLUMN_PICK + " =0", new String[] {zonein, String.valueOf(after), String.valueOf(before)},
                null,null,null,null)
//                        :
//                mDB.query(table, null,
//                        COLUMN_ZONEIN + " =? and " + COLUMN_STARTTIME + " >? and " + COLUMN_STARTTIME + " <=? and " +
//                                COLUMN_PICK + " = 0 and " + COLUMN_DEST_ID + " =? ",
//                        new String[] {zonein, String.valueOf(after), String.valueOf(before), MainActivity.clientId},
//                        null,null,null,null)
                ;
        int ret = c.getCount();
        c.close();
        return ret;
    }

    public static GoodsMovement[] selectGoods(String zonein, long after, long before) {
        GoodsMovement[] ret = null;
        String table = DB_TABLE_GOODSMOVEMENT;
        Cursor c =
//                (MainActivity.clientId == null)?
                        mDB.query(table, null, COLUMN_ZONEIN + " =? and " + COLUMN_STARTTIME + " >? and " + COLUMN_STARTTIME + " <=? and " +
                        COLUMN_PICK + " =0", new String[]{zonein, String.valueOf(after), String.valueOf(before)},
                null, null, null, null)
//                        :
//                mDB.query(table, null, COLUMN_ZONEIN + " =? and " + COLUMN_STARTTIME + " >? and " + COLUMN_STARTTIME + " <=? and " +
//                                COLUMN_PICK + " =0 and " + COLUMN_DEST_ID + " =? ",
//                        new String[]{zonein, String.valueOf(after), String.valueOf(before), MainActivity.clientId},
//                        null, null, null, null)
                ;
        if(c.getCount() > 0) {
            ret = new GoodsMovement[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                GoodsMovement gm = new GoodsMovement(
                        c.getString(1),
                        c.getString(2),
                        c.getString(4),
                        c.getString(8),
                        c.getString(3),
                        c.getString(7),
                        c.getString(6),
                        c.getString(5),
                        c.getString(9),
                        c.getInt(10),
                        c.getInt(11),
                        c.getLong(12),
                        c.getString(13),
                        c.getString(14),
                        c.getString(15),
                        c.getString(18),
                        c.getString(19),
                        c.getString(22),
                        c.getString(23)
                );
//                Log.d(TAG, "Select goods " + c.getString(2) + " " + c.getInt(11));
                gm.rowId = c.getLong(0);
                gm.url = c.getString(20);
                ret[i++] = gm;
            }
        }
        c.close();
        return ret;
    }
    public static GoodsMovement[] selectGoods(String zonein, long after) {
        GoodsMovement[] ret = null;
        String table = DB_TABLE_GOODSMOVEMENT;
        Cursor c =
//                (MainActivity.clientId == null)?
                        mDB.query(table, null, COLUMN_ZONEIN + " =? and " + COLUMN_STARTTIME + " >? and " + COLUMN_PICK + " =0",
                new String[]{zonein, String.valueOf(after)},
                null, null, null, null)
//                        :
//                mDB.query(table, null, COLUMN_ZONEIN + " =? and " + COLUMN_STARTTIME + " >? and " +
//                                COLUMN_PICK + " =0 and "  + COLUMN_DEST_ID + " =? ",
//                        new String[]{zonein, String.valueOf(after), MainActivity.clientId},
//                        null, null, null, null)
                ;
        if(c.getCount() > 0) {
            ret = new GoodsMovement[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                GoodsMovement gm = new GoodsMovement(
                        c.getString(1),
                        c.getString(2),
                        c.getString(4),
                        c.getString(8),
                        c.getString(3),
                        c.getString(7),
                        c.getString(6),
                        c.getString(5),
                        c.getString(9),
                        c.getInt(10),
                        c.getInt(11),
                        c.getLong(12),
                        c.getString(13),
                        c.getString(14),
                        c.getString(15),
                        c.getString(18),
                        c.getString(19),
                        c.getString(22),
                        c.getString(23)
                );
                gm.rowId = c.getLong(0);
                gm.url = c.getString(20);
                ret[i++] = gm;
            }
        }
        c.close();
        return ret;
    }
    public static GoodsMovement[] selectGoods(String zonein, String clientId) {
        GoodsMovement[] ret = null;
        String table = DB_TABLE_GOODSMOVEMENT;
        Cursor c =
                mDB.query(table, null, COLUMN_ZONEIN + " =? and " + COLUMN_PICK + " =0 and "  + COLUMN_DEST_ID + " =? ",
                        new String[]{zonein, clientId},
                        null, null, null, null)
                ;
        if(c.getCount() > 0) {
            ret = new GoodsMovement[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                GoodsMovement gm = new GoodsMovement(
                        c.getString(1),
                        c.getString(2),
                        c.getString(4),
                        c.getString(8),
                        c.getString(3),
                        c.getString(7),
                        c.getString(6),
                        c.getString(5),
                        c.getString(9),
                        c.getInt(10),
                        c.getInt(11),
                        c.getLong(12),
                        c.getString(13),
                        c.getString(14),
                        c.getString(15),
                        c.getString(18),
                        c.getString(19),
                        c.getString(22),
                        c.getString(23)
                );
                gm.rowId = c.getLong(0);
                gm.url = c.getString(20);
                ret[i++] = gm;
            }
        }
        c.close();
        return ret;
    }
    public static Cell[] getCells() {
        Cell[] ret = null;
        String table = DB_TABLE_CELLS;
        Cursor c = mDB.query(table, null, null, null, null, null, null);
        if (c.getCount() > 0) {
            ret = new Cell[c.getCount()];
            int i = 0;
            while (c.moveToNext())
                ret[i++] = new Cell(
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getInt(4),
                        c.getInt(5),
                        c.getString(6),
                        c.getString(7),
                        c.getInt(8)
                );
        }
        c.close();
        return ret;
    }
    public static Cell getCellByName(String name) {
        Cell ret = null;
        String table = DB_TABLE_CELLS;
        Cursor c = mDB.query(table, null, COLUMN_CELL_NAME + " like '" + name + "%'", null, null, null, null);
        if (c.moveToNext())
            ret = new Cell(
                c.getString(1),
                c.getString(2),
                c.getString(3),
                c.getInt(4),
                c.getInt(5),
                c.getString(6),
                c.getString(7),
                c.getInt(8)
        );
        c.close();
        return ret;
    }
    public static Cell getCellById(String id) {
        Cell ret = null;
        String table = DB_TABLE_CELLS;
        Cursor c = mDB.query(table, null, COLUMN_CELL_ID + " =? ", new String[] {id}, null, null, null);
        if (c.moveToNext())
            ret = new Cell(
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getInt(4),
                    c.getInt(5),
                    c.getString(6),
                    c.getString(7),
                    c.getInt(8)
            );
        c.close();
        return ret;
    }
    public static BarCode getBarCode(String code) {
        BarCode ret = null;
        int qnt;
        String barcodeTable = DB_TABLE_BARCODES, goodsCode;
        Cursor c = mDB.query(barcodeTable, null,COLUMN_BARCODE + " =? ", new String[]{code},
                null, null, null, null );
        if (c.moveToFirst()) {
            goodsCode = c.getString(1);
            qnt = c.getInt(3);
            Log.d(TAG, "Found goods barcode = " + goodsCode + " qnt = " + qnt);
            ret = new BarCode(goodsCode, code, qnt);
        }
        c.close();
        return ret;
    }
/*
    public static void setPick(long rowId, int qnt) {
        String table = DB_TABLE_GOODSMOVEMENT;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PICK, PICKED);
        cv.put(COLUMN_QNT, qnt);
        int rows = mDB.update(table, cv, COLUMN_ID + " =? ", new String[] {String.valueOf(rowId)});
        FL.d(TAG,"Pick set for " + rows + " rows row id " + rowId + " qnt " + qnt);
    }

 */
    public static void setPick(String goods, String mdoc, String cellOut, String cellIn, int qnt) {
        String table = DB_TABLE_GOODSMOVEMENT;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PICK, PICKED);
        cv.put(COLUMN_QNT, qnt);
        int rows = mDB.update(table, cv, COLUMN_GOODS_ID + " =? and " + COLUMN_MDOC + " =? and " +
                COLUMN_CELL_OUT_TASK + " =? and " + COLUMN_CELL_IN_TASK + " =?", new String[] {goods, mdoc, cellOut, cellIn});
        FL.d(TAG,"Pick set for " + rows + " rows goods " +
                goods + " mdoc " + mdoc + " cellOut " + cellOut + " cellIn " + cellIn +" qnt " + qnt);
    }
    /*
    public static int lockRow(long rowId, int lock) {
        String table = DB_TABLE_GOODSMOVEMENT;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LOCK, lock);
        int rows = mDB.update(table, cv, COLUMN_ID + " =? ", new String[] {String.valueOf(rowId)});
        FL.d(TAG,"Lock row for " + rows + " rows row id " + rowId + " lock " + lock);
        return rows;
    }

     */

    public static GoodsMovement[] getGoodsById(String goodsId) {
        GoodsMovement[] ret = null;
        String table = DB_TABLE_GOODSMOVEMENT;
        Cursor c = mDB.query(table, null,COLUMN_GOODS_ID + " =? ", new String[]{goodsId},
                null, null, null, null );
        if(c.getCount() > 0) {
            ret = new GoodsMovement[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                GoodsMovement gm = new GoodsMovement(c.getString(1),
                        c.getString(2),
                        c.getString(4),
                        c.getString(8),
                        c.getString(3),
                        c.getString(7),
                        c.getString(6),
                        c.getString(5),
                        c.getString(9),
                        c.getInt(10),
                        c.getInt(11),
                        c.getLong(12),
                        c.getString(13),
                        c.getString(14),
                        c.getString(15),
                        c.getString(18),
                        c.getString(19),
                        c.getString(22),
                        c.getString(23)
                );
                gm.rowId = c.getLong(0);
                gm.url = c.getString(20);
                ret[i++] = gm;
            }
        }
        c.close();
        return ret;
    }
    /*
    public static String[] findBarCodes(String goodsCode) {
        String table = DB_TABLE_BARCODES;
        String[] ret = null;
        Cursor c = mDB.query(table, null, COLUMN_GOODS_ID + " =? ", new String[] {goodsCode}, null, null, null);
        if (c.getCount() > 0) {
            Log.d(TAG, "Goods =" + goodsCode + " count " + c.getCount());
            ret = new String[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                String barCode = c.getString(2);
                ret[i++] = barCode;
            }
        }
        c.close();
        return ret;
    }

     */
    public static ArrayList<Zone> getZones(int type) {
        ArrayList<Zone> ret = new ArrayList<>();
        Zone z;
        int count;
        Cursor c = mDB.query(true, DB_TABLE_CELLS, new String[] {COLUMN_ZONEIN, COLUMN_ZONEIN_DESCR}, "length(" + COLUMN_ZONEIN + ") >0 and " + COLUMN_CELL_TYPE + " =? ",
                new String[] {String.valueOf(type)}, null, null, null, null);
        count = c.getCount();
        if(count > 0) {
//            Log.d(TAG, "getZones count " + count);
            while (c.moveToNext()) {
//                Log.d(TAG, "Dump zone #" + i + " " + c.getString(c.getColumnIndex(COLUMN_ZONEIN)) + " " + c.getString(c.getColumnIndex(COLUMN_ZONEIN_DESCR)));
                z = new Zone(c.getString(c.getColumnIndex(COLUMN_ZONEIN)), c.getString(c.getColumnIndex(COLUMN_ZONEIN_DESCR)));
                ret.add(z);
            }
        }
        c.close();
        return ret;
    }

    /*
    public static ArrayList<GoodsMovement> pickedGoods() {
        ArrayList<GoodsMovement> ret = new ArrayList<>();
        String table = DB_TABLE_GOODSMOVEMENT;
        Cursor c = mDB.query(table, null, COLUMN_PICK + " =? ",
                new String[]{String.valueOf(PICKED)},
                null, null, null, null);
        if(c.getCount() > 0) {
            while (c.moveToNext()) {
                GoodsMovement gm = new GoodsMovement(
                        c.getString(1),
                        c.getString(2),
                        c.getString(4),
                        c.getString(8),
                        c.getString(3),
                        c.getString(7),
                        c.getString(6),
                        c.getString(5),
                        c.getString(9),
                        c.getInt(10),
                        c.getInt(11),
                        c.getLong(12),
                        c.getString(13),
                        c.getString(14),
                        c.getString(15),
                        c.getString(18),
                        c.getString(19)
                );
                gm.rowId = c.getLong(0);
                ret.add(gm);
            }
        }
        c.close();
        return ret;
    }

     */
    public static ArrayList<GoodsMovement> pickedGoods() {
        ArrayList<GoodsMovement> ret = new ArrayList<>();
        Cursor c = mDB.query(DB_TABLE_ADDGOODS, null, null,null, null, null, null, null);
        if(c.getCount() > 0) {
            while (c.moveToNext()) {
                GoodsMovement gm = new GoodsMovement(
                        c.getString(1),
                        c.getString(6),
                        c.getString(3),
                        "",
                        c.getString(2),
                        "",
                        c.getString(3),
                        c.getString(2),
                        c.getString(5),
                        0,
                        c.getInt(4),
                        0,
                        c.getString(8),
                        "",
                        c.getString(7),
                        "",
                        "",
                        "",
                        ""
                );
                gm.rowId = c.getLong(0);
                ret.add(gm);
            }
        }
        c.close();
        return ret;
    }
    public static int getSkipped(String goods, String mdoc, String cellOut, String cellIn){
        int ret = 0;
        String table = DB_TABLE_SKIPGOODS;
        Cursor c = mDB.query(table, new String[] {COLUMN_QNT}, COLUMN_GOODS_ID + " =? and " + COLUMN_MDOC + " =? and " +
                COLUMN_CELL_OUT_TASK + " =? and " + COLUMN_CELL_IN_TASK + " =?",
                new String[]{goods,mdoc,cellOut,cellIn},null,null,null);
        if(c.moveToNext()) {
            ret = c.getInt(0);
        }
        c.close();
        return ret;

    }
    public static ArrayList<GoodsMovement> skippedGoods() {
        ArrayList<GoodsMovement> ret = new ArrayList<>();
        String table = DB_TABLE_SKIPGOODS;
        Cursor c = mDB.query(table, null, null,null, null, null, null, null);
        if(c.getCount() > 0) {
            while (c.moveToNext()) {
                GoodsMovement gm = new GoodsMovement(
                        c.getString(1),
                        c.getString(6),
                        c.getString(3),
                        "",
                        c.getString(2),
                        "",
                        c.getString(3),
                        c.getString(2),
                        c.getString(5),
                        0,
                        c.getInt(4),
                        0,
                        "",
                        "",
                        c.getString(7),
                        "",
                        "",
                        "",
                        ""
                );
                gm.rowId = c.getLong(0);
                ret.add(gm);
            }
        }
        c.close();
        return ret;
    }
/*
    public static String getZone(String cell) {
        String ret = null;
        Cursor c = mDB.query(DB_TABLE_CELLS, new String[] {COLUMN_ZONEIN},COLUMN_CELL_ID + " = '" + cell + "'",
                null,null,null,null);
        if(c.moveToNext()) {
            ret = c.getString(0);
        }
        c.close();
        return ret;
    }

 */
    public static GoodsMovement[] deficiencyToUpload() {
        GoodsMovement[] ret = null;
        int qnt, rows, count;
        String goodsCode, mdoc, cellOut, cellOutTask, cellIn, cellInTask;
        final String table = DB_TABLE_ADDGOODS;
//        ContentValues cv = new ContentValues();
//        cv.put(COLUMN_PICK, DUMPED);
//        rows = mDB.update(table, cv, COLUMN_QNT + " <0", null);
//        Log.d(TAG,"Dump set for " + rows + " rows");
        Cursor c = mDB.query(table, null, COLUMN_QNT + "<0", null, null, null, null);
        count = c.getCount();
        Log.d(TAG, "Found " + count + " rows for upload");
        if (count > 0) {
            ret = new GoodsMovement[count];
            c.moveToFirst();
            for (int i = 0; i < count; i++) {
                goodsCode = c.getString(1);
                cellIn = c.getString(2);
                cellOut = c.getString(3);
                cellInTask = c.getString(2);
                cellOutTask = c.getString(3);
                mdoc = c.getString(5);
                qnt = c.getInt(4);
                GoodsMovement gm = new GoodsMovement(goodsCode, "", cellOut, "", cellIn, "", cellOutTask, cellInTask, mdoc, 0, qnt, 0,
                        "", "", "", "", "","","");
                gm.rowId = c.getLong(0);
                gm.dctNum = App.deviceUniqueIdentifier;
                ret[i] = gm;
                c.moveToNext();
            }
        }
        c.close();
        return ret;
    }
    /*
    public static void clearDeficiency() {
        String table = DB_TABLE_GOODSMOVEMENT;
        int rows = mDB.delete(table, COLUMN_QNT + " <0 ", null);
        Log.d(TAG, "Deleted " + rows + " deficiency rows");
    }

     */

    public static void addPickedGoods(String goods, String mdoc, String cellOutTask, String cellInTask, int qnt, String goods_desc,
                                      String units, String article) {
        String table = DB_TABLE_ADDGOODS;
        beginTr();
        if (qnt < 0) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_GOODS_ID, goods);
            cv.put(COLUMN_MDOC, mdoc);
            cv.put(COLUMN_CELL_OUT_TASK, cellOutTask);
            cv.put(COLUMN_CELL_IN_TASK, cellInTask);
            cv.put(COLUMN_QNT, qnt);
            cv.put(COLUMN_GOODS_DESC, goods_desc);
            cv.put(COLUMN_GOODS_UNITS, units);
            cv.put(COLUMN_GOODS_ARTICLE, article);
            mDB.insert(table, null, cv);
        } else {
            /*
            ContentValues updateCv = new ContentValues();
            updateCv.put(COLUMN_QNT, qnt);
            int rows = mDB.update(table, updateCv, COLUMN_GOODS_ID + " =? and " + COLUMN_MDOC + " =? and " +
                            COLUMN_CELL_OUT_TASK + " =? and " + COLUMN_CELL_IN_TASK + " =? and " + COLUMN_QNT + " >0",
                    new String[]{goods, mdoc, cellOutTask, cellInTask});
            if (rows == 0) {

             */
                ContentValues cv = new ContentValues();
            cv.put(COLUMN_GOODS_ID, goods);
            cv.put(COLUMN_MDOC, mdoc);
            cv.put(COLUMN_CELL_OUT_TASK, cellOutTask);
            cv.put(COLUMN_CELL_IN_TASK, cellInTask);
            cv.put(COLUMN_QNT, qnt);
            cv.put(COLUMN_GOODS_DESC, goods_desc);
            cv.put(COLUMN_GOODS_UNITS, units);
            cv.put(COLUMN_GOODS_ARTICLE, article);
            mDB.insert(table, null, cv);
//            }
        }
        endTr();
    }
    public static void addSkippedGoods(String goods, String mdoc, String cellOutTask, String cellInTask, int qnt, String goods_desc, String units) {
        String table = DB_TABLE_SKIPGOODS;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOODS_ID, goods);
        cv.put(COLUMN_MDOC, mdoc);
        cv.put(COLUMN_CELL_OUT_TASK, cellOutTask);
        cv.put(COLUMN_CELL_IN_TASK, cellInTask);
        cv.put(COLUMN_QNT, qnt);
        cv.put(COLUMN_GOODS_DESC, goods_desc);
        cv.put(COLUMN_GOODS_UNITS, units);
        mDB.insert(table, null, cv);
    }
    public static int deletePickedGoods(long rowId) {
        String table = DB_TABLE_ADDGOODS;
        return mDB.delete(table, COLUMN_ID + " =?", new String[] {String.valueOf(rowId)});
    }
    public static GoodsMovement[] goodsToDump(String cellId) {
        String table = DB_TABLE_ADDGOODS;
        GoodsMovement[] ret = null;
        Cursor c = mDB.query(table, null, null,null,null, null, null, null);
        if(c.getCount() > 0) {
            ret = new GoodsMovement[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                GoodsMovement gm = new GoodsMovement(
                        c.getString(1),
                        c.getString(6),
                        c.getString(3),
                        "",
                        cellId,
                        "",
                        c.getString(3),
                        c.getString(2),
                        c.getString(5),
                        0,
                        c.getInt(4),
                        0,
                        "",
                        "",
                        c.getString(7),
                        "",
                        "",
                        "",
                        ""
                );
                gm.rowId = c.getLong(0);
                gm.dctNum = App.deviceUniqueIdentifier;
                ret[i++] = gm;
            }
        }
        c.close();
        return ret;
    }
    public static PrintLabelRequest smallPieceGoods() {
        PrintLabelRequest ret = null;
        GoodsLabel[] labels;
        Cursor c;
        String tableGM = DB_TABLE_GOODSMOVEMENT, tablePicked = DB_TABLE_ADDGOODS, tableCells = DB_TABLE_CELLS;
        String sqlQuery = "select " + "GM." + COLUMN_GOODS_ID + ",GM." + COLUMN_GOODS_DESC
                + ",GM." + COLUMN_GOODS_ARTICLE + ",GM." + COLUMN_QNT + ",GM." + COLUMN_GOODS_UNITS + ",C." + COLUMN_CELL_DESCR
                + " from " + tableGM + " as GM"
                + " left join " + tableCells + " as C "
                + "on GM." + COLUMN_CELL_OUT_TASK + " = C." + COLUMN_CELL_ID
                + " left join " + tablePicked + " as P "
                + "on GM." + COLUMN_GOODS_ID + " = P." + COLUMN_GOODS_ID
                + " where C." + COLUMN_CELL_DISTANCE + " >= 7440 and C." + COLUMN_CELL_DISTANCE + " <= 8500 and P." + COLUMN_CELL_OUT_TASK + " is NULL LIMIT 10";
        /*
        Cursor c = mDB.query(table, new String[] {COLUMN_GOODS_ID,COLUMN_GOODS_DESC,COLUMN_GOODS_ARTICLE,COLUMN_QNT,COLUMN_GOODS_UNITS},
//                COLUMN_CELL_DISTANCE + " >= 7500 and " + COLUMN_CELL_DISTANCE + " <= 8500",
                null,
                null, null, null, null," 10");

         */
        c = mDB.rawQuery(sqlQuery, null);
        if (c.getCount() > 0) {
            int n = c.getCount();
            Log.d(TAG, "Found " + n +" small-piece goods");
            labels = new GoodsLabel[n];
            for (int i = 0; i < n; i++) {
                c.moveToNext();
                GoodsLabel gl = new GoodsLabel(
                        c.getString(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(4),
                        c.getInt(3),
                        App.getStoreMan(),
                        c.getString(5)
                );
                labels[i] = gl;
            }
            ret = new PrintLabelRequest(n,labels);
        }
        c.close();
        return ret;
    }
    public static Client[] getClientArray() {
        Client[] ret = null;
        String workZonesArg = "('";
        if (App.getWorkZones() != null)
        for (int i = 0; i < App.getWorkZones().size(); i++) {
            workZonesArg += App.getWorkZones().get(i).zonein;
            if (i == App.getWorkZones().size() - 1) {
                workZonesArg += "')";
            } else {
                workZonesArg += "','";
            }
        }
        String whereArg = " where GM." + COLUMN_PICK + " =0 and C." + COLUMN_ZONEIN + " in " + workZonesArg;
        Log.d(TAG,"whereArg = " + whereArg);
        String tableGM = DB_TABLE_GOODSMOVEMENT, tableCells = DB_TABLE_CELLS;
        String sqlQuery = "select distinct " + "GM." + COLUMN_DEST_ID + ",GM." + COLUMN_DEST_DESCR
//             +  ", GM." + COLUMN_CELL_OUT_TASK +
//                ", C." + COLUMN_ZONEIN
//                + ",GM." + COLUMN_GOODS_ARTICLE + ",GM." + COLUMN_QNT + ",GM." + COLUMN_GOODS_UNITS + ",C." + COLUMN_CELL_DESCR
                + " from " + tableGM + " as GM"
                + " left join " + tableCells + " as C "
                + "on GM." + COLUMN_CELL_OUT_TASK + " = C." + COLUMN_CELL_ID;
        if (App.getWorkZones() != null) sqlQuery += whereArg;
        Cursor c;
        /*
        c = mDB.query(true, tableGM, new String[] {COLUMN_DEST_ID, COLUMN_DEST_DESCR},
                null, null, null, null, null, null);

         */
        c = mDB.rawQuery(sqlQuery, null);
        int count = c.getCount();
        if(count > 0) {
            ret = new Client[count];
            for (int i = 0; i < count; i++) {
                c.moveToNext();
                Client cl = new Client(c.getString(0), c.getString(1));
                ret[i] = cl;
//                Log.d(TAG, "Client id " + cl.clientId + " description " + cl.clientDescription + " " + i
//                                + " " + c.getString(2)
//                        + " " + c.getString(3)
//                );
            }
        }
        c.close();
        return ret;
    }
    public static void debugClient(String clientId) {
        String tableGM = DB_TABLE_GOODSMOVEMENT;
        Cursor c = mDB.query(tableGM, null, COLUMN_DEST_ID + " =?", new String[] {clientId}, null, null, null);
        int count = c.getCount();
        Log.d(TAG, "Found " + count + " rows for " + clientId);
        /*
        while (c.moveToNext()) {
            String goodsDesc = c.getString(2);
            String cellInDescr = c.getString(7);
            String cellOutDescr = c.getString(8);
            int qnt = c.getInt(11);
            String client = c.getString(23);
            Log.d(TAG, "Client " + client + " goods " + goodsDesc + " cell out " + cellOutDescr + " cell in " + cellInDescr + " qnt " + qnt);
        }

         */
        c.close();
    }
}
