package com.chengmeng.start.welcome;

import android.content.Intent;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.chengmeng.R;
import com.chengmeng.main_main.MainMain;
import com.chengmeng.message.baichuan.im.LoginSampleHelper;
import com.chengmeng.message.baichuan.mine.BaiChuanUtils;
import com.chengmeng.message.baichuan.util.AppUtil;
import com.chengmeng.message.msg_tool.InfoTool;
import com.chengmeng.message.msg_tool.MsgTool;
import com.chengmeng.message.msg_tool.ParamTool;
import com.chengmeng.message.msg_tool.UserStateTool;
import com.chengmeng.more.AboutMe;
import com.chengmeng.start.login.Login;
import com.chengmeng.start.tool.UserTool;
import com.chengmeng.tools.all.StaticMethod;
import com.chengmeng.tools.connect.ConnectDialog;
import com.chengmeng.tools.connect.ConnectEasy;
import com.chengmeng.tools.connect.ConnectList;
import com.chengmeng.tools.connect.ConnectListener;
import com.chengmeng.tools.connect.ConnectSign;
import com.chengmeng.tools.connect.ServerURL;
import com.chengmeng.tools.file.CacheTool;
import com.chengmeng.tools.top.NetActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Welcome extends NetActivity {
    @Bind(R.id.welcome_img_0)
    ImageView img0;
    @Bind(R.id.welcome_img_1)
    ImageView img1;

    private String name = "", pass = "";
    private String msg = "";
    private boolean need_close = false;
    private int curr_index = 0;

    @Override
    public void onCreate() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.start_welcome);
        ButterKnife.bind(this);

        postRun();
        /*
        getMessage();
        loadFirstImg();
        startNewThread();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                postRun();
            }
        }, 1000);// 时间可以慢慢调整
        */
    }

    private void postRun() {
        if (isSeriousError())
            return;
        initIP();
        UserTool.setLoginState(Welcome.this, false);// 先保存为未登录状态，然后在login中修改
        if (!UserStateTool.isLoginEver(Welcome.this)) {
            try {
                LoginSampleHelper.getInstance().initIMKitTemp();
            } catch (Exception e) {
            }
        }
        updateTimestamp();
    }

    private void initIP() {
        ServerURL.getIP();
    }




    private boolean isSeriousError() {
        String value = ParamTool.getParam("serious_error");
        if (value.equals("1")) {
            Toast.makeText(this, "系统正在维护", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AboutMe.class);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private void getMessage() {
        msg = getIntent().getStringExtra("note_extra");
        if (msg == null)
            msg = "";
    }

    private void autoLogin() {
        String[] user = UserTool.getUser(this);
        name = user[0];
        pass = user[1];
        if (name.equals("") || pass.length() < 6) {// 未登录过，或者是测试的
            pass = "";
            toLoginActivity();
            return;
        }
        // 有登录记录，到login中自动登录（减少重复代码）
        startLogin();
    }

    private void toLoginActivity() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    private void toMainActivity() {
        //if (!msg.equals("")) {}
        Intent intent = new Intent(this, MainMain.class);
        startActivity(intent);
        finish();
    }

    private void startLogin() {
        ConnectEasy.POSTLOGIN(this, ServerURL.LOGIN, new ConnectListener() {
            public ConnectDialog showDialog(ConnectDialog dialog) {
                return null;
            }

            public ConnectList setParam(ConnectList list) {
                list.put("appSecret", Login.APP_SECRET);
                list.put("username", name);
                list.put("password", pass);
                return list;
            }

            public void onResponse(String response) {
                loginBaiChuan(response); // 失败等都在这里面处理了
            }
        });
    }

    private void loginBaiChuan(String response) {
        String pass = MsgTool.dealResponseGetPass(response, name);
        String name = null;
        try {
            name = BaiChuanUtils.getUserName(Long.parseLong(InfoTool.getUserID(this)));
        } catch (Exception e) {
            return;
        }
        // 保存登录状态
        UserTool.setLoginState(Welcome.this, true);// 保存为登录状态
        BaiChuanUtils.login(name, pass, null);//在后台慢慢地登录去吧
        CacheTool.clearCache();//清空缓存
        //等待3秒钟，减少网络占用
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AppUtil.dismissProgressDialog();
                toMainActivity();
            }
        }, 3000);
//        AppUtil.dismissProgressDialog();
//        if (state) {
//            toMainActivity();
//        } else {
//            BaiChuanUtils.showToast("登录失败");
//            toLoginActivity();
//        }
    }

    @Override
    protected void onDestroy() {
        need_close = true;
        System.gc();
        super.onDestroy();
    }


    @Override
    public void receiveMessage(String what) {
    }

    @Override
    public void newThread() {
        while (!need_close) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            sendMessage(null);
            if (need_close)
                return;
            try {
                Thread.sleep(900);
            } catch (Exception e) {
            }
            sendMessage("");
        }
    }

    private void updateTimestamp() {
        StaticMethod.POST(this, ServerURL.TIME_STAMP, new ConnectListener() {
            @Override
            public ConnectList setParam(ConnectList list) {
                return null;
            }

            @Override
            public ConnectDialog showDialog(ConnectDialog dialog) {
                return null;
            }

            @Override
            public void onResponse(String response) {
                ConnectSign.dealTimeSpace(response);//内部已处理

                if (UserTool.isFirstRun(Welcome.this)) {
                    startActivity(new Intent(Welcome.this, FirstRun.class));
                    finish();
                } else {
                    autoLogin();
                }
            }
        });
    }

}
