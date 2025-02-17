package com.chengmeng.sell.orders.sale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chengmeng.R;
import com.chengmeng.message.msg_tool.MsgPublicTool;
import com.chengmeng.service.SellOrderDetailActivitySale;
import com.chengmeng.tools.all.StaticMethod;
import com.chengmeng.tools.connect.ServerURL;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/27 0027.
 */
public class SaleFinishAdapter extends BaseAdapter {
    private SharedPreferences sp;
    private Context context;
    private List<Map<String, Object>> listitems;
    private LayoutInflater mInflater;
    private AlertDialog myDialog;

    public SaleFinishAdapter(Context context, List<Map<String, Object>> listitems) {
        mInflater = LayoutInflater.from(context);
        sp = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        this.context = context;
        this.listitems = listitems;
    }

    @Override
    public int getCount() {
        return listitems.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        final int pos = position;
        final int id;
        holder = new ViewHolder();
        View view = null;
        if (convertView != null)
            view = convertView;
        else
            view = mInflater.inflate(R.layout.sell_allorder_sale_finish, null);
        holder.allorder_sale_finish_rhv_headview = (ImageView) view.findViewById(R.id.allorder_sale_finish_rhv_headview);
        holder.allorder_sale_finish_tv_nickname = (TextView) view.findViewById(R.id.allorder_sale_finish_tv_nickname);
        holder.allorder_sale_finish_iv_describe = (ImageView) view.findViewById(R.id.allorder_sale_finish_iv_describe);
        holder.allorder_sale_finish_tv_progectcontent = (TextView) view.findViewById(R.id.allorder_sale_finish_tv_progectcontent);
        holder.allorder_sale_finish_tv_pricecontent = (TextView) view.findViewById(R.id.allorder_sale_finish_tv_pricecontent);
        holder.allorder_sale_finish_tv_numbercontent = (TextView) view.findViewById(R.id.allorder_sale_finish_tv_numbercontent);
        holder.allorder_sale_finish_tv_timecontent = (TextView) view.findViewById(R.id.allorder_sale_finish_tv_timecontent);
        holder.allorder_sale_finish_tv_connectcontent = (TextView) view.findViewById(R.id.allorder_sale_finish_tv_connectcontent);
        holder.allorder_sale_finish_tv_charge = (TextView) view.findViewById(R.id.allorder_sale_finish_tv_chargecontent);
        holder.allorder_sale_finish_RL = (RelativeLayout) view.findViewById(R.id.allorder_sale_finish_RL);
        id = (int) listitems.get(position).get("id");
        holder.allorder_sale_finish_RL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SellOrderDetailActivitySale.class);
                intent.putExtra("come", "other");
                intent.putExtra("ugsId", "" + id);
                context.startActivity(intent);
            }
        });

        getBitmapHead(holder.ip + (String) listitems.get(position).get("headview"), holder.allorder_sale_finish_rhv_headview);
        final long username = (Long) listitems.get(position).get("username");
        holder.allorder_sale_finish_rhv_headview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MsgPublicTool.showAllInfo(context, username);
            }
        });
        holder.allorder_sale_finish_tv_nickname.setText((String) listitems.get(position).get("nickname"));
        getBitmap(holder.ip + (String) listitems.get(position).get("describe"), holder.allorder_sale_finish_iv_describe);
        holder.allorder_sale_finish_tv_progectcontent.setText((String) listitems.get(position).get("progectcontent"));
        holder.allorder_sale_finish_tv_pricecontent.setText((String) listitems.get(position).get("reward_money") + "元/" + (String) listitems.get(position).get("reward_unit"));
        holder.allorder_sale_finish_tv_numbercontent.setText("" + (Integer) listitems.get(position).get("numbercontent"));
        holder.allorder_sale_finish_tv_timecontent.setText((String) listitems.get(position).get("timecontent"));
        holder.allorder_sale_finish_tv_timecontent.setText("" + (long) listitems.get(position).get("connectcontent"));
        holder.allorder_sale_finish_tv_charge.setText("" + (Integer) listitems.get(position).get("charge") + "元");

        return view;
    }

    static final class ViewHolder {
        public TextView allorder_sale_finish_tv_nickname, allorder_sale_finish_tv_progectcontent,
                allorder_sale_finish_tv_pricecontent, allorder_sale_finish_tv_numbercontent, allorder_sale_finish_tv_timecontent, allorder_sale_finish_tv_connectcontent, allorder_sale_finish_tv_charge;
        public ImageView allorder_sale_finish_iv_describe;
        public ImageView allorder_sale_finish_rhv_headview;
        private RelativeLayout allorder_sale_finish_RL;
        public String ip = ServerURL.getIP();


    }

    private void getBitmap(final String url, final ImageView imageview) {
        StaticMethod.BITMAP(context, url, imageview);
    }

    private void getBitmapHead(final String url, final ImageView imageview) {
        StaticMethod.BITMAPHEAD(context, url, imageview);
    }
}
