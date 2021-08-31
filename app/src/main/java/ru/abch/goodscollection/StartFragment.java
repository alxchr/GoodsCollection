package ru.abch.goodscollection;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bosphere.filelogger.FL;

import java.util.ArrayList;

import ru.abch.goodscollection.ui.main.MainViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "StartFragment";
    EditText etStoreman;
    String sStoreMan;
    static int storeMan = -1;
    ListView lvWorkZones;
    WorkZoneAdapter workZoneAdapter;
    Button btStart1;
    ArrayList<Zone> workZones, selectedWorkZones;
    public StartFragment() {
        // Required empty public constructor
    }


    public static StartFragment newInstance() {
        StartFragment fragment = new StartFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        lvWorkZones = view.findViewById(R.id.lv_work_zones);
        etStoreman = view.findViewById(R.id.et_storeman);
        btStart1 = view.findViewById(R.id.bt_start1);
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
        etStoreman.setText(String.valueOf(App.getStoreMan()));
        etStoreman.requestFocus();
        workZones = Database.getZones(App.ZONE_PICK);
        workZoneAdapter = new WorkZoneAdapter(getActivity(), workZones);
        lvWorkZones.setAdapter(workZoneAdapter);
        btStart1.setOnClickListener(view -> {
            sStoreMan = etStoreman.getText().toString();
            selectedWorkZones = new ArrayList<>();
            for (Zone wz : workZones) {
                Log.d(TAG,"Workzone " + wz.zonein + " " + wz.zonein_descr + " " + wz.checked);
                if(wz.checked) {
                    selectedWorkZones.add(wz);
                }
            }
            if(selectedWorkZones.isEmpty()) {
                Log.d(TAG,"Nothing selected");
                MainActivity.say(getResources().getString(R.string.storeman_number_tts));
            } else {
                Log.d(TAG,"Workzones selected # " + selectedWorkZones.size());
                App.setWorkZones(selectedWorkZones);
                try {
                    storeMan = Integer.parseInt(sStoreMan);
                    FL.d(TAG, "Storeman = " + storeMan);
                    App.setStoreMan(storeMan);
                    FL.d(TAG, "Picked goods size =" + App.pickedGoods.size());
                    if(App.pickedGoods != null && App.pickedGoods.size() > 0) {
                        FL.d(TAG, "goto Picked fragment");
                        ((MainActivity) getActivity()).gotoPickedFragment(App.pickedGoods);
                    } else {
                        ((MainActivity) getActivity()).refreshData();
                        ((MainActivity) getActivity()).gotoMainFragment(App.getWorkZones());
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    MainActivity.say(getResources().getString(R.string.storeman_number_tts));
                }
            }
        });
    }
}