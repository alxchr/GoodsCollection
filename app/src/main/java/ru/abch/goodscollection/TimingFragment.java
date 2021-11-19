package ru.abch.goodscollection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bosphere.filelogger.FL;

import java.util.ArrayList;

public class TimingFragment extends Fragment {

    private TimingViewModel mViewModel;
//    int iddocdef;
    String zonein;
    ArrayList<Timing> timings;
    ListView lvTiming;
    TimingAdapter timingAdapter;
    EditText etStartCell;
    Button btStart;
    String TAG = TimingFragment.class.getSimpleName();
    Cell startCell = null;
//    ArrayList<GoodsMovement> goodsMovements;
    ProgressBar pbBar;
    /*
    public static TimingFragment newInstance(int iddocdef) {
        TimingFragment tf = new TimingFragment();
        tf.iddocdef = iddocdef;
        return tf;
    }

     */
    public static TimingFragment newInstance(String zonein) {
        TimingFragment tf = new TimingFragment();
        tf.zonein = zonein;
        return tf;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Zone " + zonein);
        timings = ((MainActivity) getActivity()).getTimings(zonein);
//        goodsMovements = ((MainActivity) getActivity()).goodsMovements;
        if(timings == null) Log.e(TAG,"timings is null");
        if(((MainActivity) getActivity()).goodsMovements == null) Log.e(TAG,"goodsmovements is null");
        if(timings != null && ((MainActivity) getActivity()).goodsMovements != null) {
            FL.d(TAG, "Zone " + zonein + " goodsMovements size = " + ((MainActivity) getActivity()).goodsMovements.size() +
                    " timings size = " + timings.size());
        }
        if(timings != null && timings.size() > 0) {
            timingAdapter = new TimingAdapter(getActivity(), timings);
            lvTiming.setAdapter(timingAdapter);
        }
        btStart.setOnClickListener(view -> {
            Log.d(TAG,"Start clicked");
//            etStartCell.setText("");
            ((MainActivity) getActivity()).gotoPickingFragment(((MainActivity) getActivity()).goodsMovements);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        timings = ((MainActivity) getActivity()).getTimings(zonein);
//        goodsMovements = ((MainActivity) getActivity()).goodsMovements;
        if(timings != null && ((MainActivity) getActivity()).goodsMovements != null) {
            FL.d(TAG, "Zone " + zonein + " goodsMovements size = " +
                    ((MainActivity) getActivity()).goodsMovements.size() + " timings size = " + timings.size());
        }
        if(timings != null && timings.size() > 0) {
            timingAdapter = new TimingAdapter(getActivity(), timings);
            lvTiming.setAdapter(timingAdapter);
            App.currentTiming = timings.get(0);
        }
        btStart.setOnClickListener(view -> {
            Log.d(TAG,"Start clicked");
            ((MainActivity) getActivity()).gotoPickingFragment(((MainActivity) getActivity()).goodsMovements);
        });
        MainActivity.say(getResources().getString(R.string.start_cell));
        etStartCell.setText("");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.timing_fragment, container, false);
        lvTiming = view.findViewById(R.id.lv_timing);
        etStartCell = view.findViewById(R.id.et_start_cell);
        btStart = view.findViewById(R.id.bt_start);
        pbBar = view.findViewById(R.id.pbbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TimingViewModel.class);
        // TODO: Use the ViewModel
        btStart.setEnabled(false);
        etStartCell.requestFocus();
        etStartCell.addTextChangedListener(new TextWatcher() {
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
//                    Log.d(TAG, "After text changed =" + editable.toString());
                    if (input.contains("\n") && input.indexOf("\n") == 0) {
                        input = input.substring(1);
//                        Log.d(TAG, "Enter char begins string =" + input);
                    }
                    if (input.contains("\n") && input.indexOf("\n") > 0 && !input.contains("-")) {
                        entIndex = input.indexOf("\n");
                        input = input.substring(0, entIndex);
//                        Log.d(TAG, "Enter at " + entIndex + " position of input =" + input);
                        if (CheckCode.checkCellStr(input)) {
                            prefix = Integer.parseInt(input.substring(0, input.indexOf(".")));
                            suffix = Integer.parseInt(input.substring(input.indexOf(".") + 1));
                            result = String.format("%02d", prefix) + String.format("%03d", suffix);
                            Log.d(TAG, "Cell name " + result);
                            startCell = Database.getCellByName(result);
                            if (startCell != null) {
                                App.currentDistance = startCell.distance;
                                Log.d(TAG, "Found cell " + startCell.descr + " distance " + App.currentDistance);
                                etStartCell.setEnabled(false);
//                                btStart.setEnabled(true);
                                etStartCell.setText(startCell.descr);
                                timings = ((MainActivity) getActivity()).getTimings(zonein);
                                if(((MainActivity) getActivity()).goodsMovements != null) {
                                    SortTask st = new SortTask();
                                    st.execute(((MainActivity) getActivity()).goodsMovements);
                                } else {
                                    Log.e(TAG, "goodsMovements is null");
                                }
                            } else {
                                Log.d(TAG, "Cell name " + result + " not found");
                                MainActivity.say(getResources().getString(R.string.enter_again));
                                etStartCell.setText("");
                            }
                        } else {
                            MainActivity.say(getResources().getString(R.string.enter_again));
                            etStartCell.setText("");
                        }

                    }
                }
            }
        });

    }
    public void startCellScanned(String code) {
        String result = Config.getCellName(code);
        FL.d(TAG, "Scanned cell, code " + code + " name " + result);
        if(result == null) {
            startCell = null;
        } else {
            startCell = Database.getCellByName(result);
        }
        if(startCell != null) {
            Log.d(TAG, "Found cell " + startCell.descr + " distance " + startCell.distance);
            etStartCell.setEnabled(false);
            App.currentDistance = startCell.distance;
//            btStart.setEnabled(true);
            etStartCell.setText(startCell.descr);
            timings = ((MainActivity) getActivity()).getTimings(zonein);
            if(((MainActivity) getActivity()).goodsMovements != null) {
                SortTask st = new SortTask();
                st.execute(((MainActivity) getActivity()).goodsMovements);
            } else {
                Log.e(TAG, "goodsMovements is null");
            }
        } else {
            Log.d(TAG, "Cell name " + result + " not found");
            MainActivity.say(getResources().getString(R.string.enter_again));
        }
    }

    private class SortTask extends AsyncTask<ArrayList<GoodsMovement>, Void, Void> {
        @Override
        protected Void doInBackground(ArrayList<GoodsMovement>... goodsMovements) {
//            FL.d(TAG, "Sort doInBackground");
            ArrayList<GoodsMovement> gms = goodsMovements[0];
            for (int i = 0; i < gms.size(); i++) {
                gms.get(i).distance = Database.getCellById(gms.get(i).cellOut).distance;
            }
            gms.sort((lhs, rhs) -> {
                int ret = 0;
                if (lhs != null && rhs != null) {
                    int leftDistance = lhs.getDistance(startCell.distance);
                    int rightDistance = rhs.getDistance(startCell.distance);
                    ret = Integer.compare(leftDistance, rightDistance);
                }
                return ret;
            });
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btStart.setVisibility(View.GONE);
            pbBar.setVisibility(View.VISIBLE);
//            FL.d(TAG, "Sort onPreExecute");
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pbBar.setVisibility(View.GONE);
            btStart.setVisibility(View.VISIBLE);
            btStart.setEnabled(true);
//            FL.d(TAG, "Sort onPostExecute");
        }
    }
}