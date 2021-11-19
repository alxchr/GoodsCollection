package ru.abch.goodscollection;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bosphere.filelogger.FL;

import java.util.ArrayList;

public class GoodsPickingFragment extends Fragment {

    private GoodsPickingViewModel mViewModel;
    TextView tvGoodsDesc, tvPickingTitle, tvTotal, tvArticle, tvBrand, labelTotal, labelPicked, tvCell;
    EditText etPickQty;
    ArrayList<GoodsMovement> goodsMovements, collectedGoods;
    GoodsMovement currentGoods = null;
    Button btSkip, btBreak, btDeficiency;
    boolean pickClicked = false;
    int  total, subtotal, qty;
    String TAG = GoodsPickingFragment.class.getSimpleName();
    AlertDialog.Builder adbGoods, adbSkip, adbBreak, adbDeficiency;
    WebView photoView;
    LinearLayout llButtons, llTotal, llQty, llCell;
    ProgressBar pbBar;
    public static GoodsPickingFragment newInstance(ArrayList<GoodsMovement> goodsMovements) {
        GoodsPickingFragment pf = new GoodsPickingFragment();
        pf.goodsMovements = goodsMovements;
        return pf;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.goods_picking_fragment, container, false);
        tvGoodsDesc = view.findViewById(R.id.tv_goods_desc);
        tvCell = view.findViewById(R.id.tv_cell);
        etPickQty = view.findViewById(R.id.et_pick_qty);
        etPickQty.setEnabled(false);
        btBreak = view.findViewById(R.id.bt_break);
        btSkip = view.findViewById(R.id.bt_skip);
        btSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adbSkip = new AlertDialog.Builder(getActivity());
                adbSkip.setMessage(R.string.confirm_skip);
                adbSkip.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        FL.d(TAG, "Skip negative button click");
                    }
                });
                adbSkip.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        if(goodsMovements != null && !goodsMovements.isEmpty()) {
                            if (subtotal > 0) {
                                FL.d(TAG,"Skip with subtotal " + subtotal + " goods " + currentGoods.goods_descr);
                                Database.setPick(currentGoods.goods, currentGoods.mdoc,
                                        currentGoods.cellOut_task, currentGoods.cellIn_task, subtotal);
                                Database.addPickedGoods(currentGoods.goods,
                                        currentGoods.mdoc,
                                        currentGoods.cellOut_task,
                                        currentGoods.cellIn_task,
                                        subtotal,
                                        currentGoods.goods_descr,
                                        currentGoods.units,
                                        currentGoods.goods_article);
                            }
                            Database.addSkippedGoods(currentGoods.goods,
                                    currentGoods.mdoc,
                                    currentGoods.cellOut_task,
                                    currentGoods.cellIn_task,
                                    total - subtotal,
                                    currentGoods.goods_descr,
                                    currentGoods.units);
                            App.currentDistance = goodsMovements.get(0).distance;
                            Database.setLock(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task, 0);
                            goodsMovements.remove(0);
                            goodsMovements.sort((lhs, rhs) -> {
                                int ret = 0;
                                if (lhs != null && rhs != null) {
                                    int leftDistance = lhs.getDistance(App.currentDistance);
                                    int rightDistance = rhs.getDistance(App.currentDistance);
                                    ret = Integer.compare(leftDistance, rightDistance);
                                }
                                return ret;
                            });
                        }
                        pickClicked = false;
                        nextGoods();
                        FL.d(TAG, "Skip positive button click");
                    }
                });
                adbSkip.setCancelable(false);
                adbSkip.create().show();
            }
        });
        tvPickingTitle = view.findViewById(R.id.tv_picking_title);
        btBreak.setOnClickListener(view1 -> {
            Log.d(TAG, "Break clicked");
            adbBreak= new AlertDialog.Builder(getActivity());
            adbBreak.setMessage(R.string.confirm_finish_picking);
            adbBreak.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    FL.d(TAG, "Break negative button click");
                }
            });
            adbBreak.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
//                        MainActivity.say(getResources().getString(R.string.finish_picking));
                    if (subtotal > 0) {
                        Database.addPickedGoods(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task,
                                subtotal, currentGoods.goods_descr, currentGoods.units, currentGoods.goods_article);
                    }
                    goodsMovements.clear();
                    Database.unlockGoodsMovements();
                    FL.d(TAG, "Break positive button click");
                    ((MainActivity) getActivity()).gotoFinishPickingFragment();
                }
            });
            adbBreak.setCancelable(false);
            adbBreak.create().show();
        });
        tvTotal = view.findViewById(R.id.tv_goods_total);
        etPickQty.setOnKeyListener((view12, i, keyEvent) -> {
            if(keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                    (i == KeyEvent.KEYCODE_ENTER)){
                String sQty = ((EditText) view12).getText().toString();
                if (sQty.contains("\n") && sQty.indexOf("\n") == 0) {
                    sQty = sQty.substring(1);
                }
                try {
                    int qnt = Integer.parseInt(sQty);
                    qty = qnt;
                    subtotal += qnt;
                    FL.d(TAG, "input qty = " + qty);
                    if(subtotal < total) {
                        String remain = String.valueOf(total - subtotal);// + getResources().getString(R.string.remain_of) + total;
                        tvTotal.setText(remain);
                        etPickQty.setText("");
                        Database.addPickedGoods(currentGoods.goods,
                                currentGoods.mdoc,
                                currentGoods.cellOut_task,
                                currentGoods.cellIn_task,
                                qty,
                                currentGoods.goods_descr,
                                currentGoods.units,
                                currentGoods.goods_article);
                    } else
                    if (subtotal == total) {
                        Database.setPick(currentGoods.goods, currentGoods.mdoc,
                                currentGoods.cellOut_task, currentGoods.cellIn_task, subtotal);
                        Database.addPickedGoods(currentGoods.goods,
                                currentGoods.mdoc,
                                currentGoods.cellOut_task,
                                currentGoods.cellIn_task,
//                                    subtotal,
                                qty,
                                currentGoods.goods_descr,
                                currentGoods.units,
                                currentGoods.goods_article);
                        App.currentDistance = goodsMovements.get(0).distance;
                        Database.setLock(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task, 0);
                        goodsMovements.remove(0);
                        goodsMovements.sort((lhs, rhs) -> {
                            int ret = 0;
                            if (lhs != null && rhs != null) {
                                int leftDistance = lhs.getDistance(App.currentDistance);
                                int rightDistance = rhs.getDistance(App.currentDistance);
                                ret = Integer.compare(leftDistance, rightDistance);
                            }
                            return ret;
                        });
//                            ((MainActivity) getActivity()).beep();
                        MainActivity.say(getResources().getString(R.string.picked_tts));
                        nextGoods();
                    } else {
                        etPickQty.setText(String.valueOf(subtotal));
                        adbGoods = new AlertDialog.Builder(getActivity());
                        FL.d(TAG, "Subtotal = " + subtotal + " total = " + total);
                        MainActivity.say(getResources().getString(R.string.excessive_goods));
                        adbGoods.setMessage(R.string.confirm_excessive_goods);
                        adbGoods.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                subtotal -= qnt;
                                etPickQty.setText("");
                                etPickQty.requestFocus();
                                etPickQty.setSelection(etPickQty.getText().length());
                                FL.d(TAG, "Excessive goods negative button click");
                            }
                        });
                        adbGoods.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                Database.setPick(currentGoods.goods, currentGoods.mdoc,
                                        currentGoods.cellOut_task, currentGoods.cellIn_task, subtotal);
                                Database.addPickedGoods(currentGoods.goods,
                                        currentGoods.mdoc,
                                        currentGoods.cellOut_task,
                                        currentGoods.cellIn_task,
//                                            subtotal,
                                        qty,
                                        currentGoods.goods_descr,
                                        currentGoods.units,
                                        currentGoods.goods_article);
                                App.currentDistance = goodsMovements.get(0).distance;
                                Database.setLock(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task, 0);
                                goodsMovements.remove(0);
                                goodsMovements.sort((lhs, rhs) -> {
                                    int ret = 0;
                                    if (lhs != null && rhs != null) {
                                        int leftDistance = lhs.getDistance(App.currentDistance);
                                        int rightDistance = rhs.getDistance(App.currentDistance);
                                        ret = Integer.compare(leftDistance, rightDistance);
                                    }
                                    return ret;
                                });
//                                    ((MainActivity) getActivity()).beep();
                                MainActivity.say(getResources().getString(R.string.picked_tts));
                                nextGoods();
                                FL.d(TAG, "Excessive goods positive button click");
                            }
                        });
                        adbGoods.setCancelable(false);
                        adbGoods.create().show();
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    etPickQty.setText("");
                    MainActivity.say(getResources().getString(R.string.ent_number));
                }
            }
            return false;
        });
        tvArticle = view.findViewById(R.id.tv_article);
        tvBrand = view.findViewById(R.id.tv_brand);
        labelTotal = view.findViewById(R.id.label_total);
        labelPicked = view.findViewById(R.id.label_picked);
        btDeficiency = view.findViewById(R.id.bt_deficiency);
        btDeficiency.setOnClickListener(view13 -> {
            adbDeficiency= new AlertDialog.Builder(getActivity());
            adbDeficiency.setMessage(R.string.confirm_deficiency);
            adbDeficiency.setNegativeButton(R.string.no, (dialog, arg1) -> FL.d(TAG, "Deficiency negative button click"));
            adbDeficiency.setPositiveButton(R.string.yes, (dialog, arg1) -> {
                FL.d(TAG, "Deficiency " + (total - subtotal) + " total " + total + " subtotal " + subtotal);
                Database.addPickedGoods(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task,
                        subtotal - total, currentGoods.goods_descr, currentGoods.units, currentGoods.goods_article);
                ((MainActivity) getActivity()).dumpDeficiency();
                if(goodsMovements != null && !goodsMovements.isEmpty()) {
                    App.currentDistance = goodsMovements.get(0).distance;
                    Database.setLock(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task, 0);
                    goodsMovements.remove(0);
                    goodsMovements.sort((lhs, rhs) -> {
                        int ret = 0;
                        if (lhs != null && rhs != null) {
                            int leftDistance = lhs.getDistance(App.currentDistance);
                            int rightDistance = rhs.getDistance(App.currentDistance);
                            ret = Integer.compare(leftDistance, rightDistance);
                        }
                        return ret;
                    });
                }
                pickClicked = false;
                nextGoods();
                FL.d(TAG, "Deficiency positive button click");
            });
            adbDeficiency.setCancelable(false);
            adbDeficiency.create().show();
        });
        photoView = view.findViewById(R.id.photo_view);
        llButtons = view.findViewById(R.id.ll_buttons);
        llCell = view.findViewById(R.id.ll_cell);
        llQty = view.findViewById(R.id.ll_qty);
        llTotal = view.findViewById(R.id.ll_total);
        pbBar = view.findViewById(R.id.pbbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(GoodsPickingViewModel.class);
        // TODO: Use the ViewModel
    }


    @Override
    public void onStart() {
        super.onStart();
        collectedGoods = new ArrayList<>();
        if(goodsMovements != null) mViewModel.setGoodsMovements(goodsMovements);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).clearSelectedGoods();
        ArrayList<Timing> timings = ((MainActivity) getActivity()).getTimings(App.zonein);
        goodsMovements = ((MainActivity) getActivity()).goodsMovements;
        Log.d(TAG, "goodsMovements size " + goodsMovements.size());
        if(goodsMovements != null) {
            llButtons.setVisibility(View.GONE);
            pbBar.setVisibility(View.VISIBLE);
            for (int i = 0; i < goodsMovements.size(); i++) {
                goodsMovements.get(i).distance = Database.getCellById(goodsMovements.get(i).cellOut).distance;
//                FL.d(TAG +" before sort", "Cell " + goodsMovements.get(i).cellOut_descr +
//                        " distance " + goodsMovements.get(i).distance);
            }
            goodsMovements.sort((lhs, rhs) -> {
                int ret = 0;
                if (lhs != null && rhs != null) {
                    int leftDistance = lhs.getDistance(App.currentDistance);
                    int rightDistance = rhs.getDistance(App.currentDistance);
                    ret = Integer.compare(leftDistance, rightDistance);
                }
                return ret;
            });
            pbBar.setVisibility(View.GONE);
            llButtons.setVisibility(View.VISIBLE);
/*
            for(int i = 0; i < goodsMovements.size(); i++) {
                FL.d(TAG +" after sort", "Cell " + goodsMovements.get(i).cellOut_descr +
                        " distance " + goodsMovements.get(i).distance);
            }

 */
            nextGoods();
        } else {
            FL.d(TAG, "onResume goodsMovements is null");
        }
    }
    void nextGoods() {
        String urgentStr = (MainActivity.urgent)? getResources().getString(R.string.urgent) : "";
        String photoURL;
        if (MainActivity.updateGoodsMovements != null) {
            Database.clearGoodsMovements();
            // Delete picked here
            if(MainActivity.pickedRowsForDelete != null) {
                Database.beginTr();
                for (long rowId : MainActivity.pickedRowsForDelete) {
                    Database.deletePickedGoods(rowId);
                    FL.d(TAG, "Delete row " + rowId);
                }
                Database.endTr();
                MainActivity.pickedRowsForDelete = null;
            }
            if (MainActivity.updateGoodsMovements.length > 0) {
                Database.beginTr();
                FL.d(TAG, "Update DB " + MainActivity.updateGoodsMovements.length + " goods movements");
                for (int i = 0; i < MainActivity.updateGoodsMovements.length; i++) {
                    if (MainActivity.inSelectedZones(MainActivity.updateGoodsMovements[i].cellOut)) {
                        photoURL = MainActivity.updateGoodsMovements[i].url;
                        if (photoURL != null && photoURL.length() > 0 && photoURL.contains("\\")) {
                            photoURL = photoURL.replaceAll("\\\\", "/");
                        }
                        MainActivity.updateGoodsMovements[i].qnt -= Database.countPicked(
                                MainActivity.updateGoodsMovements[i].goods,
                                MainActivity.updateGoodsMovements[i].mdoc,
                                MainActivity.updateGoodsMovements[i].cellOut_task,
                                MainActivity.updateGoodsMovements[i].cellIn_task);
                        if (MainActivity.updateGoodsMovements[i].qnt > 0) {
                            Database.addGoodsMovement(
                                    MainActivity.updateGoodsMovements[i].goods,
                                    MainActivity.updateGoodsMovements[i].goods_descr,
                                    MainActivity.updateGoodsMovements[i].cellOut,
                                    MainActivity.updateGoodsMovements[i].cellOut_descr,
                                    MainActivity.updateGoodsMovements[i].cellIn,
                                    MainActivity.updateGoodsMovements[i].cellIn_descr,
                                    MainActivity.updateGoodsMovements[i].cellOut_task,
                                    MainActivity.updateGoodsMovements[i].cellIn_task,
                                    MainActivity.updateGoodsMovements[i].mdoc,
                                    MainActivity.updateGoodsMovements[i].iddocdef,
                                    MainActivity.updateGoodsMovements[i].qnt,
                                    MainActivity.updateGoodsMovements[i].startTime,
                                    MainActivity.updateGoodsMovements[i].goods_article,
                                    MainActivity.updateGoodsMovements[i].goods_brand,
                                    MainActivity.updateGoodsMovements[i].units,
                                    MainActivity.updateGoodsMovements[i].zonein,
                                    MainActivity.updateGoodsMovements[i].zonein_descr,
                                    photoURL,
                                    MainActivity.updateGoodsMovements[i].dest_id,
                                    MainActivity.updateGoodsMovements[i].dest_descr
                            );
                        } else {
                            FL.d(TAG, "Goods already picked " + MainActivity.updateGoodsMovements[i].goods_article + " qnt " +
                                    MainActivity.updateGoodsMovements[i].qnt);
                        }
                    }
                }
                Database.endTr();
                ArrayList<Timing> timings = ((MainActivity)getActivity()).getTimings(App.zonein);
                goodsMovements = ((MainActivity) getActivity()).goodsMovements;
            }
            MainActivity.updateGoodsMovements = null;
        }
        for (GoodsMovement gm : goodsMovements) {
            int nSkipped = Database.getSkipped(gm.goods, gm.mdoc, gm.cellOut_task, gm.cellIn_task);
            if (nSkipped > 0)
                FL.d(TAG, "Skipped " + gm.goods_article + " " + nSkipped + " mdoc " + gm.mdoc +
                        " cellOut " + gm.cellOut_descr + " cellIn " + gm.cellIn_descr);
            if (gm.qnt > nSkipped) {
                gm.qnt -= nSkipped;
            } else {
                goodsMovements.remove(gm);
                FL.d(TAG, "Remove " + gm.goods_article + " " + gm.qnt + " mdoc " + gm.mdoc +
                        " cellOut " + gm.cellOut_descr + " cellIn " + gm.cellIn_descr);
            }
        }
        if(!goodsMovements.isEmpty()) {
            tvPickingTitle.setText(getResources().getString(R.string.picking));
            currentGoods = goodsMovements.get(0);
            while (!goodsMovements.isEmpty() && Database.setLock(currentGoods.goods,
                    currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task, 1) == 0) {
                goodsMovements.remove(0);
                if (!goodsMovements.isEmpty()) currentGoods = goodsMovements.get(0);
            }
            if (!goodsMovements.isEmpty()) {
//                currentGoods = goodsMovements.get(0);
                MainActivity.currentGoods = currentGoods;
                FL.d(TAG, "Next goods " + currentGoods.goods_descr + " qnt " + currentGoods.qnt + " article " + currentGoods.goods_article +
                        " cell " + currentGoods.cellOut_descr + " time " + Config.comttTime(currentGoods.startTime));
                tvGoodsDesc.setText(currentGoods.goods_descr);
                tvCell.setText(currentGoods.cellOut_descr);
                tvCell.setTextColor(getResources().getColor(R.color.purple_500));
                MainActivity.say(urgentStr + getResources().getString(R.string.cell) + " " + currentGoods.cellOut_descr);
                total = currentGoods.qnt;
//            subtotal = 0;
                subtotal = Database.countPicked(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task);
                qty = 0;
                String remain = String.valueOf(total - subtotal);// + getResources().getString(R.string.remain_of) + total;
                tvTotal.setText(remain);
                etPickQty.setEnabled(true);
                etPickQty.requestFocus();
                etPickQty.setText("");
                etPickQty.setSelection(etPickQty.getText().length());
                tvArticle.setText(currentGoods.goods_article);
                tvBrand.setText(currentGoods.goods_brand);
                labelPicked.setVisibility(View.VISIBLE);
                labelPicked.setText(getResources().getString(R.string.picked) + currentGoods.units);
                labelTotal.setVisibility(View.VISIBLE);
                String labelRemain = getResources().getString(R.string.remain) + currentGoods.units;
                labelTotal.setText(labelRemain);
            } else {
                Log.d(TAG, "Goods list is empty, go to finish picking");
                Database.unlockGoodsMovements();
                ((MainActivity) getActivity()).gotoFinishPickingFragment();
            }
//            Database.setLock(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task, 1);
//            int nSkipped = Database.getSkipped(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task);

        } else {
            Log.d(TAG, "Goods list is empty, go to finish picking");
            Database.unlockGoodsMovements();
            ((MainActivity) getActivity()).gotoFinishPickingFragment();
        }
    }

    public void processScan(String code) {
        BarCode barCode;
        if(goodsMovements == null || goodsMovements.isEmpty()) {
            Database.unlockGoodsMovements();
            ((MainActivity) getActivity()).gotoFinishPickingFragment();
        } else {
            barCode = Database.getBarCode(code);
            if(barCode != null && barCode.goods.equals(currentGoods.goods)) {
                qty += barCode.qnt;
//                String toastBarcode = barCode.barcode + " " + currentGoods.goods_descr + " " + barCode.qnt;
//                Toast.makeText(getContext(), toastBarcode, Toast.LENGTH_SHORT).show();
                if (qty + subtotal == total) {
                    Database.setPick(currentGoods.goods, currentGoods.mdoc,
                            currentGoods.cellOut_task, currentGoods.cellIn_task, total);
                    Database.beginTr();
                    Database.updateGoodsQuantity(currentGoods.goods, currentGoods.cellOut_task, currentGoods.mdoc, total);
                    Database.addPickedGoods(currentGoods.goods,
                            currentGoods.mdoc,
                            currentGoods.cellOut_task,
                            currentGoods.cellIn_task,
                            total,
                            currentGoods.goods_descr,
                            currentGoods.units,
                            currentGoods.goods_article);
                    Database.endTr();
                    App.currentDistance = goodsMovements.get(0).distance;
                    Database.setLock(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task, 0);
                    goodsMovements.remove(0);
                    goodsMovements.sort((lhs, rhs) -> {
                        int ret = 0;
                        if (lhs != null && rhs != null) {
                            int leftDistance = lhs.getDistance(App.currentDistance);
                            int rightDistance = rhs.getDistance(App.currentDistance);
                            ret = Integer.compare(leftDistance, rightDistance);
                        }
                        return ret;
                    });
//                    ((MainActivity) getActivity()).beep();
                    MainActivity.say(getResources().getString(R.string.picked_tts));
                    nextGoods();
                } else {
                    if (qty + subtotal > total) {
                        MainActivity.say(getResources().getString(R.string.excessive_goods));
                        adbGoods = new AlertDialog.Builder(getActivity());
                        adbGoods.setMessage(R.string.confirm_excessive_goods);
                        adbGoods.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                qty -= barCode.qnt;
                                etPickQty.setText(String.valueOf(qty));
                                etPickQty.setSelection(etPickQty.getText().length());
                                etPickQty.requestFocus();
                                FL.d(TAG, "Excessive goods negative button click");
                            }
                        });
                        adbGoods.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                Database.setPick(currentGoods.goods, currentGoods.mdoc,
                                        currentGoods.cellOut_task, currentGoods.cellIn_task, total);
                                Database.addPickedGoods(currentGoods.goods,
                                        currentGoods.mdoc,
                                        currentGoods.cellOut_task,
                                        currentGoods.cellIn_task,
                                        total,
                                        currentGoods.goods_descr,
                                        currentGoods.units,
                                        currentGoods.goods_article);
                                App.currentDistance = goodsMovements.get(0).distance;
                                Database.setLock(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task, 0);
                                goodsMovements.remove(0);
                                goodsMovements.sort((lhs, rhs) -> {
                                    int ret = 0;
                                    if (lhs != null && rhs != null) {
                                        int leftDistance = lhs.getDistance(App.currentDistance);
                                        int rightDistance = rhs.getDistance(App.currentDistance);
                                        ret = Integer.compare(leftDistance, rightDistance);
                                    }
                                    return ret;
                                });
//                                ((MainActivity) getActivity()).beep();
                                MainActivity.say(getResources().getString(R.string.picked_tts));
                                nextGoods();
                                FL.d(TAG, "Excessive goods positive button click");
                            }
                        });
                        adbGoods.setCancelable(false);
                        adbGoods.create().show();
                    } else {    // qty + subtotal < total
                        etPickQty.requestFocus();
                        etPickQty.setText(String.valueOf(qty));
                        etPickQty.setSelection(etPickQty.getText().length());
                        if (barCode.qnt > 1) MainActivity.say(String.valueOf(barCode.qnt));
                    }
                }
            } else {
                MainActivity.say(getResources().getString(R.string.wrong_barcode));
            }
        }
    }
    public void processScan(GoodsMovement[] gms, int qnt) {
        if(!goodsMovements.isEmpty()) {
            for (GoodsMovement gm : gms) {
                if(gm.goods.equals(currentGoods.goods)) {
                    qty += qnt;
//                    String toastBarcode = gm.goods + " " + gm.goods_descr + " " + qnt;
//                    Toast.makeText(getContext(), toastBarcode, Toast.LENGTH_SHORT).show();
                    if (qty + subtotal < total) {
                        etPickQty.requestFocus();
                        etPickQty.setText(String.valueOf(qty));
                        etPickQty.setSelection(etPickQty.getText().length());
                        if (qnt > 1) MainActivity.say(String.valueOf(qnt));
                    } else if (qty + subtotal == total) {
                        Database.setPick(currentGoods.goods, currentGoods.mdoc,
                                currentGoods.cellOut_task, currentGoods.cellIn_task, total);
                        Database.addPickedGoods(currentGoods.goods,
                                currentGoods.mdoc,
                                currentGoods.cellOut_task,
                                currentGoods.cellIn_task,
                                total, currentGoods.goods_descr,
                                currentGoods.units,
                                currentGoods.goods_article);
                        App.currentDistance = goodsMovements.get(0).distance;
                        Database.setLock(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task, 0);
                        goodsMovements.remove(0);
                        goodsMovements.sort((lhs, rhs) -> {
                            int ret = 0;
                            if (lhs != null && rhs != null) {
                                int leftDistance = lhs.getDistance(App.currentDistance);
                                int rightDistance = rhs.getDistance(App.currentDistance);
                                ret = Integer.compare(leftDistance, rightDistance);
                            }
                            return ret;
                        });
//                        ((MainActivity) getActivity()).beep();
                        MainActivity.say(getResources().getString(R.string.picked_tts));
                        nextGoods();
                        break;
                    } else {
                        MainActivity.say(getResources().getString(R.string.excessive_goods));
                        adbGoods = new AlertDialog.Builder(getActivity());
                        adbGoods.setMessage(R.string.confirm_excessive_goods);
                        adbGoods.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                qty -= qnt;
                                etPickQty.setText(String.valueOf(qty));
//                                String remain = String.valueOf(total - subtotal);// + getResources().getString(R.string.remain_of) + total;
//                                tvTotal.setText(remain);
                                etPickQty.requestFocus();
                                etPickQty.setSelection(etPickQty.getText().length());
                                FL.d(TAG, "Excessive goods negative button click");
                            }
                        });
                        adbGoods.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                Database.setPick(currentGoods.goods, currentGoods.mdoc,
                                        currentGoods.cellOut_task, currentGoods.cellIn_task, subtotal);
                                Database.addPickedGoods(currentGoods.goods,
                                        currentGoods.mdoc,
                                        currentGoods.cellOut_task,
                                        currentGoods.cellIn_task,
                                        subtotal,
                                        currentGoods.goods_descr,
                                        currentGoods.units,
                                        currentGoods.goods_article);
                                App.currentDistance = goodsMovements.get(0).distance;
                                Database.setLock(currentGoods.goods, currentGoods.mdoc, currentGoods.cellOut_task, currentGoods.cellIn_task, 0);
                                goodsMovements.remove(0);
                                goodsMovements.sort((lhs, rhs) -> {
                                    int ret = 0;
                                    if (lhs != null && rhs != null) {
                                        int leftDistance = lhs.getDistance(App.currentDistance);
                                        int rightDistance = rhs.getDistance(App.currentDistance);
                                        ret = Integer.compare(leftDistance, rightDistance);
                                    }
                                    return ret;
                                });
//                                ((MainActivity) getActivity()).beep();
                                MainActivity.say(getResources().getString(R.string.picked_tts));
                                etPickQty.setText("");
                                nextGoods();
                                FL.d(TAG, "Excessive goods positive button click");
                            }
                        });
                        adbGoods.setCancelable(false);
                        adbGoods.create().show();
                    }
                    break;
                } else {
                    FL.d(TAG, "Try other barcode");
                }
            }
//            MainActivity.say(getResources().getString(R.string.wrong_barcode));
        } else {
            MainActivity.say(getResources().getString(R.string.wrong_barcode));
            FL.e(TAG, "Empty goods list");
        }
    }
    public void closePhoto() {
        photoView.setVisibility(View.GONE);
        tvPickingTitle.setVisibility(View.VISIBLE);
        tvArticle.setVisibility(View.VISIBLE);
        tvGoodsDesc.setVisibility(View.VISIBLE);
        tvBrand.setVisibility(View.VISIBLE);
        llButtons.setVisibility(View.VISIBLE);
        llCell.setVisibility(View.VISIBLE);
        llQty.setVisibility(View.VISIBLE);
        llTotal.setVisibility(View.VISIBLE);
    }
    public void showPhoto(String photoURL) {
//        String photoURL = Config.photoPath + currentGoods.url;
        tvPickingTitle.setVisibility(View.GONE);
        tvArticle.setVisibility(View.GONE);
        tvGoodsDesc.setVisibility(View.GONE);
        tvBrand.setVisibility(View.GONE);
        llButtons.setVisibility(View.GONE);
        llCell.setVisibility(View.GONE);
        llQty.setVisibility(View.GONE);
        llTotal.setVisibility(View.GONE);
        photoView.setWebViewClient(new WebViewClient());
        photoView.getSettings().setLoadWithOverviewMode(true);
        photoView.getSettings().setUseWideViewPort(true);
        Log.d(TAG, "show photo " + photoURL);
        photoView.loadUrl(photoURL);
        photoView.setVisibility(View.VISIBLE);
    }

    public void setPosition() {
        goodsMovements.sort((lhs, rhs) -> {
            int ret = 0;
            if (lhs != null && rhs != null) {
                int leftDistance = lhs.getDistance(App.currentDistance);
                int rightDistance = rhs.getDistance(App.currentDistance);
                ret = Integer.compare(leftDistance, rightDistance);
            }
            return ret;
        });
        nextGoods();
    }
}