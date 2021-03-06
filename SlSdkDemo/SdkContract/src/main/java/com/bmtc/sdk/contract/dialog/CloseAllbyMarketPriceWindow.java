package com.bmtc.sdk.contract.dialog;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bmtc.sdk.contract.R;
import com.bmtc.sdk.library.common.pswkeyboard.OnPasswordInputFinish;
import com.bmtc.sdk.library.common.pswkeyboard.widget.PopEnterPassword;
import com.bmtc.sdk.library.constants.BTConstants;
import com.bmtc.sdk.library.contract.ContractCalculate;
import com.bmtc.sdk.library.trans.BTContract;
import com.bmtc.sdk.library.trans.IResponse;
import com.bmtc.sdk.library.trans.data.Contract;
import com.bmtc.sdk.library.trans.data.ContractOrder;
import com.bmtc.sdk.library.trans.data.ContractOrders;
import com.bmtc.sdk.library.trans.data.ContractPosition;
import com.bmtc.sdk.library.uilogic.LogicGlobal;
import com.bmtc.sdk.library.utils.MathHelper;
import com.bmtc.sdk.library.utils.ToastUtil;
import com.bmtc.sdk.library.utils.UtilSystem;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

public class CloseAllbyMarketPriceWindow extends PopupWindow implements View.OnClickListener {
    private LinearLayout parentFrame;
    private Button btnClose;

    private Button btnCloseAll;
    private Button btnCancelOrders;
    private Button btnCancel;
    private Context context;
    private AnimatorSet mAnimatorSet;
    private static final int DURATION = 700;
    private long mDuration =DURATION ;

    private TextView tvType;
    private TextView tvContractName;
    private TextView tvVolume;

//    private ImageView mLoadingImage;
    private AnimationDrawable animationDrawable;

    private RelativeLayout mCancelTipsRl;
    private RelativeLayout mCancellingRl;
    private RelativeLayout mCancelledRl;

    private ContractPosition mContractPosition;
    private List<ContractOrder> mOrderList = new ArrayList<>();

    public void setOrderList(ContractPosition position, List<ContractOrder> orderList) {
        if (position == null) {
            return;
        }

        mContractPosition = position;
        final Contract contract = LogicGlobal.getContract(mContractPosition.getInstrument_id());
        if (contract == null) {
            return;
        }

        if (orderList == null) {
            mOrderList.clear();
            return;
        }
        mOrderList.clear();
        mOrderList.addAll(orderList);

        tvContractName.setText(contract.getSymbol());
        if (mContractPosition.getPosition_type() == ContractPosition.POSITION_TYPE_LONG) {
            tvType.setText(R.string.sl_str_sell_close);
            tvType.setTextColor(context.getResources().getColor(R.color.sl_colorRed));
            tvType.setBackgroundResource(R.drawable.sl_border_red);
        } else if (mContractPosition.getPosition_type() == ContractPosition.POSITION_TYPE_SHORT) {
            tvType.setText(R.string.sl_str_buy_close);
            tvType.setTextColor(context.getResources().getColor(R.color.sl_colorGreen));
            tvType.setBackgroundResource(R.drawable.sl_border_green);
        }

        double vol = 0.0;
        double amount = 0.0;
        for (int i=0; i<mOrderList.size(); i++) {
            ContractOrder order = mOrderList.get(i);
            if (order == null) {
                continue;
            }

            vol += MathHelper.sub(order.getQty(), order.getCum_qty());
            amount += MathHelper.mul(vol, MathHelper.round(order.getPx()));
        }

        double price = MathHelper.div(amount, vol);

        tvVolume.setText(ContractCalculate.getVolUnit(contract, vol, price));
    }

    public CloseAllbyMarketPriceWindow(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            return;
        }
        View rootView = inflater.inflate(R.layout.sl_dialog_close_all_by_market_price, null);
        this.setContentView(rootView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        this.setOutsideTouchable(true);

        this.update();
        this.setBackgroundDrawable(new BitmapDrawable());

        initView(rootView);
    }

    public void startAnim(){
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(
                ObjectAnimator.ofFloat(this, "translationY", 300, 0).setDuration(mDuration),
                ObjectAnimator.ofFloat(this, "alpha", 0, 1).setDuration(mDuration*3/2)

        );
        mAnimatorSet.start();
    }

    private void initView(View view) {
        parentFrame = view.findViewById(R.id.ll_frame);
        btnClose = view.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btnCancelOrders = view.findViewById(R.id.btn_cancel_orders);
        btnCancelOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doCancelAll("");
            }
        });

        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btnCloseAll = view.findViewById(R.id.btn_close_all);


        tvType = view.findViewById(R.id.tv_type);
        tvContractName = view.findViewById(R.id.tv_contract_name);
        tvVolume = view.findViewById(R.id.tv_volume_value);
//
//        mLoadingImage = view.findViewById(R.id.iv_loading);
//        mLoadingImage.setImageResource(R.drawable.sl_anim_loading);
//        animationDrawable = (AnimationDrawable) mLoadingImage.getDrawable();
//        animationDrawable.start();

        mCancelTipsRl = view.findViewById(R.id.rl_cancel_tips);
        mCancellingRl = view.findViewById(R.id.rl_cancelling);
        mCancelledRl = view.findViewById(R.id.rl_cancelled);
    }


    public void showbtnCancelOrders(String str) {
        btnCancelOrders.setVisibility(View.VISIBLE);
        btnCancelOrders.setText(str);
    }

    public void showBtnCancel(String str) {
        btnCancel.setVisibility(View.VISIBLE);
        btnCancel.setText(str);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_close ){
            this.dismiss();
        }
    }

    public Button getBtnClose() {
        return btnClose;
    }

    public Button getBtnCloseAll() {
        return btnCloseAll;
    }

    public Button getBtnCancel() {
        return btnCancel;
    }

    private void doCancelAll(String pwd) {
        if (mContractPosition == null || mOrderList == null) {
            return;
        }

        final List<ContractOrder> orderList = new ArrayList<>();
        orderList.addAll(mOrderList);

        ContractOrders orders = new ContractOrders();
        orders.setContract_id(mContractPosition.getInstrument_id());
        for (int i=0; i<orderList.size(); i++){
            ContractOrder item = orderList.get(i);
            if (item == null) {
                continue;
            }

            orders.getOrders().add(item);
        }

        mCancelTipsRl.setVisibility(View.GONE);
        mCancellingRl.setVisibility(View.VISIBLE);
        mCancelledRl.setVisibility(View.GONE);
        btnCancelOrders.setVisibility(View.VISIBLE);
        btnCancelOrders.setEnabled(false);
        btnCloseAll.setVisibility(View.GONE);

        IResponse<List<Long>> response = new IResponse<List<Long>>() {
            @Override
            public void onResponse(String errno, String message, List<Long> data) {
                if (TextUtils.equals(errno, BTConstants.ERRNO_PERMISSION_DENIED)) {
                    mCancelTipsRl.setVisibility(View.VISIBLE);
                    mCancellingRl.setVisibility(View.GONE);
                    mCancelledRl.setVisibility(View.GONE);
                    btnCancelOrders.setVisibility(View.VISIBLE);
                    btnCancelOrders.setEnabled(true);
                    btnCloseAll.setVisibility(View.GONE);

                    final PopEnterPassword popEnterPassword = new PopEnterPassword(context);
                    popEnterPassword.showAtLocation(btnCancelOrders, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    popEnterPassword.setOnFinishInput(new OnPasswordInputFinish() {
                        @Override
                        public void inputFinish(String password) {
                            doCancelAll(UtilSystem.toMD5(password));
                            popEnterPassword.dismiss();
                        }
                    });
                    return;
                }

                if (!TextUtils.equals(errno, BTConstants.ERRNO_OK) || !TextUtils.equals(message, BTConstants.ERRNO_SUCCESS)) {
                    ToastUtil.shortToast(LogicGlobal.sContext, message);
                    dismiss();
                    return;
                }

                if (data != null && data.size() > 0) {
                    ToastUtil.shortToast(LogicGlobal.sContext, LogicGlobal.sContext.getString(R.string.sl_str_some_orders_cancel_failed));
                    dismiss();
                } else {

                    mCancelTipsRl.setVisibility(View.GONE);
                    mCancellingRl.setVisibility(View.GONE);
                    mCancelledRl.setVisibility(View.VISIBLE);
                    btnCancelOrders.setVisibility(View.GONE);
                    btnCancelOrders.setEnabled(true);
                    btnCloseAll.setVisibility(View.VISIBLE);
                }

                BTContract.getInstance().userPositions(mContractPosition.getInstrument_id(), 1, 0, 0, new IResponse<List<ContractPosition>>() {
                    @Override
                    public void onResponse(String errno, String message, List<ContractPosition> data) {

                    }
                });
            }
        };

        if (TextUtils.isEmpty(pwd)) {
            BTContract.getInstance().cancelOrders(orders, response);
        } else {
            BTContract.getInstance().cancelOrders(orders, pwd, response);
        }
    }
}
