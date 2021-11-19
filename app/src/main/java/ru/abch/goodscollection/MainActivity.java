package ru.abch.goodscollection;

import static android.os.Environment.isExternalStorageEmulated;
import static android.text.InputType.TYPE_CLASS_NUMBER;
import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bosphere.filelogger.FL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.abch.goodscollection.ui.main.MainFragment;
import ru.abch.goodscollection.ui.main.MainViewModel;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, SoundPool.OnLoadCompleteListener {
    private static final int REQ_PERMISSIONS = 1230, REQUEST_ENABLE_BT = 1231;
    private static final String TAG = "MainActivity";
    String btmac = "a4:da:32:83:cc:dd";
    public Connection printerConnection;
    public ZebraPrinter printer;
    final String[] ids = new String[]{"     2   ", "     JCTR", "    10SSR", "    12SPR", "    1ASPR", "    1BSPR", "    1ISPR", "    1LSPR",
            "    1OSPR", "    1PSPR", "    1CSPR", "    1SSPR", "    1USPR", "    15SPR", "    1TSPR", "    28SPR", "    27SPR", "    2BSPR",
            "    2FSPR", "    2GSPR", "    2DSPR", "    2HSPR", "    2ISPR", "    2JSPR"};
    String[] names;
    public MainViewModel mViewModel;
    AlertDialog.Builder adbSettings, adbDCTNum, adbUpload, adbPrinter, adbLabel, adbPicked, adbPosition, adbSelection;
    private static TextToSpeech mTTS;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    String[] devices = null, hwDevices = null;
    private static final String ACTION_BARCODE_DATA = "com.honeywell.sample.action.BARCODE_DATA";
    private final int ERROR = 1;
    private int state = ERROR;
    GetWSBarcodes getBarCodes;
    GetWSCells getCells;
    GetWSGoods getGoods;
    PostWebservice postWS;
    URI uri = null;
    String barcodesURL = null;
    String cellsURL = null;
    String goodsURL = null;
    String[] timeIntervalName;
    int[] timeInterval;
    MainFragment mf = null;
    boolean backPressed = false;
    GoodsMovement[] selectedGoods = null;
    ConnectivityManager cm;
    IntentFilter intentFilter;
    public static boolean online = false;
    private Timer mTimer;
    private OfflineTimerTask offlineTimerTask;
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public ArrayList<GoodsMovement> goodsMovements = null;
    public static boolean urgent = false;
    SoundPool soundPool;
    int soundId1;
    final int MAX_STREAMS = 5;
    TimingFragment tf;
    GoodsPickingFragment gpf;
    ArrayList<Zone> zones = null;
    Context mContext;
    public static GoodsMovement[] updateGoodsMovements = null;
    public static ArrayList<Long> pickedRowsForDelete = null;
    PickedFragment pf;
    AlertDialog adPosition;
    FinishPickingFragment fpf;
    GetWSDump getWSDump;
    String dumpURL = null;
    public static GoodsMovement currentGoods;
    private int labelQnt;
    LinearLayout llPrintLabel, llPickedGoods;
    int printerType = 0;
    int labNum;
    boolean photoShown = false;
    final int POSTGOODS = 1, POSTLABEL = 2, POSTDEFICIENCY = 3;
    String filenameSD;
    Timer refreshTimer;
    boolean requestPosition = false, unitedMode = false, nextMode;
    public static String clientId = null;
    Cell newPosition;
    final Handler uiHandler = new Handler();
    boolean testLabel = false;
    private EditText etDCTNumber, etLabels, etLabelQnt, etPosition;

    public static byte[] getConfigLabel(String goods, int qnt, int storeman, String article, String description, String cellDescr) {
        byte[] configLabel;
        String desc;
        String barcode = "G" + goods + Integer.toString(qnt, 36).toUpperCase();
        String header = qnt + " шт.             кладовшик " + storeman;
        try {
            description.getBytes("cp1251");
            desc = description;
        } catch (UnsupportedEncodingException e) {
            desc = Config.transliterate(description);
        }
        String cpclConfigLabel = "! 50 200 200 227 1\r\n" +
                "ON-FEED IGNORE\r\n" +
//                    "BOX 0 10 440 210 8\r\n" +
//                    "ENCODING UTF-8\r\n" +
                "ML 30\r\n" +
//                "T 0 3 10 0 \r\n" +
                "TEXT ARIAL02.CPF 0 10 0 " +
                header +
                "\r\n" +
                cellDescr + "    " + article + "\r\n" +
                desc + "\r\n" +
                "ENDML\r\n" +
                "CENTER\r\n" +
//                    "BARCODE-TEXT 7 0 5\r\n" +
                "BARCODE 39 1 0 50 0 130 " + barcode + "\r\n" +
//                    "BARCODE-TEXT OFF\r\n" +
                "FORM\r\n" +
                "PRINT\r\n";
//        Log.d(TAG,cpclConfigLabel);

        try {
            configLabel = cpclConfigLabel.getBytes("cp1251");
        } catch (UnsupportedEncodingException e) {
            FL.d(TAG, "Article " + article + " barcode " + barcode + " description " + description);
            e.printStackTrace();
            FL.e(TAG, e.getMessage());
            configLabel = cpclConfigLabel.getBytes();
        }
        return configLabel;
    }

    public ZebraPrinter connect() {
        Log.d(TAG, "Connecting...");
        printerConnection = null;
        printerConnection = new BluetoothConnection(btmac);
        try {
            printerConnection.open();
            Log.d(TAG, "Connected");
        } catch (ConnectionException e) {
            Log.e(TAG, "Comm Error! Disconnecting");
            disconnect();
        }
        ZebraPrinter printer = null;
        if (printerConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(printerConnection);
                Log.d(TAG, "Determining Printer Language");
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                Log.d(TAG, "Printer Language " + pl);
            } catch (ConnectionException e) {
                Log.e(TAG, "Unknown Printer Language");
                printer = null;
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                Log.e(TAG, "Unknown Printer Language");
                printer = null;
                disconnect();
            }
        }
        return printer;
    }

    public void disconnect() {
        try {
            Log.d(TAG, "Disconnecting");
            if (printerConnection != null) {
                printerConnection.close();
            } else {
                Log.e(TAG, "Not Connected");
            }
        } catch (ConnectionException e) {
            Log.e(TAG, "COMM Error! Disconnected");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (printerConnection != null && printerConnection.isConnected()) {
            disconnect();
        }
    }

    private void sendLabel() {
        try {
            String description;// = Config.transliterate(currentGoods.goods_descr);
            description = currentGoods.goods_descr;
            if (description.length() > 28) {
//                description = Config.transliterate(currentGoods.goods_descr).substring(0,28);
                description = currentGoods.goods_descr.substring(0, 27);
            }
            String idBarcode = currentGoods.goods.replaceAll(" ", ".");
            FL.d(TAG, "id " + idBarcode + " num36 " + Integer.toString(labelQnt, 36));
            byte[] configLabel = getConfigLabel(idBarcode, labelQnt, App.getStoreMan(), currentGoods.goods_article,
                    description, currentGoods.cellOut_descr);
            printerConnection.write(configLabel);
            Log.d(TAG, "Sending Data");
            if (printerConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();
                Log.d(TAG, friendlyName);
            }
        } catch (ConnectionException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(openFileInput("stack.trace")));
            String line, trace = "";
            while((line = reader.readLine()) != null) {
                trace += line+"\n";
            }
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            String subject = "Error report";
            String body = "\n" + trace + "\n";
            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"comtt.ru@yandex.ru"});
            sendIntent.putExtra(Intent.EXTRA_TEXT, body);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            sendIntent.setType("message/rfc822");
            startActivity(Intent.createChooser(sendIntent, "Title:"));
            deleteFile("stack.trace");
        } catch (FileNotFoundException fnfe) {
            Log.d(TAG, "Send e-mail: File not found");
        } catch (IOException ioe) {
            Log.d(TAG, "Send e-mail: IO exception");
            deleteFile("stack.trace");
        } catch (Exception e) {
            deleteFile("stack.trace");
            Log.d(TAG, "Can't send e-mail");
        }
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_smallogo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.main_activity);
        ArrayList<String> requestPermissionsList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.BLUETOOTH);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.INTERNET);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissionsList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(requestPermissionsList.size() > 0) {
            String[] requestPermissionsArray = new String[requestPermissionsList.size()];
            requestPermissionsArray = requestPermissionsList.toArray(requestPermissionsArray);
            ActivityCompat.requestPermissions(this, requestPermissionsArray, REQ_PERMISSIONS);
        }
        Log.d(TAG, getResources().getString(R.string.app_name) + " started");
        App.cells = Database.getCells();
//        Log.d(TAG, "Cells # " + App.cells.length);
        if (savedInstanceState == null || mf == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance(App.getWorkZones()), MainFragment.class.getSimpleName())
                    .commitNow();
        }
        mTTS = new TextToSpeech(this, this);
        names = getResources().getStringArray(R.array.store_names);
        adbSettings = new AlertDialog.Builder(this);
        if(App.getStoreIndex() < 0) {
            adbSettings.setTitle(R.string.store_choice)
                    .setItems(names, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            FL.d(TAG, "Index = " + which + " store id=" + ids[which]);
                            App.setStoreIndex(which);
                            App.setStoreId(ids[which]);
                            App.setStoreName(names[which]);
                        }

                    }).create().show();
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            devices = new String[pairedDevices.size()];
            hwDevices = new String[pairedDevices.size()];
            int i = 0;
            for (BluetoothDevice device : pairedDevices) {
                devices[i] = device.getName();
                hwDevices[i++] = device.getAddress();
            }
        }
        btmac = App.getBthw();
        Log.d(TAG, "Bluetooth address " + btmac);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.addDefaultNetworkActiveListener(new ConnectivityManager.OnNetworkActiveListener() {
            @Override
            public void onNetworkActive() {
                FL.d(TAG,"Network active");
            }
        });
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        try {
            uri = new URI(
                    Config.scheme, null, Config.host, Config.port,
                    Config.barcodesPath + App.getStoreId() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        barcodesURL = uri.toASCIIString();
        try {
            uri = new URI(
                    Config.scheme, null, Config.host, Config.port,
                    Config.cellsPath + App.getStoreId() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        cellsURL = uri.toASCIIString();
        try {
            uri = new URI(
                    Config.scheme, null, Config.host, Config.port,
                    Config.goodsPath + App.getStoreId() + "/" + App.getTimestamp() + "/0/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        goodsURL = uri.toASCIIString();
        timeInterval = getResources().getIntArray(R.array.time_interval);
        timeIntervalName = getResources().getStringArray(R.array.time_interval_name);
        soundPool= new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(this);
        soundId1 = soundPool.load(this, R.raw.pik, 1);
        try {
            uri = new URI(
                    Config.scheme, null, Config.host, Config.port,
                    Config.dumpPath + App.getStoreId() + "/" + App.deviceUniqueIdentifier + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        dumpURL = uri.toASCIIString();
        refreshCells();
        filenameSD = App.deviceUniqueIdentifier+ "_" + getCurrentDate() + ".txt";
        refreshTimer  = new Timer();
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                FL.d(TAG, "Refresh on timer");
                refreshOnTimer();
            };
        }, 60000L, 60000L);
        getDump();
    }

    public void sendLabel(byte[] label) {
        try {
            Log.d(TAG, "Print label length " + label.length);
            printerConnection.write(label);
            if (printerConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) printerConnection).getFriendlyName();
                Log.d(TAG,friendlyName);
            }
        } catch (ConnectionException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    private byte[] getTestLabel() {
        byte[] configLabel;
        String barcode = "0123456789";
        String str = "ZQ220";
        String cpclConfigLabel = "! 50 200 200 227 1\r\n" +
                "ON-FEED IGNORE\r\n" +
                "ML 30\r\n" +
                "TEXT ARIAL02.CPF 0 10 0 " +
                str + " \r\n" +
                "Тест принтера" + " \r\n" +
                "ENDML\r\n" +
                "CENTER\r\n" +
                "BARCODE 39 1 1 50 0 130 " + barcode + "\r\n" +

                "FORM\r\n" +
                "PRINT\r\n";
        try {
            configLabel = cpclConfigLabel.getBytes("cp1251");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            configLabel = cpclConfigLabel.getBytes();
        }
        return configLabel;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Add your menu entries here
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.warehouse_item:
                FL.d(TAG, "Warehouse clicked");
                adbSettings = new AlertDialog.Builder(this);
                adbSettings.setTitle(R.string.store_choice)
                        .setItems(names, (dialog, which) -> {
                            Log.d(TAG, "Index = " + which + " store id=" + ids[which]);
                            App.setStoreIndex(which);
                            App.setStoreId(ids[which]);
                            App.setStoreName(names[which]);
                        }).create().show();
                return true;
            case R.id.printer_item:
                FL.d(TAG, "Printer clicked");
                if(devices != null) {
                    adbPrinter = new AlertDialog.Builder(this);
                    adbPrinter.setTitle(R.string.printer_choice).setItems(devices, (dialog, which) -> {
                        Log.d(TAG, "Index = " + which + " printer " + devices[which]);
                        btmac = hwDevices[which];
                        App.setBtHW(btmac);
                    }).create().show();
                }
                return true;
            case R.id.print_item:
                FL.d(TAG, "Print clicked");
                Spinner spinner;
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.printers));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                if (currentGoods != null) {
                    adbLabel = new AlertDialog.Builder(this);
                    adbLabel.setCancelable(false);
                    adbLabel.setMessage(R.string.adb_label);
                    llPrintLabel = (LinearLayout) getLayoutInflater().inflate(R.layout.adb_label, null);
                    etLabels = llPrintLabel.findViewById(R.id.et_labels);
                    etLabelQnt = llPrintLabel.findViewById(R.id.et_label_qnt);
                    spinner = llPrintLabel.findViewById(R.id.spinner_printer);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(printerType);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view,
                                                   int position, long id) {
                            printerType = position;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            printerType = 0;
                        }
                    });
//                    etLabels = new EditText(this);
                    etLabels.setInputType(TYPE_CLASS_NUMBER);
                    etLabels.setText("1");
                    etLabels.setSelection(1);
                    etLabelQnt.setInputType(TYPE_CLASS_NUMBER);
                    etLabelQnt.setText(String.valueOf(currentGoods.qnt));
                    etLabelQnt.setSelection(etLabelQnt.getText().length());
//                    adbLabel.setView(etLabels);
                    adbLabel.setView(llPrintLabel);
                    etLabels.requestFocus();
                    adbLabel.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    adbLabel.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String num = etLabels.getText().toString();
                            labNum = Integer.parseInt(num);
                            labelQnt = Integer.parseInt(etLabelQnt.getText().toString());
                            FL.d(TAG, "Labels num " + num + " goods id " + currentGoods.goods + " qnt " + labelQnt);
                            if(printerType == 0) {
                                PrintLabelTask pt = new PrintLabelTask();
                                pt.execute();
                            } else {
                                GoodsLabel gl = new GoodsLabel(currentGoods.goods,
                                        currentGoods.goods_descr,
                                        currentGoods.goods_article,
                                        currentGoods.units,labelQnt,
                                        App.getStoreMan(),
                                        currentGoods.cellOut_descr);
                                GoodsLabel[] labels = new GoodsLabel[labNum];
                                for (int j = 0; j < labNum; j++) labels[j] = gl;
                                PrintLabelRequest pr = new PrintLabelRequest(labNum,labels);
                                postWS = new PostWebservice();
                                String smallPieceURL;
                                Gson gson = new Gson();
                                try {
                                    uri = new URI(
                                            Config.scheme, null, Config.host, Config.port,
                                            Config.labelsPath ,
                                            null, null);
                                    smallPieceURL = uri.toASCIIString();
                                    try {
                                        postWS.type = POSTLABEL;
                                        postWS.post(smallPieceURL, gson.toJson(pr));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    adbLabel.create().show();
                } else {
                    TestPrintTask tp = new TestPrintTask();
                    tp.execute();
                }
                return true;
            case R.id.refresh_item:
                FL.d(TAG, "Refresh clicked");
                if (App.state != App.START) refreshData();
                return true;
            case R.id.dct_num_item:
                FL.d(TAG, "DCT num clicked");
                etDCTNumber = new EditText(this);
                etDCTNumber.setInputType(TYPE_CLASS_NUMBER);
                etDCTNumber.setText(String.valueOf(App.getDctNum()));
                adbDCTNum = new AlertDialog.Builder(this);
                adbDCTNum.setCancelable(false);
                adbDCTNum.setMessage(R.string.dct_num);
                adbDCTNum.setView(etDCTNumber);
                adbDCTNum.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                adbDCTNum.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String num = etDCTNumber.getText().toString();
                        Log.d(TAG, "DCT num " + num);
                        int dctNum = Integer.parseInt(num);
                        App.setDctNum(dctNum);
                    }
                });
                adbDCTNum.create().show();
                return true;
            case R.id.small_piece:
                FL.d(TAG, "Small-piece clicked");
                PrintLabelRequest pr = Database.smallPieceGoods();
                if(pr != null) {
                    postWS = new PostWebservice();
                    String smallPieceURL;
                    Gson gson = new Gson();
                    try {
                        uri = new URI(
                                Config.scheme, null, Config.host, Config.port,
                                Config.labelsPath,
                                null, null);
                        smallPieceURL = uri.toASCIIString();
                        try {
                            postWS.type = POSTLABEL;
                            postWS.post(smallPieceURL, gson.toJson(pr));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else {
                    FL.d(TAG, "Small-piece: nothing to print");
                }
                return true;
            case R.id.photo_item:
                Log.d(TAG, "Photo clicked");
                String photoURL;
                if (currentGoods != null && currentGoods.url != null) {
                    if (currentGoods.url.length() > 0) {
                        photoURL = Config.photoPath + currentGoods.url;
                        GoodsPickingFragment gpf = (GoodsPickingFragment) getSupportFragmentManager().findFragmentByTag(GoodsPickingFragment.class.getSimpleName());
                        if (gpf != null) {
                            FL.d(TAG, GoodsPickingFragment.class.getSimpleName() + " is on, photo " + photoURL);
                            gpf.showPhoto(photoURL);
                            photoShown = true;
                        }
                    } else {
                        if(!this.isFinishing())
                        Toast.makeText(this, R.string.no_photo, Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            case R.id.picked_goods:
                Log.d(TAG, "Picked goods clicked");
                if (App.pickedGoods != null) {

                    adbPicked = new AlertDialog.Builder(this);
                    adbPicked.setCancelable(true);
                    adbPicked.setMessage(R.string.adb_picked);
                    llPickedGoods = (LinearLayout) getLayoutInflater().inflate(R.layout.adb_picked, null);
                    PickedAdapter pickedAdapter = new PickedAdapter(this, Database.pickedGoods());
                    ListView lvGoodsList = llPickedGoods.findViewById(R.id.lv_pickedlist);
                    lvGoodsList.setAdapter(pickedAdapter);
                    adbPicked.setView(llPickedGoods);
                    adbPicked.create().show();
                }
                return true;
            case R.id.position:
                Log.d(TAG, "Position clicked");
                requestPosition = true;
                adbPosition = new AlertDialog.Builder(this);
                adbPosition.setCancelable(true);
                adbPosition.setMessage(R.string.current_position);
                etPosition = new EditText(this);
                adbPosition.setView(etPosition);
                adPosition = adbPosition.create();
                etPosition.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        Log.d(TAG, "On text changed =" + charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String input;
                        int entIndex;
                        int prefix, suffix;
                        String result;
                        if (editable.length() > 2) {
                            input = editable.toString();
                            if (input.contains("\n") && input.indexOf("\n") == 0) {
                                input = input.substring(1);
                            }
                            if (input.contains("\n") && input.indexOf("\n") > 0) {
                                entIndex = input.indexOf("\n");
                                input = input.substring(0, entIndex);
                                if (CheckCode.checkCellStr(input)) {
                                    prefix = Integer.parseInt(input.substring(0, input.indexOf(".")));
                                    suffix = Integer.parseInt(input.substring(input.indexOf(".") + 1));
                                    result = String.format("%02d", prefix) + String.format("%03d", suffix);
                                    Log.d(TAG, "Cell name " + result);
                                    newPosition = Database.getCellByName(result);
                                    requestPosition = false;
                                    adPosition.dismiss();
                                    if (newPosition != null) setPosition(newPosition);
                                } else {
                                    MainActivity.say(getResources().getString(R.string.enter_again));
                                }
                                etPosition.setText("");
                            }
                        }
                    }
                });
                adPosition.show();
                return true;
            case R.id.pick_mode:
                showAdbSelection();
                break;
            default:
                break;
        }
        return false;
    }

    private void showAdbSelection() {
        LinearLayout llAdbSelection = (LinearLayout) getLayoutInflater().inflate(R.layout.adb_pick_mode, null);
        RadioGroup rgSelection = llAdbSelection.findViewById(R.id.rg_selection);
        RadioButton rbSeparated = llAdbSelection.findViewById(R.id.rb_separated);
        RadioButton rbUnited = llAdbSelection.findViewById(R.id.rb_united);
        unitedMode = App.getUnitedPick();
        rgSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d(TAG, "Radio group " + i);
                switch (i) {
                    case R.id.rb_separated:
                        nextMode = false;
                        break;
                    case R.id.rb_united:
                        nextMode = true;
                        break;
                    default:
                        break;
                }

            }
        });
        if (unitedMode) {
            rbSeparated.setChecked(false);
            rbUnited.setChecked(true);
        } else {
            rbSeparated.setChecked(true);
            rbUnited.setChecked(false);
        }
        adbSelection = new AlertDialog.Builder(this);
        adbSelection.setTitle(R.string.pick_mode).setView(llAdbSelection);
        adbSelection.setNegativeButton(R.string.cancel,
                (dialog, which) -> dialog.cancel());
        adbSelection.setPositiveButton(R.string.ok,
                (dialog, which) -> {
                    App.setUnitedPick(nextMode);
                    if (unitedMode != nextMode) {

                        unitedMode = nextMode;
                        App.setUnitedPick(unitedMode);
                    }
                });
        adbSelection.create().show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = new Locale("ru");
            int result = mTTS.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS: language not supported");
            }
            if (result == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TTS OK App.state = " + App.state);
                if (App.state == App.START && !App.storemanSelected) say(getResources().getString(R.string.storeman_number_tts));
            }
        } else {
            Log.e(TAG, "TTS: error");
        }
    }
    public static void say(String text) {
        if (Config.tts) mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }
    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        Log.d(TAG, "onLoadComplete, sampleId = " + i + ", status = " + i1);
    }

    private BroadcastReceiver barcodeDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_BARCODE_DATA.equals(intent.getAction())) {
/*
These extras are available:
"version" (int) = Data Intent Api version
"aimId" (String) = The AIM Identifier
Honeywell Android Data Collection Intent APIAPI DOCUMENTATION
"charset" (String) = The charset used to convert "dataBytes" to "data" string
"codeId" (String) = The Honeywell Symbology Identifier
"data" (String) = The barcode data as a string
"dataBytes" (byte[]) = The barcode data as a byte array
"timestamp" (String) = The barcode timestamp
*/
                int version = intent.getIntExtra("version", 0);
                if (version >= 1) {
                    String codeId = intent.getStringExtra("codeId");
                    String data = intent.getStringExtra("data");
                    processScan(data, codeId);
                }
            }
        }
    };

    private void processScan(String scan, String codeId) {
        Log.d(TAG, "Code id=" + codeId + " barcode =" + scan);
        if (codeId.equals("c") && scan.length() == 11) {
//            Log.d(TAG, "Scan UPC =" + scan);
            //Rebuild UPC check digit
            String res = scan;
            int[] resDigit = new int[11];
            for (int i = 0; i < 11; i++) {
                resDigit[i] = Integer.parseInt(res.substring(i, i + 1));
            }
            int e = resDigit[1] + resDigit[3] + resDigit[5] + resDigit[7] + resDigit[9];
            int o = resDigit[0] + resDigit[2] + resDigit[4] + resDigit[6] + resDigit[8] + resDigit[10];
            o *= 3;
            String r = String.valueOf(o + e);
            int c = 10 - Integer.parseInt(r.substring(r.length() - 1));
            if (c == 10) c = 0;
            res = res + c;
            FL.d(TAG, "Rebuilt UPC code =" + res);
            processScan(res);
        } else
        if (codeId.equals("d")  && scan.length() == 12) {
            //Rebuild EAN13 check digit
            String res = scan;
            int[] resDigit = new int[12];
            for (int i = 0; i < 12; i++) {
                resDigit[i] = Integer.parseInt(res.substring(i, i + 1));
            }
            int e = (resDigit[1] + resDigit[3] + resDigit[5] + resDigit[7] + resDigit[9] + resDigit[11]) * 3;
            int o = resDigit[0] + resDigit[2] + resDigit[4] + resDigit[6] + resDigit[8] + resDigit[10];
            String r = String.valueOf(o + e);
            int c = 10 - Integer.parseInt(r.substring(r.length() - 1));
            if (c == 10) c = 0;
            res = res + c;
            FL.d(TAG, "Rebuilt EAN13 code =" + res + " request position " + requestPosition);
            if (requestPosition) {
                requestPosition = false;
                String result = Config.getCellName(res);
                if (result == null) {
                    newPosition = null;
                } else {
                    newPosition = Database.getCellByName(result);
                }
                FL.d(TAG, "Scanned cell, code " + res + " name " + result);
                adPosition.dismiss();
                if (newPosition != null) setPosition(newPosition);
            } else {
                processScan(res);
            }
        } else
        if (codeId.equals("b")) {
            if (CheckCode.checkGoods39(scan) && scan.length() >= 11 && scan.contains(".")) {
                String goodsId = scan.substring(1, 10).replaceAll("\\.", " ");
                String goodsQnt = scan.substring(10).replaceAll("\\.", "");
//                Log.d(TAG, "Goods id " + goodsId + " goods qnt " + goodsQnt);
                if (goodsQnt != null && goodsId != null) {
                    int qnt;
                    try {
                        qnt = Integer.parseInt(goodsQnt, 36);
                        Log.d(TAG, "Goods id " + goodsId + " goods qnt " + qnt);
                        GoodsMovement[] gms = Database.getGoodsById(goodsId);
                        if (gms != null) {
                            GoodsPickingFragment gpf = (GoodsPickingFragment) getSupportFragmentManager().findFragmentByTag(GoodsPickingFragment.class.getSimpleName());
                            if (gpf != null) {
                                FL.d(TAG, GoodsPickingFragment.class.getSimpleName() + " is on, goods " + gms[0].goods_descr);
                                gpf.processScan(gms, qnt);
                            }
                        } else {
                            say(getResources().getString(R.string.wrong_barcode));
                        }
                    } catch (NumberFormatException e) {
//                        Log.d(TAG, " Code 39 barcode " + scan);
                        processScan(scan);
                    }
                } else {
                    processScan(scan);
                }
            } else {
                processScan(scan);
            }
        }   else processScan(scan);
    }
    private void processScan(String scan) {
        tf = (TimingFragment) getSupportFragmentManager().findFragmentByTag(TimingFragment.class.getSimpleName());
        GoodsPickingFragment gpf = (GoodsPickingFragment) getSupportFragmentManager().findFragmentByTag(GoodsPickingFragment.class.getSimpleName());
        pf = (PickedFragment) getSupportFragmentManager().findFragmentByTag(PickedFragment.class.getSimpleName());
        fpf = (FinishPickingFragment) getSupportFragmentManager().findFragmentByTag(FinishPickingFragment.class.getSimpleName());
        if (tf != null) {
            FL.d(TAG, TimingFragment.class.getSimpleName() + " is on, scanned " + scan);
            tf.startCellScanned(scan);
        } else if (gpf != null) {
            FL.d(TAG, GoodsPickingFragment.class.getSimpleName() + " is on, scanned " + scan);
            gpf.processScan(scan);
        } else if (pf != null) {    //After start goods saved, but not uploaded.
            FL.d(TAG, PickedFragment.class.getSimpleName() + " is on, scanned " + scan);
            pf.dumpCellScanned(scan);
        } else if (fpf != null) {   //Finish picking regular way
            FL.d(TAG, FinishPickingFragment.class.getSimpleName() + " is on, scanned " + scan);
            fpf.dumpCellScanned(scan);
        }
    }

    private void setPosition(Cell cell) {
        GoodsPickingFragment gpf = (GoodsPickingFragment) getSupportFragmentManager().findFragmentByTag(GoodsPickingFragment.class.getSimpleName());
        App.currentDistance = cell.distance;
        Log.d(TAG, "Set position distance " + App.currentDistance);
        if (gpf != null) {
            gpf.setPosition();
        }
    }

    public class GetWSBarcodes {
        OkHttpClient client;
        String TAG = "GetWSBarcodes";

        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            client = getHTTPClient(url);
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    BarCodesResult bcr = gson.fromJson(resp, BarCodesResult.class);
                    if (bcr != null) {
                        Log.d(TAG, "Result = " + bcr.success + " length = " + bcr.counter);
                        if (bcr.counter > 0) {
                            Database.beginTr();
                            for (int i = 0; i < bcr.counter; i++) {
                                Database.addBarCode(
                                        bcr.bc[i].goods,
                                        bcr.bc[i].barcode,
                                        bcr.bc[i].qnt
                                );
                            }
                            Database.endTr();
                        }
                    }
                }

                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    public class PostWebservice {
        public final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client;
        int type = 0;
        String TAG = "PostWebService";
        void post(String url, String json) throws IOException {
            if (type != POSTLABEL) writeFileSD(json);
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Log.d(TAG, "POST url " + url);
            client = getHTTPClient(url);
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    FL.d(TAG, "Call error " + e.getMessage());
                    say(getResources().getString(R.string.no_connection));
                }
                public void onResponse(Call call, Response response)
                        throws IOException {
                    FL.d(TAG, "Responce =" + response);
                }
            });
        }
    }
    public void refreshData() {
        getBarCodes = new GetWSBarcodes();
        getGoods = new GetWSGoods();
//        Database.clearGoodsMovements();
        try {
            getBarCodes.run(barcodesURL);
            getGoods.run(goodsURL, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void refreshOnTimer() {
        getDump();
        getGoods = new GetWSGoods();
        try {
            getGoods.run(goodsURL, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshCells() {
        getCells = new GetWSCells();
        try {
            getCells.run(cellsURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public class GetWSCells {
        OkHttpClient client;
        String TAG = "GetWSCells";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            client = getHTTPClient(url);
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    CellsResult cellsResult = gson.fromJson(resp, CellsResult.class);
                    if (cellsResult != null) {
                        Log.d(TAG, "Result = " + cellsResult.success + " length = " + cellsResult.counter);
                        if (cellsResult.counter > 0) {
                            Database.beginTr();
                            Database.clearCells();
                            for (int i = 0; i < cellsResult.counter; i++) {
//                                Log.d(TAG, " " + bcr.bc[i].goods + " " + bcr.bc[i].barcode+ " " + bcr.bc[i].qnt);
                                Database.addCell(
                                        cellsResult.cells[i].id,
                                        cellsResult.cells[i].name,
                                        cellsResult.cells[i].descr,
                                        cellsResult.cells[i].type,
                                        cellsResult.cells[i].distance,
                                        cellsResult.cells[i].zonein,
                                        cellsResult.cells[i].zonein_descr,
                                        cellsResult.cells[i].emptySize
                                );
                            }
                            Database.endTr();
//                            App.cells = cellsResult.cells;
                        }
                    }
                    zones = Database.getZones(App.ZONE_DUMP);
                    if(zones != null) {
                        for(int i = 0; i < zones.size(); i++)  Log.d(TAG, "Dump zone " + zones.get(i).zonein + " " + zones.get(i).zonein_descr);
                    }
                }
                public void onFailure(Call call, IOException e) {
                    FL.d(TAG, "Refresh cell error" + e.getMessage() + "\n from db cells #"  + App.cells.length);
                }
            });
        }
    }
    public class GetWSGoods {
        OkHttpClient client;
        String TAG = "GetWSGoods", photoURL;
        void run(String url, boolean runUI) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            client = getHTTPClient(url);
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    GoodsMovementResult goodsResult = gson.fromJson(resp, GoodsMovementResult.class);
                    if (goodsResult != null) {
                        FL.d(TAG, "Result = " + goodsResult.success + " length = " + goodsResult.counter + " time " + goodsResult.timestamp + " state = " + App.state);
                        if (goodsResult.success) {
                            App.setTimestamp(goodsResult.timestamp);
                            try {
                                uri = new URI(
                                        Config.scheme, null, Config.host, Config.port,
                                        Config.goodsPath + App.getStoreId() + "/" + App.getTimestamp() + "/0/",
                                        null, null);
                                goodsURL = uri.toASCIIString();
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                            if (runUI) {
                                Database.clearGoodsMovements();
                                if (goodsResult.counter > 0) {
                                    Database.beginTr();
                                    FL.d(TAG, "Start addGoodsMovement() for " + goodsResult.counter + " positions");
                                    if (App.state == App.MAINCYCLE) runOnUiThread(runWaitButtonsChanged);
                                    for (int i = 0; i < goodsResult.counter; i++) {
                                        if (inSelectedZones(goodsResult.goodsMovements[i].cellOut)) {
                                            photoURL = goodsResult.goodsMovements[i].url;
                                            if (photoURL != null && photoURL.length() > 0 && photoURL.contains("\\")) {
                                                photoURL = photoURL.replaceAll("\\\\", "/");
                                            }
                                            if (goodsResult.goodsMovements[i].qnt > 0 &&
                                                    Database.checkLock(goodsResult.goodsMovements[i].goods,
                                                        goodsResult.goodsMovements[i].mdoc,
                                                        goodsResult.goodsMovements[i].cellOut_task,
                                                        goodsResult.goodsMovements[i].cellIn_task) == 0) {
                                                Database.addGoodsMovement(
                                                        goodsResult.goodsMovements[i].goods,
                                                        goodsResult.goodsMovements[i].goods_descr,
                                                        goodsResult.goodsMovements[i].cellOut,
                                                        goodsResult.goodsMovements[i].cellOut_descr,
                                                        goodsResult.goodsMovements[i].cellIn,
                                                        goodsResult.goodsMovements[i].cellIn_descr,
                                                        goodsResult.goodsMovements[i].cellOut_task,
                                                        goodsResult.goodsMovements[i].cellIn_task,
                                                        goodsResult.goodsMovements[i].mdoc,
                                                        goodsResult.goodsMovements[i].iddocdef,
                                                        goodsResult.goodsMovements[i].qnt,
                                                        goodsResult.goodsMovements[i].startTime,
                                                        goodsResult.goodsMovements[i].goods_article,
                                                        goodsResult.goodsMovements[i].goods_brand,
                                                        goodsResult.goodsMovements[i].units,
                                                        goodsResult.goodsMovements[i].zonein,
                                                        goodsResult.goodsMovements[i].zonein_descr,
                                                        photoURL,
                                                        goodsResult.goodsMovements[i].dest_id,
                                                        goodsResult.goodsMovements[i].dest_descr
                                                );
                                            } else {
                                                FL.d(TAG, "Goods already picked " + goodsResult.goodsMovements[i].goods_article + " qnt " +
                                                        goodsResult.goodsMovements[i].qnt);
                                            }
                                        }
                                    }
                                    Database.endTr();
                                    FL.d(TAG, "End addGoodsMovement() state = " + App.state);
                                }
                                switch (App.state) {
                                    case App.MAINCYCLE:
                                        runOnUiThread(runButtonsChanged);
                                        runOnUiThread(runClientSpinnerChanged);
                                        break;
                                    case App.TIMINGS:
                                        runOnUiThread(runTimingFragment);
                                        break;
                                    case App.GOODSPICKING:
                                        runOnUiThread(runPickingFragment);
                                        break;
                                    case App.FINISHPICKING:
                                        break;
                                    default:
                                        runOnUiThread(runMainFragment);
                                        break;
                                }
                            } else {
                                updateGoodsMovements = goodsResult.goodsMovements;
                                FL.d(TAG, "Update on timer size " + updateGoodsMovements.length );
                            }
                        }
                    }
                }
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    Runnable runClientSpinnerChanged = () -> {
        mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
        if(mf != null) {
            mf.buildClientsArray();
            mf.spinnerAdapter.notifyDataSetChanged();
        } else {
            FL.e(TAG, MainFragment.class.getSimpleName() + " not found");
        }
    };
    Runnable runButtonsChanged = () -> {
        mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
        if(mf != null) {
            mf.enableButtons(true);
            mf.setButtons();
        } else {
            FL.e(TAG, MainFragment.class.getSimpleName() + " not found");
        }
    };
    Runnable runWaitButtonsChanged = () -> {
        mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
        if(mf != null) {
            mf.enableButtons(false);
        } else {
            FL.e(TAG, MainFragment.class.getSimpleName() + " not found");
        }
    };
    Runnable runMainFragment = () -> {
        mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
        if(mf != null) gotoMainFragment(App.getWorkZones());
    };
    Runnable runTimingFragment = () -> gotoTimingFragment(App.zonein);
    Runnable runPickingFragment = () -> gotoPickingFragment(goodsMovements);
    public ArrayList<Timing> getTimings(String zonein) {
        long now = Config.toComttTime(System.currentTimeMillis());
        long after = 0, before = now + timeInterval[0];
        ArrayList<Timing> res = new ArrayList<>();
        int qty;
        int intervalCount = (clientId == null)?  timeInterval.length : 1;   // set 1 if client was selected
        for (int i = 0; i < intervalCount; i++) {
            if (clientId == null) {
                qty = (i < intervalCount - 1) ? Database.countPositions(zonein, after, before) : Database.countPositions(zonein, after);
            } else {
                qty = Database.countPositions(zonein, clientId);
            }
            FL.d(TAG,"i = " + i + " after = " + Config.comttTime(after) + " before = " + Config.comttTime(before) + " qty = " + qty);
            if(i == 0) urgent = false;
            if (qty > 0) {
                if(i == 0) urgent = true;
                if(selectedGoods == null) {
                    if (clientId == null) {
                        selectedGoods = (i == 4) ? Database.selectGoods(zonein, after) : Database.selectGoods(zonein, after, before);
                    } else {
                        selectedGoods = Database.selectGoods(zonein, clientId);
                    }
                    int selected = (selectedGoods == null)? 0 : selectedGoods.length;
                    FL.d(TAG,"Select goods zone " + zonein + " after " + Config.comttTime(after) +
                            " before " + Config.comttTime(before) + " selected " + selected);
                    if(selectedGoods != null && selectedGoods.length > 0) {
                        FL.d(TAG, selectedGoods.length + " goods to collect");
                        goodsMovements = new ArrayList<>();
                        for (GoodsMovement gm : selectedGoods) {
                            goodsMovements.add(gm);
                        }
                    }
                }
                Timing timing = new Timing(timeInterval[i], timeIntervalName[i], qty);
                res.add(timing);
            }
            after = before;
            if (i < timeInterval.length -1) {
                before = now + timeInterval[i+1];
            }
        }
        return res;
    }
    private void deletePickedRows() {
        Database.beginTr();
        for (long rowId : pickedRowsForDelete) {
            Database.deletePickedGoods(rowId);
            FL.d(TAG, "Delete row " + rowId);
        }
        Database.endTr();
        pickedRowsForDelete = null;
    }
    public void gotoTimingFragment(String zonein) {
        App.state = App.TIMINGS;
        if (tf == null) {
            tf = TimingFragment.newInstance(zonein);
        }
        if(pickedRowsForDelete != null) {
            deletePickedRows();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, tf, TimingFragment.class.getSimpleName())
                .commitNow();
    }
    public void gotoMainFragment(ArrayList<Zone> workZones) {
        App.state = App.MAINCYCLE;
        getDump();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MainFragment.newInstance(workZones), MainFragment.class.getSimpleName())
                .commitNow();
    }
    public void gotoPickedFragment(ArrayList<GoodsMovement> pickedGoods) {
        if (pf == null) {
            pf = PickedFragment.newInstance(pickedGoods);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container,pf, PickedFragment.class.getSimpleName())
                    .commitNow();
        }
    }
    public void gotoStartFragment() {
        App.state = App.START;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, StartFragment.newInstance(), StartFragment.class.getSimpleName())
                .commitNow();
    }
    public void gotoFinishPickingFragment() {
        App.state = App.FINISHPICKING;
        try {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, FinishPickingFragment.newInstance(), FinishPickingFragment.class.getSimpleName())
                .commitNow();
        } catch (Exception e) {
            FL.e(TAG, e.getMessage());
        }

    }
    @Override
    public void onBackPressed() {
        getDump();
        if (getSupportFragmentManager().findFragmentByTag(TimingFragment.class.getSimpleName()) != null) {
            Log.d(TAG, TimingFragment.class.getSimpleName() + " is on");
            backPressed = false;
            refreshData();
            gotoMainFragment(App.getWorkZones());
        } else if (getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName()) != null) {
            Log.d(TAG, MainFragment.class.getSimpleName() + " is on");
            if (backPressed) {
                super.onBackPressed();
                if(pickedRowsForDelete != null) {
                    deletePickedRows();
                }
                Database.clearSkipped();
                Database.purgeGoodsMovements();
                System.exit(0);
            } else {
                if(!this.isFinishing()) {
                    Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_LONG).show();
                    say(getResources().getString(R.string.confirm_exit));
                }
                backPressed = true;
            }
        } else if (getSupportFragmentManager().findFragmentByTag(GoodsPickingFragment.class.getSimpleName()) != null) {
            Log.d(TAG, GoodsPickingFragment.class.getSimpleName() + " is on");
            backPressed = false;
            if(photoShown) {
                photoShown = false;
                gpf.closePhoto();
            } else {
                say(getResources().getString(R.string.dump_for_packing));
            }
        } else if (getSupportFragmentManager().findFragmentByTag(StartFragment.class.getSimpleName()) != null) {
            Log.d(TAG, StartFragment.class.getSimpleName() + " is on");
            if (backPressed) {
                if(pickedRowsForDelete != null) {
                    deletePickedRows();
                }
                super.onBackPressed();
                System.exit(0);
            } else {
                if(!this.isFinishing()) {
                    Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_LONG).show();
                    say(getResources().getString(R.string.confirm_exit));
                }
                backPressed = true;
            }
        } else if (getSupportFragmentManager().findFragmentByTag(FinishPickingFragment.class.getSimpleName()) != null) {
            Log.d(TAG, FinishPickingFragment.class.getSimpleName() + " is on");
            backPressed = false;
            say(getResources().getString(R.string.dump_for_packing));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        registerReceiver(networkChangeReceiver, intentFilter);
        if(cm.getActiveNetworkInfo() != null) {
            FL.d(TAG,"Network " + cm.getActiveNetworkInfo().getExtraInfo() + " " + cm.getActiveNetworkInfo().getDetailedState());
        }
        registerReceiver(barcodeDataReceiver, new IntentFilter(ACTION_BARCODE_DATA));
        Database.unlockGoodsMovements();
            FL.d(TAG, "State = " + App.state);
            switch (App.state) {
                case App.START:
                    gotoStartFragment();
                    break;
                case App.TIMINGS:
                    Log.d(TAG, "Resume timings fragment");
                    gotoTimingFragment(App.zonein);
                    break;
                case App.GOODSPICKING:
                    Log.d(TAG, "Resume picking fragment");
                    gotoPickingFragment(goodsMovements);
                    break;
                case App.MAINCYCLE:
                    FL.d(TAG, "Picked goods size =" + App.pickedGoods.size());
                    if(App.pickedGoods != null && App.pickedGoods.size() > 0) {
                        FL.d(TAG, "Resume Picked fragment");
                        gotoPickedFragment(App.pickedGoods);
                    } else {
                        FL.d(TAG, "Resume Main fragment");
                        Database.purgeGoodsMovements();
                        gotoMainFragment(App.getWorkZones());
                    }
                    break;
                case App.FINISHPICKING:
                    Log.d(TAG, "Resume FinishPicking fragment");
                    gotoFinishPickingFragment();
                    break;
                default:
                    gotoStartFragment();
                    break;
            }
    }
    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String phrase;
            if(cm.getActiveNetworkInfo() != null) {
                FL.d(TAG,"Network " + cm.getActiveNetworkInfo().getExtraInfo() + " " + cm.getActiveNetworkInfo().getDetailedState());
                phrase = "wifi подключен";
                online = true;
                if (mTimer != null) {
                    mTimer.cancel();
                }
            } else {
                FL.d(TAG, "Network disconnected");
                phrase = "wifi отключен";
                online = false;
                mTimer = new Timer();
                offlineTimerTask = new OfflineTimerTask();
                mTimer.schedule(offlineTimerTask, Config.offlineTimeout);
            }
            if(!((Activity) mContext).isFinishing())
            Toast.makeText(context, phrase, Toast.LENGTH_LONG).show();
        }
    };
    class OfflineTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(uploadAlert);
        }
    }
    Runnable uploadAlert = new Runnable() {
        @Override
        public void run() {
            adbUpload.create().show();
            say(getResources().getString(R.string.force_upload));
        }
    };
    public void gotoPickingFragment(ArrayList<GoodsMovement> goodsMovements) {
        App.state = App.GOODSPICKING;
        if(gpf == null) gpf =  GoodsPickingFragment.newInstance(goodsMovements);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, gpf, GoodsPickingFragment.class.getSimpleName())
                .commitNow();
    }
    public void dumpIntoCell(Cell cell) {
        FL.d(TAG, "Dump into cell " + cell.descr);
        GoodsMovement[] goodsForDump = /* Database.goodsToUpload(cell.id) */ Database.goodsToDump(cell.id);
        GoodsMovementResult gmr;
        String dumpGoodsURL;
        postWS = new PostWebservice();
        Gson gson = new Gson();
        if(goodsForDump != null && goodsForDump.length > 0) {
            gmr = new GoodsMovementResult(true, goodsForDump.length, App.getStoreMan());
            gmr.goodsMovements = goodsForDump;
            FL.d(TAG, "Prepared " + goodsForDump.length + " positions");
            try {
                uri = new URI(
                        Config.scheme, null, Config.host, Config.port,
                        Config.goodsPath + App.getStoreId() + "/" + App.getStoreMan() + "/",
                        null, null);
                dumpGoodsURL = uri.toASCIIString();
                try {
                    postWS.type = POSTGOODS;
                    postWS.post(dumpGoodsURL, gson.toJson(gmr));
                    FL.d(TAG, "Dump posted " + dumpGoodsURL);
                } catch (IOException e) {
                    FL.d(TAG, "Dump error " + e.getMessage());
                }
            } catch (URISyntaxException e) {
                FL.d(TAG, "URI error " + e.getMessage());
            }
            Database.clearSkipped();
        }
    }
    public void clearSelectedGoods() {
        selectedGoods = null;
    }
/*    public void beep() {
        soundPool.play(soundId1, 1, 1, 0, 0, 1);
    }

 */

    public static boolean inSelectedZones(String cellId) {
        boolean ret = false;
        for (Zone wz : App.getWorkZones()) {
            for (int i = 0; i < App.cells.length; i++) {
                if (wz.zonein.equals(App.cells[i].zonein) && cellId.equals(App.cells[i].id)) {
                    ret = true;
                    break;
                }
                if(ret) break;
            }
        }
        return ret;
    }
    public void dumpDeficiency() {
        FL.d(TAG, "Dump deficiency");
        GoodsMovement[] deficiencyForDump = Database.deficiencyToUpload();
        GoodsMovementResult gmr;
        String dumpGoodsURL;
        postWS = new PostWebservice();
        Gson gson = new Gson();
        if(deficiencyForDump != null && deficiencyForDump.length > 0) {
            gmr = new GoodsMovementResult(true, deficiencyForDump.length, App.getStoreMan());
            gmr.goodsMovements = deficiencyForDump;
            try {
                uri = new URI(
                        Config.scheme, null, Config.host, Config.port,
                        Config.goodsPath + App.getStoreId() + "/" + App.getStoreMan() + "/",
                        null, null);
                dumpGoodsURL = uri.toASCIIString();
                try {
                    postWS.type = POSTDEFICIENCY;
                    postWS.post(dumpGoodsURL, gson.toJson(gmr));
//                    uploadDeficiency = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
    public class GetWSDump {
        OkHttpClient client;
        String TAG = "GetWSDump";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            client = getHTTPClient(url);
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    DumpResult dumpResult = gson.fromJson(resp, DumpResult.class);
                    if (dumpResult != null) {
                        Log.d(TAG, "Result = " + dumpResult.success + " length = " + dumpResult.counter);
                        if (dumpResult.counter > 0) {
                            if (pickedRowsForDelete == null) pickedRowsForDelete = new ArrayList<>();
//                            Database.beginTr();
                            for (int i = 0; i < dumpResult.counter; i++) {
                                FL.d(TAG, "Mark for delete row " + dumpResult.rows[i]);
                                pickedRowsForDelete.add(dumpResult.rows[i]);
//                                Database.deletePickedGoods(dumpResult.rows[i]);
                            }
//                            Database.endTr();
//                            clearSelectedGoods();
                        }
                    }
                }
                public void onFailure(Call call, IOException e) {
                    FL.d(TAG, e.getMessage());
                }
            });
        }
    }
    private void getDump() {
        getWSDump = new GetWSDump();
        try{
            getWSDump.run(dumpURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class PrintLabelTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            printer = connect();
            for (int j = 0; j < labNum; j++) {
                sendLabel();
            }
            disconnect();
            return null;
        }
    }

    class TestPrintTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            printer = connect();
            sendLabel(getTestLabel());
            disconnect();
            return null;
        }
    }

    void writeFileSD(String line) {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "SDcard not available: " + Environment.getExternalStorageState());
            return;
        }
        File sdPath = null;
        File[] listExternalDirs = ContextCompat.getExternalFilesDirs(this, null);
        for(File f : listExternalDirs) {
//            Log.d(TAG,"External dir " + f.getAbsolutePath() + " emulated " + isExternalStorageEmulated(f));
            if(!isExternalStorageEmulated(f)) {
                sdPath = f;
                break;
            }
        }
        if (sdPath == null) {
            if(!this.isFinishing())
            Toast.makeText(this,getResources().getString(R.string.no_sd_card),Toast.LENGTH_SHORT).show();
            for(File f : listExternalDirs) {
                if(f.getAbsolutePath().contains("emulated")) {
                    sdPath = f;
                    break;
                }
            }
        }
        if (sdPath != null) {
            sdPath.mkdirs();
            File sdFile = new File(sdPath, filenameSD);
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile, true));
                bw.write(line + "\n\r");
                bw.close();
                Log.d(TAG, "File is written out: " + sdFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                if(!this.isFinishing())
                Toast.makeText(this,getResources().getString(R.string.sd_card_error),Toast.LENGTH_SHORT).show();
            }
        } else {
            if(!this.isFinishing())
            Toast.makeText(this,getResources().getString(R.string.sd_card_error),Toast.LENGTH_SHORT).show();
        }
    }

    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Yekaterinburg"));
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    private OkHttpClient getHTTPClient(String url) {
        OkHttpClient client = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            InputStream clientCertificateContent = getResources().openRawResource(R.raw.terminal);
            keyStore.load(clientCertificateContent, "".toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "".toCharArray());
            InputStream myTrustedCAFileContent = getResources().openRawResource(R.raw.chaincert);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate myCAPublicKey = (X509Certificate) certificateFactory.generateCertificate(myTrustedCAFileContent);
            KeyStore trustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustedStore.load(null);
            trustedStore.setCertificateEntry(myCAPublicKey.getSubjectX500Principal().getName(), myCAPublicKey);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustedStore);
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) throws
                                CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance(SSL);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            if (url.contains("https:")) {
                client = new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        })
                        .build();
            } else client = new OkHttpClient();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
        }
        return client;
    }
}