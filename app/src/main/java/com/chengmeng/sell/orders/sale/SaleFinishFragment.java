package com.chengmeng.sell.orders.sale;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.BaseAdapter;

import com.alibaba.fastjson.JSONException;
import com.chengmeng.message.msg_tool.InfoTool;
import com.chengmeng.sell.orders.SellAllOrderBuy;
import com.chengmeng.sell.orders.SellAllOrderSell;
import com.chengmeng.sell.orders.base.SellAllOrderFragment;
import com.chengmeng.tools.all.StaticMethod;
import com.chengmeng.tools.connect.ConnectDialog;
import com.chengmeng.tools.connect.ConnectList;
import com.chengmeng.tools.connect.ConnectListener;
import com.chengmeng.tools.connect.ServerURL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SaleFinishFragment extends SellAllOrderFragment {
    private SaleFinishAdapter saleFinishAdapter;
    private List<Map<String, Object>> finishItems;
    private int page;
    public SaleFinishFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public SaleFinishFragment(BaseAdapter adapter) {
        super(adapter);
    }

    @Override
    public void onListLoad() {
        page++;
        updateDataFromNetForFAdapter();
    }

    @Override
    public void onListRefresh() {
        page = 1;
        updateDataFromNetForFAdapter();
    }

    @Override
    public void onListClick(int position) {
    }

    @Override
    public boolean onLazyLoad() {
        page = 1;
        finishItems = new ArrayList<Map<String, Object>>();
        saleFinishAdapter = new SaleFinishAdapter(getActivity(), finishItems);
        updateDataFromNetForFAdapter();
        return true;
    }

    //连接后台获取服务的详细信息
    private void updateDataFromNetForFAdapter() {
        StaticMethod.POST(getActivity(), SellAllOrderSell.URL,
                new ConnectListener() {
                    @Override
                    public ConnectDialog showDialog(ConnectDialog dialog) {
                        return dialog;
                    }

                    @Override
                    public ConnectList setParam(ConnectList list) {
                        list.put("userId", InfoTool.getUserID(getActivity()));
                       // list.put("userId", "" + 1);
                        list.put("page", "" + page);
                        list.put("status", "4,5");
                        return list;
                    }

                    @Override
                    public void onResponse(String response) {
                        if (response == null || response.equals("")) {
                            showToast("网络错误");
                            return;
                        }
                        dealResponse(response);
                    }
                });
    }

    //处理后台发送来的数据
    private void dealResponse(String response) {
        if (response.equals("failed")) {
        } else {
            try {
                if (page == 1) finishItems.clear();
                SellAllOrderBuy.Analysis(getActivity(), response, finishItems);
                Log.i("responseresponse",response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setAdapter(saleFinishAdapter);
        updateList();
    }

}
