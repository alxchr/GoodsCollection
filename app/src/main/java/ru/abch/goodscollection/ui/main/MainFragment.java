package ru.abch.goodscollection.ui.main;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bosphere.filelogger.FL;

import java.util.ArrayList;

import ru.abch.goodscollection.App;
import ru.abch.goodscollection.Client;
import ru.abch.goodscollection.Config;
import ru.abch.goodscollection.Database;
import ru.abch.goodscollection.MainActivity;
import ru.abch.goodscollection.R;
import ru.abch.goodscollection.WorkZoneAdapter;
import ru.abch.goodscollection.Zone;
import ru.abch.goodscollection.ZoneAdapter;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private MainViewModel mViewModel;
    ListView lvButtons;
    ZoneAdapter zoneAdapter;
    ArrayList<Zone> workZones, allZones, selected;
    ProgressBar pbBar;
    Spinner spClients;
    Client[] clients;
    String[] clientIds, clientNames;
    public ArrayAdapter<String> spinnerAdapter;
    int nClients;
    public static MainFragment newInstance(ArrayList<Zone> workZones) {
        MainFragment mf;
        mf = new MainFragment();
        mf.workZones = workZones;
        return mf;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        lvButtons = view.findViewById(R.id.lv_buttons);
        pbBar = view.findViewById(R.id.pbbar);
        spClients = view.findViewById(R.id.sp_clients);
        return view;
    }
    public void buildClientsArray() {
        clients = Database.getClientArray();
        nClients = 0;
        if (clients != null && clients.length > 0) {
            nClients += clients.length;
        }
        Log.d(TAG, "# clients" + nClients);
        clientIds = new String[nClients + 1];
        clientNames = new String[nClients + 1];
        clientIds[0] = "";
        clientNames[0] = getResources().getString(R.string.all_clients);
        for (int i = 0; i < nClients; i++) {
            clientIds[i+1] = clients[i].clientId;
            clientNames[i+1] = clients[i].clientDescription;
        }
        spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, clientNames);
//        spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, clientNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spClients.setAdapter(spinnerAdapter);
        spClients.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Client # " + position);
//                ((TextView) parent.getChildAt(0)).setTextSize(24);
                if(position == 0) {
                    MainActivity.clientId = null;
                } else {
                    MainActivity.clientId = clientIds[position];
                    Database.debugClient(MainActivity.clientId);
                }
                zoneAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                zoneAdapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
        allZones = Database.getZones(App.ZONE_DUMP);
        zoneAdapter = new ZoneAdapter(getActivity(), Database.getZones(App.ZONE_DUMP));
        lvButtons.setAdapter(zoneAdapter);
//        pbBar.setVisibility(View.VISIBLE);
//        lvButtons.setVisibility(View.GONE);
        buildClientsArray();
        /*
        spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, clientNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spClients.setAdapter(spinnerAdapter);
        spClients.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Client # " + position);
                if(position == 0) {
                    MainActivity.clientId = null;
                } else {
                    MainActivity.clientId = clientIds[position];
                    Database.debugClient(MainActivity.clientId);
                }
                zoneAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                zoneAdapter.notifyDataSetChanged();
            }
        });
*/
    }
    public void setButtons() {
        zoneAdapter.notifyDataSetChanged();
    }
    public void enableButtons(boolean enabled) {

        if (enabled) {
            pbBar.setVisibility(View.GONE);
            lvButtons.setVisibility(View.VISIBLE);
        } else {
            pbBar.setVisibility(View.VISIBLE);
            lvButtons.setVisibility(View.GONE);
        }
    }
}