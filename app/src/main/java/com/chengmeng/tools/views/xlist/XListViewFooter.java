package com.chengmeng.tools.views.xlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chengmeng.R;

/**
 * 列表底部
 *
 * @author Internet
 */
public class XListViewFooter extends LinearLayout {
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_LOADING = 2;

    private boolean mEnableSpring = false;
    private String text_load_more = "查看更多", text_load_touch = "松开载入更多";
    private Context mContext;

    private View mContentView;
    private View mProgressBar;
    private TextView mHintView;

    public XListViewFooter(Context context) {
        super(context);
        initView(context);
    }

    public XListViewFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void setState(int state) {
        mHintView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mHintView.setVisibility(View.INVISIBLE);
        if (state == STATE_READY) {
            mHintView.setVisibility(View.VISIBLE);
            if (!mEnableSpring)
                mHintView.setText(text_load_touch);
        } else if (state == STATE_LOADING) {
            if (!mEnableSpring)
                mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mHintView.setVisibility(View.VISIBLE);
            if (!mEnableSpring)
                mHintView.setText(text_load_more);
        }
    }

    /**
     * 设置为弹性列表
     */
    public void setFooterSpring() {
        mEnableSpring = true;
        mHintView.setText("");
    }

    public void setBottomMargin(int height) {
        if (height < 0)
            return;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
                .getLayoutParams();
        lp.bottomMargin = height;
        mContentView.setLayoutParams(lp);
    }

    public int getBottomMargin() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
                .getLayoutParams();
        return lp.bottomMargin;
    }

    public void setFooterText(String show, String show_touch) {
        text_load_more = show;
        text_load_touch = show_touch;
        mHintView.setText(text_load_more);
    }

    /**
     * normal status
     */
    public void normal() {
        mHintView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * loading status
     */
    public void loading() {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * hide footer when disable pull load more
     */
    public void hide() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
                .getLayoutParams();
        lp.height = 0;
        mContentView.setLayoutParams(lp);
    }

    /**
     * show footer
     */
    public void show() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentView
                .getLayoutParams();
        lp.height = LayoutParams.WRAP_CONTENT;
        mContentView.setLayoutParams(lp);
    }

    private void initView(Context context) {
        mContext = context;
        LinearLayout moreView = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.xlistview_footer, null);
        addView(moreView);
        moreView.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        mContentView = moreView.findViewById(R.id.xlistview_footer_content);
        mProgressBar = moreView.findViewById(R.id.xlistview_footer_progressbar);
        mHintView = (TextView) moreView
                .findViewById(R.id.xlistview_footer_hint_textview);
    }

}
