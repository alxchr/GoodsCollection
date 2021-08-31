package ru.abch.goodscollection;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bosphere.filelogger.FL;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PickedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PickedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    /*
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
*/
    Button btDump, btDelete;
    ListView lvGoodsList;
    ArrayList<GoodsMovement> pickedGoods;
    PickedAdapter pickedAdapter;
    String TAG = "PickedFragment";
    EditText etDumpCell;
    Cell dumpCell = null;
    public PickedFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static PickedFragment newInstance(ArrayList<GoodsMovement> pickedGoods) {
        PickedFragment fragment = new PickedFragment();
        /*
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

         */
        fragment.pickedGoods = pickedGoods;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

         */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_picked, container, false);
        btDelete = view.findViewById(R.id.bt_delete);
        btDump = view.findViewById(R.id.bt_dump);
        lvGoodsList = view.findViewById(R.id.lv_goodslist);
        btDelete.setOnClickListener(view1 -> {
            Log.d(TAG, "Delete pressed");
            App.clearPickedGoods();
            Database.clearPicked();
            ((MainActivity) getActivity()).gotoMainFragment(App.getWorkZones());
        });
        btDump.setOnClickListener(view12 -> {
            Log.d(TAG, "Dump pressed");
            App.zonein = pickedGoods.get(0).zonein;
            etDumpCell.setEnabled(true);
            etDumpCell.requestFocus();
        });
        etDumpCell = view.findViewById(R.id.et_dump_cell);
        etDumpCell.addTextChangedListener(new TextWatcher() {
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
                if(editable.length() > 2) {
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
                            result = String.format("%02d",prefix) + String.format("%03d",suffix);
                            Log.d(TAG, "Cell name " + result);
                            dumpCell = Database.getCellByName(result);
                            if(dumpCell != null /* && dumpCell.zonein.equals(App.zonein) */) {
                                Log.d(TAG, "Found dump cell " + dumpCell.descr);
                                etDumpCell.setEnabled(false);
                                etDumpCell.setText(dumpCell.descr);
                                ((MainActivity) getActivity()).dumpIntoCell(dumpCell);
                                ((MainActivity) getActivity()).gotoMainFragment(App.getWorkZones());
                            } else {
                                Log.d(TAG, "Illegal cell name " + result);
                                MainActivity.say(getResources().getString(R.string.enter_again));
                            }
                        } else {
                            MainActivity.say(getResources().getString(R.string.enter_again));
                        }
                        etDumpCell.setText("");
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        pickedAdapter = new PickedAdapter(getActivity(), pickedGoods);
        Log.d(TAG,"Picked goods size " + pickedGoods.size());
        lvGoodsList.setAdapter(pickedAdapter);
        etDumpCell.setEnabled(false);
    }
    public void dumpCellScanned(String code) {
        String result = Config.getCellName(code);
        FL.d(TAG, "Scanned cell, code " + code + " name " + result);
        if (result == null) {
            dumpCell = null;
        } else {
            dumpCell = Database.getCellByName(result);
        }
        Log.d(TAG, "Dump cell " + dumpCell);
        if(dumpCell != null /* && dumpCell.zonein.equals(App.zonein) */) {
            Log.d(TAG, "Found cell " + dumpCell.descr);
            etDumpCell.setEnabled(false);
            etDumpCell.setText(dumpCell.descr);
            ((MainActivity) getActivity()).dumpIntoCell(dumpCell);
            ((MainActivity) getActivity()).gotoMainFragment(App.getWorkZones());
        } else {
            Log.d(TAG, "Illegal cell name " + result);
            MainActivity.say(getResources().getString(R.string.enter_again));
        }
    }
}