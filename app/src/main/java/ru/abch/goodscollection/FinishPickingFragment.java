package ru.abch.goodscollection;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.bosphere.filelogger.FL;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FinishPickingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FinishPickingFragment extends Fragment {
    String TAG = FinishPickingFragment.class.getSimpleName();
    Button btContinue;
    EditText etDumpCell;
    Cell dumpCell;
    public FinishPickingFragment() {
        // Required empty public constructor
    }


    public static FinishPickingFragment newInstance() {
        FinishPickingFragment fragment = new FinishPickingFragment();

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
        View view = inflater.inflate(R.layout.fragment_finish_picking, container, false);
        btContinue = view.findViewById(R.id.bt_continue);
        etDumpCell = view.findViewById(R.id.et_dump_cell);
        etDumpCell.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btContinue.setEnabled(false);
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
                            if(dumpCell != null && dumpCell.zonein.equals(App.zonein)) {
                                Log.d(TAG, "Found dump cell " + dumpCell.descr);
                                etDumpCell.setEnabled(false);
                                etDumpCell.setText(dumpCell.descr);
                                ((MainActivity) getActivity()).dumpIntoCell(dumpCell);
                                ((MainActivity) getActivity()).gotoMainFragment(App.getWorkZones());
                            } else {
                                etDumpCell.setText("");
                                FL.d(TAG, "Illegal cell name " + result);
                                MainActivity.say(getResources().getString(R.string.enter_again));
                            }
                        } else {
                            MainActivity.say(getResources().getString(R.string.enter_again));
                            etDumpCell.setText("");
                        }
                        etDumpCell.setText("");
                    }
                }
            }
        });
        btContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).clearSelectedGoods();
                Database.clearSkipped();
                ArrayList<Timing> timings = ((MainActivity) getActivity()).getTimings(App.zonein);
                FL.d(TAG, "Continue picking at " + App.zonein + " timings size " + timings.size() + " goods movements size "
                        + ((MainActivity)getActivity()).goodsMovements.size());
                ((MainActivity) getActivity()).gotoTimingFragment(App.zonein);
            }
        });
        return view;
    }
    public void dumpCellScanned(String code) {
        String result = Config.getCellName(code);
        FL.d(TAG, "Scanned cell, code " + code + " name " + result);
        if (result == null) {
            dumpCell = null;
        } else {
            dumpCell = Database.getCellByName(result);
        }
        if(dumpCell != null && dumpCell.zonein.equals(App.zonein)) {
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

    @Override
    public void onResume() {
        super.onResume();
        etDumpCell.requestFocus();
        String finishPickingTTS = getResources().getString(R.string.finish_picking_tts1) + App.currentTiming.name
                + getResources().getString(R.string.finish_picking_tts2);
        MainActivity.say(finishPickingTTS);
        if(Database.countPositions(App.zonein) > 0) {
            btContinue.setEnabled(true);
        } else {
            btContinue.setEnabled(false);
        }
    }
}