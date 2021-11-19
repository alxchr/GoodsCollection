package ru.abch.goodscollection;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLConfig;
import com.bosphere.filelogger.FLConst;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class App extends Application {
    static ru.abch.goodscollection.App instance = null;
    static String TAG = "App";
    static SharedPreferences sp;
    private static int storeIndex, storeMan, dctNum;
    private static String storeId, storeName, bthw;
    private static final String storeIndexKey = "store_index";
    private static final String storeIdKey = "store_id";
    private static final String storeNameKey = "store_name";
    private static final String storeManKey = "store_man";
    private static final String bthwKey = "bt_hw";
    private static final String timeKey = "time";
    private static final String dctNumKey = "dct_num";
    private static final String pickModeKey = "pick_mode";
    public static int state = 0;
    public static final int START = 0, TIMINGS = 1, GOODSPICKING = 2, MAINCYCLE = 3, FINISHPICKING = 4;
//    public static int iddocdef = 0;
    GregorianCalendar calendar;
    public static Database db;
    public static Config config;
    private static String timestamp;
    public static boolean storemanSelected = false;
    public static String zonein = "";
    public static String zonein_descr = "";
    public static final int ZONE_PICK = 0, ZONE_DUMP = 1;
    private static ArrayList<Zone> workZones;
    public static Timing currentTiming = null;
    public static String deviceUniqueIdentifier;
    public static int currentDistance;
    public ArrayList<String> workZonesCells;
    public static Cell[] cells = null;
    public static ArrayList<GoodsMovement> pickedGoods = new ArrayList<>();
    private static boolean unitedPick;

    public static boolean getUnitedPick() {
        return unitedPick;
    }

    public static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(App.get());
    }

    public static App get() {
        return instance;
    }

    public static int getStoreIndex() {
        return storeIndex;
    }

    ;

    public static String getStoreId() {
        Log.d(TAG, "Get store id =" + storeId);
        return storeId;
    }
    public static String getStoreName(){return storeName;}
    public static int getStoreMan() { return storeMan;}
    public static void setStoreIndex(int i) {
        storeIndex = i;
        sp.edit().putInt(storeIndexKey, i).apply();
    }
    public static void setStoreId(String id) {
        storeId = id;
        Log.d(TAG, "Set store id =" + id);
        sp.edit().putString(storeIdKey, id).apply();
    }
    public static void setStoreName(String name) {
        storeName = name;
        sp.edit().putString(storeNameKey, name).apply();
    }
    public static void setStoreMan(int sm) {
        storeMan = sm;
        sp.edit().putInt(storeManKey, sm).apply();
    }
    public static void setBtHW(String hwaddr) {
        bthw = hwaddr;
        sp.edit().putString(bthwKey, hwaddr).apply();
    }
    public static String getBthw() {return  bthw;}
    public static String getTimestamp() {
        return timestamp;
    }
    public static void setTimestamp(String ts) {
        timestamp = ts;
        sp.edit().putString(timeKey, ts).apply();
    }
    public static int getDctNum() {
        return dctNum;
    }
    /*
    public static String getDctNum() {
        return deviceUniqueIdentifier;
    }

     */
    public static void setDctNum(int num) {
        dctNum = num;
        sp.edit().putInt(dctNumKey, num).apply();
    }

    public static ArrayList<Zone> getWorkZones() {
        return workZones;
    }

    public static void setWorkZones(ArrayList<Zone> zones) {
        workZones = zones;
    }

    public static void clearPickedGoods() {
        pickedGoods = new ArrayList<>();
    }

    public static void setUnitedPick(boolean mode) {
        unitedPick = mode;
        sp.edit().putBoolean(pickModeKey, mode).apply();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        config = new Config();
        instance = this;
        sp = getSharedPreferences();
        storeIndex = sp.getInt(storeIndexKey, -1);
        storeId = sp.getString(storeIdKey, "    12SPR");
        storeName = sp.getString(storeNameKey, "");
        storeMan = sp.getInt(storeManKey, 0);
        bthw = sp.getString(bthwKey, "");
        unitedPick = sp.getBoolean(pickModeKey, false);
        calendar = new GregorianCalendar(2000, Calendar.JANUARY, 1);
        Config.timeShift = calendar.getTime().getTime();
        FL.init(new FLConfig.Builder(this)
                .minLevel(FLConst.Level.V)
                .logToFile(true)
                .dir(new File(Environment.getExternalStorageDirectory(), "GoodsCollection"))
                .retentionPolicy(FLConst.RetentionPolicy.FILE_COUNT)
                .build());
        FL.setEnabled(true);
        FL.d(TAG, "Start application store id =" + storeId + " store index =" + storeIndex + " storeman = " + storeMan);
        db = new Database(this);
        db.open();
        timestamp = sp.getString(timeKey, "0");
        dctNum = sp.getInt(dctNumKey, 0);
        deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        workZonesCells = new ArrayList<>();
        pickedGoods = Database.pickedGoods();
        FL.d(TAG, "Picked goods size =" + pickedGoods.size());
    }
}
