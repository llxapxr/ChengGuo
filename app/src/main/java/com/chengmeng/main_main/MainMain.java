package com.chengmeng.main_main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.google.gson.Gson;
import com.chengmeng.R;
import com.chengmeng.message.baichuan.util.AndTools;
import com.chengmeng.message.info.AnBaseInfo;
import com.chengmeng.message.info.AnUserInfo;
import com.chengmeng.message.info.ShowAllInfo;
import com.chengmeng.message.msg_tool.InfoTool;
import com.chengmeng.message.msg_tool.ParamTool;
import com.chengmeng.more.MainSet;
import com.chengmeng.sell.discuss.DiscussTool;
import com.chengmeng.sell.main.MainTabSell;
import com.chengmeng.start.register.Register3;
import com.chengmeng.tools.all.StaticMethod;
import com.chengmeng.tools.bitmap.BitmapListener;
import com.chengmeng.tools.bitmap.BitmapTool;
import com.chengmeng.tools.connect.ConnectDialog;
import com.chengmeng.tools.connect.ConnectList;
import com.chengmeng.tools.connect.ConnectListener;
import com.chengmeng.tools.connect.ServerURL;
import com.chengmeng.tools.map.Location;
import com.chengmeng.tools.map.LocationListener;
import com.chengmeng.tools.views.QianxunToast;
import com.chengmeng.tools.views.slidemenu.SlidingMenu;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class MainMain extends MainTActivity implements OnClickListener {
    protected static final String TAG = "MainActivity";
    private long currentBackPressedTime = 0; // 退出时间
    private static final int BACK_PRESSED_INTERVAL = 2000; // 退出间隔
    private int last_index, current_index;// 减少重复初始化
    private MainBottom main_bottom;// 底部标签
    private MainMenu main_menu;// 菜单
    private MainTab_05 main_tab_05;
    private MainTab_01 main_tab_01;
    private ArrayList<Fragment> fragment_list;
    private int msg_index = -1;
    private TextView top_location_text;
    private boolean is_first_init;

    @Override
    public void onCreate() {
        setContentView(R.layout.main_tab);

        getMessage();
        initUM();
        initView();
        initListener();

        updateUserLocation();

        initSlidingMenu();
        loadFirstFragmen();

        is_first_init = true;
        MainSet.checkNewVersion(this, true);
    }

    private void getMessage() {
        msg_index = getIntent().getIntExtra("msg_index", -1);
    }

    /**
     * 初始化滑动菜单
     */
    private void initSlidingMenu() {
        // 设置滑动菜单的视图
        main_menu = new MainMenu();
        setBehindContentView(R.layout.menu_frame);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_frame, main_menu).commit();
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);// 滑动阴影的宽度
        sm.setShadowDrawable(null);// 滑动阴影的图像资源
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);// 触摸屏幕的模式

        sm.setScareEnable(false);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffset(AndTools.dp2px(this, 120));
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);

        // 使用在线参数修改配置，类似于热更新。（目前用于娱乐）
        String value = ParamTool.getParam("menu_type");
        if (value.equals("1")) {
            sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);// 滑动菜单视图的宽度
            sm.setFadeDegree(0f);// 渐入渐出效果的值
            sm.setBehindScrollScale(0f);// 滑动菜单滑动时缩放的效果
            sm.setScareEnable(true);// 是否允许缩放
        }

    }

    // 初始化控件
    private void initView() {
        fragment_list = new ArrayList<Fragment>();

        main_tab_01 = new MainTab_01();
        main_tab_05 = new MainTab_05();
        fragment_list.add(main_tab_01.getImFragment());
        fragment_list.add(new MainTabSell());
        fragment_list.add(new MainTab_03());
        fragment_list.add(new MainTab_04());
        fragment_list.add(main_tab_05);

        main_bottom = new MainBottom(this);

        //位置显示
        top_location_text = (TextView) findViewById(R.id.top_location_text);
    }

    private void loadFirstFragmen() {
        if (msg_index != -1) {
            current_index = last_index = msg_index;// 默认跳转
        } else {
            current_index = last_index = 1;// 首个页面--------------------------
            if (!ServerURL.isTest()) {// 不是测试模式
                String value = ParamTool.getParam("main_tab");
                int index_temp = current_index;
                try {
                    index_temp = Integer.parseInt(value);
                } catch (Exception e) {
                    index_temp = current_index;
                }
                current_index = last_index = index_temp;
            }
        }
        updateFragment();

    }

    private void updateUserLocation() {
        Location.getLocation(getApplicationContext(), new LocationListener() {
            @Override
            public void locationRespose(String locationName, double x, double y, float limit) {
                if (locationName == null || locationName.equals("")) {
                    top_location_text.setText("定位失败");
                    return;
                }
                int start_guo = locationName.indexOf("中国");
                int end_sheng = locationName.indexOf("省");
                if (start_guo == -1) {
                    top_location_text.setText("世界");
                    return;
                }
                String sheng = "", shi = "", show = "";
                try {
                    if (end_sheng != -1) {
                        sheng = locationName.substring(start_guo + 2, end_sheng + 1);
                        shi = locationName.substring(end_sheng + 1, locationName.indexOf("市") + 1);
                        show = sheng + "·" + shi;
                    } else {
                        shi = locationName.substring(start_guo + 2, locationName.indexOf("市") + 1);
                        show = shi;
                    }
                } catch (Exception e) {
                    show = "中国";
                }
                top_location_text.setText(show);
            }
        }, true);
    }

    private void updateUserInfo() {
        if (!"".equals(InfoTool.getUserID(this))) {
            AnUserInfo old_info = InfoTool.getUserInfo(this);
            final String old_nick;
            final String old_head;
            if (old_info == null) {
                old_nick = "";
                old_head = "";
            } else {
                old_nick = old_info.getNickName();
                old_head = old_info.getHeadIcon();
            }
            StaticMethod.POST(this, ServerURL.HOMEPAGE_PERSONAL_DETAIL, new ConnectListener() {
                        public ConnectDialog showDialog(ConnectDialog dialog) {
                            return null;
                        }

                        public ConnectList setParam(ConnectList list) {
                            //需要保留InfoTool的作用
                            list.put("userId", InfoTool.getUserID(MainMain.this));
                            return list;
                        }

                        public void onResponse(String response) {
                            // 各种失败都不用管
                            if (response == null || response.equals("")
                                    || response.equals("failed") || response.equals("-1")
                                    || response.equals("-2")) {
                            } else {// 如果成功的话
                                try {
                                    //存储基本信息
                                    InfoTool.saveUserInfo(MainMain.this, response);
                                    final AnUserInfo info = new Gson().fromJson(response, AnUserInfo.class);
                                    AnBaseInfo an_info = new AnBaseInfo(info.getUsername() + "",
                                            info.getNickName(), info.getGender(),
                                            info.getBirthday(), info.getAddress(),
                                            Register3.getIconPath());
                                    InfoTool.saveBaseInfo(MainMain.this, an_info);
                                    //首次启动的话，登录讨论区
                                    if (is_first_init) {
                                        DiscussTool.getInstance().loginDiscuss(MainMain.this,
                                                info.getId() + "", info.getNickName(),
                                                !info.getGender().equals("女"), ServerURL.getIP() + info.getHeadIcon());
                                    } else {
                                        if (old_nick != null && !old_nick.equals(info.getNickName())) {
                                            DiscussTool.getInstance().updateUserInfo(MainMain.this,
                                                    info.getId() + "", info.getNickName(), !info.getGender().equals("女"),
                                                    ServerURL.getIP() + info.getHeadIcon());
                                        } else if (old_head != null && !old_head.equals(info.getHeadIcon())) {
                                            DiscussTool.getInstance().updateUserInfo(MainMain.this,
                                                    ServerURL.getIP() + info.getHeadIcon());
                                        }
                                    }
                                    // 图片下载(首次刷新，或者有变化时刷新)
                                    if (is_first_init || (old_head != null && !old_head.equals(info.getHeadIcon())))
                                        StaticMethod.UBITMAPHEAD(ServerURL.getIP() + info.getHeadIcon(),
                                                new BitmapListener() {
                                                    public void onResponse(Bitmap bitmap) {
                                                        if (bitmap != null) {
                                                            boolean temp = BitmapTool.writeToFile(bitmap,
                                                                    "jpg", Register3.getIconPath());
                                                            if (ServerURL.isTest())
                                                                Log.e("EEEEE", "SAVE-" + temp + "");
                                                            main_menu.updateHeadView(bitmap, info.getNickName());
                                                            try {//可能MainTab_05还没有添加到Fragment中
                                                                if (current_index == 4)
                                                                    main_tab_05.updateHead(bitmap, info.getNickName(), info.getVerifyStatus());
                                                            } catch (Exception e) {
                                                            }
                                                        }
                                                    }
                                                });
                                    is_first_init = false;
                                } catch (Exception e) {
                                }
                            }
                        }
                    }

            );
        }
    }

    private void initListener() {
        // 头像
        initHeadIcon(new OnClickListener() {
            public void onClick(View v) {
                toggle();
            }
        });
        // 设置顶部三个标签页点击事件
        main_bottom.setClickListener(this);
        top_location_text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                top_location_text.setText("正在定位……");
                updateUserLocation();
            }
        });
    }

    private void clickBtnAdd() {
        main_bottom.transAddImage();
        Intent intent = new Intent(this, MainTabAdd.class);
        startActivityForResult(intent, MainTabAdd.CODE_ADD);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_linear_01:
                current_index = 0;
                break;
            case R.id.tab_linear_02:
                current_index = 1;
                break;
            case R.id.tab_linear_03:
                current_index = 0;
                break;
            case R.id.tab_linear_04:
                current_index = 3;
                break;
            case R.id.tab_linear_05:
                current_index = 4;
                break;
            case R.id.tab_iv_add:
                clickBtnAdd();
                return;
        }
        // 减少不必要的初始化
        if (last_index == current_index)
            return;
        AndTools.hideKeyboardEasy(this);
        updateFragment();
    }

    // UM统计
    private void initUM() {
        // SDK在统计Fragment时，需要关闭Activity自带的页面统计，
        // 然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
        MobclickAgent.openActivityDurationTrack(false);
        if (ServerURL.isTest())
            StatService.setDebugOn(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        main_tab_01.onPause();
        MobclickAgent.onPause(this);
        StatService.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        main_tab_01.onResume();
        MobclickAgent.onResume(this);
        StatService.onResume(this);
        updateUserInfo();
    }

    // 按键处理
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggle();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getSlidingMenu().isMenuShowing()) {
                showContent();
            } else if (System.currentTimeMillis() - currentBackPressedTime > BACK_PRESSED_INTERVAL) {
                currentBackPressedTime = System.currentTimeMillis();
                QianxunToast.showToast(this, "再按一次退出程序", QianxunToast.LENGTH_SHORT);
            } else {
                finish();
            }
        }
        return false;
    }

    // 长按进入测试界面
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.main_menu_head_layout) {
            if (ServerURL.isTest())
                startActivity(new Intent(this, TestActivity.class));
            showContent();// 收回菜单
        }
    }

    private void updateFragment() {
        // 顶部和底部
        main_bottom.setCurrentTab(current_index);// 底部刷新
        setTopPosition(current_index);// 顶部刷新
        //消息和推送
            if (current_index == 4) {
                main_tab_05.updateHead();
            }
//        // 替换Fragment，这个每次View都会被销毁，需要重新onCreateView
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.main_content, fragment_list.get(current_index),
//                        TAG).commit();
        //通过一种不销毁的方式，替换视图。（replace与ViewPager机制不同）
        switchContent(fragment_list.get(last_index), fragment_list.get(current_index));
        // 刷新last_index
        last_index = current_index;
    }

    public void switchContent(Fragment from, Fragment to) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out);
        if (from != to)         //是否是同一个
            transaction.hide(from);
        if (!to.isAdded()) {    // 先判断是否被add过
            transaction.add(R.id.main_content, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
        } else {
            transaction.show(to).commit(); // 隐藏当前的fragment，显示下一个
        }
    }


    @Override
    protected void showContextMenu(View v, int cur_page) {
        current_index = 0;
        // 减少不必要的初始化
        if (last_index == current_index) {
            return;
        }
        updateFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == ShowAllInfo.CODE_ALTER_INFO) {
            main_menu.updateHeadView();
            try {
//                if (current_index == 4)
                main_tab_05.updateHead();
            } catch (Exception e) {
            }
            return;
        }
        if (requestCode == MainTabAdd.CODE_ADD) {
            main_bottom.restoreAddImage();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
