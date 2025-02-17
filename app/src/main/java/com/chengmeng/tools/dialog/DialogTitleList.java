package com.chengmeng.tools.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.chengmeng.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义的对话框类<br/>
 * 自我感觉良好
 */
public class DialogTitleList extends Dialog {
	protected Context context;
	protected String title = "";
	protected TextView tv_text, tv_divider_h, tv_divider_v;
	protected ListView list;
	protected Button btn_pos, btn_neg;
	protected String text = "", text_pos = "", text_neg = "";
	protected String[] array;
	protected DialogButtonListener listener_pos, listener_neg;
	protected DialogItemListener listener_list;

	public DialogTitleList(Context context) {
		super(context);
		this.context = context;
		this.title = "";
	}

	public DialogTitleList(Context context, String title) {
		super(context,android.R.style.Theme_Holo_Light_Dialog);
		this.context = context;
		this.title = title;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (title != null && !title.equals(""))
			setTitle(title);
		else
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_title_list);
		initView();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		initViewSet();
	}

	@Override
	public void onDetachedFromWindow() {// 这个之后再stop
		super.onDetachedFromWindow();
	}

	private void initView() {
		tv_text = (TextView) findViewById(R.id.dialog_title_list_text);
		tv_divider_h = (TextView) findViewById(R.id.dialog_title_list_divider_h);
		tv_divider_v = (TextView) findViewById(R.id.dialog_title_list_divider_v);
		list = (ListView) findViewById(R.id.dialog_title_list_list);
		btn_pos = (Button) findViewById(R.id.dialog_title_list_sure);
		btn_neg = (Button) findViewById(R.id.dialog_title_list_cancel);
	}

	protected void initViewSet() {
		if (array != null && array.length != 0) {
			list.setVisibility(View.VISIBLE);
			tv_divider_h.setVisibility(View.VISIBLE);
			List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < array.length; i++) {
				Map<String, Object> item = new HashMap<String, Object>();
				item.put("name", array[i]);
				items.add(item);
			}
			SimpleAdapter adapter = new SimpleAdapter(context, items,
					R.layout.dialog_title_list_item, new String[] { "name" },
					new int[] { R.id.dialog_title_list_item_tv });
			list.setAdapter(adapter);

			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (listener_list != null)
						listener_list.onItemClick(position);
					DialogTitleList.this.dismiss();
				}
			});
		}

		if (!"".equals(text)) {
			tv_text.setVisibility(View.VISIBLE);
			tv_divider_h.setVisibility(View.VISIBLE);
			tv_text.setText(text);
			list.setVisibility(View.GONE);// 防止显示冲突
		}

		if (!"".equals(text_pos)) {
			btn_pos.setText(text_pos);
			btn_pos.setVisibility(View.VISIBLE);
			btn_pos.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listener_pos != null)
						listener_pos.onButtonClick();
					DialogTitleList.this.dismiss();
				}
			});
		}

		if (!"".equals(text_neg)) {
			btn_neg.setText(text_neg);
			btn_neg.setVisibility(View.VISIBLE);
			btn_neg.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listener_neg != null)
						listener_neg.onButtonClick();
					DialogTitleList.this.dismiss();
				}
			});
		}

		if (btn_neg.getVisibility() == View.VISIBLE
				&& btn_pos.getVisibility() == View.VISIBLE) {
			tv_divider_v.setVisibility(View.VISIBLE);
		}
	}

	public DialogTitleList setListItem(String[] array) {
		this.array = array;
		return this;
	}

	public DialogTitleList setListListener(DialogItemListener listener_list) {
		this.listener_list = listener_list;
		return this;
	}

	public DialogTitleList setText(String text) {
		this.text = text;
		return this;
	}

	public DialogTitleList setPositiveButton(String text_pos,
			DialogButtonListener listener_pos) {
		this.text_pos = text_pos;
		this.listener_pos = listener_pos;
		return this;
	}

	public DialogTitleList setNegativeButton(String text_neg,
			DialogButtonListener listener_neg) {
		this.text_neg = text_neg;
		this.listener_neg = listener_neg;
		return this;
	}

	public interface DialogItemListener {
		public void onItemClick(int position);
	}

	public interface DialogButtonListener {
		public void onButtonClick();
	}
}
