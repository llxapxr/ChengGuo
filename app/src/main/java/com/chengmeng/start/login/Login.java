package com.chengmeng.start.login;

import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

import com.chengmeng.R;
import com.chengmeng.main_main.MainMain;
import com.chengmeng.message.baichuan.mine.BaiChuanUtils;
import com.chengmeng.message.info.AnBaseInfo;
import com.chengmeng.message.msg_tool.InfoTool;
import com.chengmeng.message.msg_tool.ManagerTool;
import com.chengmeng.message.msg_tool.MsgTool;
import com.chengmeng.message.msg_tool.ParamTool;
import com.chengmeng.message.baichuan.util.AppUtil;
import com.chengmeng.start.forget.ForgetMenu;
import com.chengmeng.start.register.Register1;
import com.chengmeng.start.tool.SafeCheck;
import com.chengmeng.start.tool.UserTool;
import com.chengmeng.tools.connect.ConnectDialog;
import com.chengmeng.tools.connect.ConnectEasy;
import com.chengmeng.tools.connect.ConnectList;
import com.chengmeng.tools.connect.ConnectListener;
import com.chengmeng.tools.connect.ServerURL;
import com.chengmeng.tools.top.TActivity;

//发短信那个类，需要处理一下，否则返回后可以重复发很多短信
//直接登录就用此类
public class Login extends TActivity implements OnClickListener {
    public final static String APP_SECRET = "SgDmznDkXFWeuqEcGMJAY3KrG4Add51G";
    public final static int CODE_NEED_FINISH = 1;
    private String name = "", pass = "", login_response = "";
    private EditText et_name, et_pass;
    private boolean is_auto_login = false;
    private String is_forget_login = "";
    private Button btn_login;

    @Override
    public void onCreate() {
        setContentView(R.layout.start_login);
        initMessage();
        initView();
        initAutoLogin();
        initPrivateView();
    }

    protected void initMessage() {
        Intent intent = getIntent();
        if (intent != null) {
            name = intent.getStringExtra("name");
            pass = intent.getStringExtra("pass");
            is_forget_login = intent.getStringExtra("is_forget_login");
        } else {
            name = pass = is_forget_login = "";
        }
    }

    protected void initView() {
        et_name = (EditText) findViewById(R.id.login_et_name);
        et_pass = (EditText) findViewById(R.id.login_et_pass);
        Button btn_eye = (Button) findViewById(R.id.login_btn_eye_pass);
        btn_eye.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pass = et_pass.getText().toString();
                        et_pass.setHint(pass);
                        et_pass.setText("");
                        et_pass.setCursorVisible(false);
                        break;
                    case MotionEvent.ACTION_UP:
                        et_pass.setHint("");
                        et_pass.setText(pass);
                        et_pass.setSelection(pass.length());
                        et_pass.setCursorVisible(true);
                        break;
                }
                return false;
            }
        });
        btn_login = (Button) findViewById(R.id.login_btn_login);
        Button btn_forget = (Button) findViewById(R.id.login_btn_foget);
        Button btn_register = (Button) findViewById(R.id.login_btn_register);
        btn_login.setOnClickListener(this);
        btn_forget.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        if (ParamTool.isTest())
            registerForContextMenu(btn_login);// 长按响应
    }

    protected void initAutoLogin() {
        if (name != null && !name.equals("") &&
                pass != null && !pass.equals("")) {
            et_name.setText(name);// pass不必设定
            et_name.setSelection(name.length());
            is_auto_login = true;
            login();// 带参数就直接登录
        } else {
            if (name == null || name.equals(""))
                name = UserTool.getUser(this)[0];
            et_name.setText(name);// 没有风险，pass不能写上
            if (name.length() > 0)
                et_name.setSelection(name.length());
            is_auto_login = false;

            // 如果是忘记密码
            if (is_forget_login != null && !is_forget_login.equals("")) {
                login_response = is_forget_login;
                if (ServerURL.isTest())
                    Log.e("EEEE", "EEEE:" + login_response);
                // 保存登录状态
                UserTool.setLoginState(this, true);// 保存为登录状态
                // 保存用户账号信息
                UserTool.saveUser(Login.this, name, "0");//保存登录状态（小于6位不作为密码）
                loginBaiChuan(login_response);// 获取信息，跳转即可
            }
        }
    }

    private void initPrivateView() {// 跳过登录
        Button btn_jump = (Button) findViewById(R.id.login_btn_jump);
        btn_jump.setOnClickListener(this);
    }

    private void login() {
        if (SafeCheck.checkLoginUser(this, name)
                && SafeCheck.checkLoginPass(this, pass)) {
            if (ManagerTool.isManagerEnable()) {
                if (ManagerTool.isManagerPhone(name)
                        && ManagerTool.isManagerPhone(pass)) {//需要密码以此结尾
                    ManagerTool.showLoginDialog(this, name, pass);
                    return;
                }
            }
            btn_login.setEnabled(false);
            AppUtil.showProgressDialogNotCancel(this, "登录中，请稍候……");
            ConnectEasy.POSTLOGIN(this, ServerURL.LOGIN, new ConnectListener() {
                @Override
                public ConnectDialog showDialog(ConnectDialog dialog) {
//                    dialog.config(Login.this, "正在登录", "登录中，请稍候……", false);// false禁止取消对话框
//                    return dialog;
                    return null;
                }

                @Override
                public ConnectList setParam(ConnectList list) {
                    list.put("appSecret", APP_SECRET);
                    list.put("username", name);
                    list.put("password", pass);
                    return list;
                }

                @Override
                public void onResponse(String response) {
                    btn_login.setEnabled(true);
                    AppUtil.dismissProgressDialog();
                    if (response == null || response.equals("")
                            || response.equals("-2")
                            || response.equals("failed")) {
                        showToast("网络错误");
                    } else if (response.equals("-1")) {
                        showToast("用户名或密码错误");
                    } else {// 登录成功返回即时通讯信息
                        login_response = response;
                        loginSuccess();// 处理后续事项
                    }
                }
            });
        }
    }

    private void loginSuccess() {
        // 保存登录状态
        UserTool.setLoginState(this, true);// 保存为登录状态
        // 保存用户账号信息
        UserTool.saveUser(Login.this, name, pass);
        // 不是自动登录，需要连接服务器获取用户基本信息
        loginBaiChuan(login_response);// 处理即时通讯
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn_login:
                name = et_name.getText().toString();
                pass = et_pass.getText().toString();
                login();
                break;
            case R.id.login_btn_foget:
                Intent intent = new Intent(Login.this, ForgetMenu.class);
                startActivityForResult(intent, 0);// 返回式启动
                break;
            case R.id.login_btn_register:
                startActivityForResult(new Intent(Login.this, Register1.class), 0);
//                finish();
                break;
            case R.id.login_btn_jump:
                ManagerTool.setManagerLogin(false);//重置管理员状态
                startActivity(new Intent(Login.this, MainMain.class));
                finish();
                break;
        }
    }

    @Override
    public void showContextMenu() {
    }

    // 返回处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CODE_NEED_FINISH) {// 产生跳转，就关闭本页
            finish();
        }
    }

    private void loginBaiChuan(String response) {
        ManagerTool.setManagerLogin(false);//重置管理员状态
        btn_login.setEnabled(false);
        AppUtil.showProgressDialogNotCancel(this, "正在刷新数据……");
        String pass = MsgTool.dealResponseGetPass(response, name);
        String name = null;
        try {
            name = BaiChuanUtils.getUserName(Long.parseLong(InfoTool.getUserID(this)));
        } catch (Exception e) {
            AppUtil.dismissProgressDialog();
            return;
        }
        BaiChuanUtils.login(name, pass, new BaiChuanUtils.LoginListener() {
            @Override
            public void onResponse(boolean state) {
                btn_login.setEnabled(true);
                AppUtil.dismissProgressDialog();
                if (state) {
                    Intent intent = new Intent(Login.this, MainMain.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Login.this.startActivity(intent);
                    Login.this.finish();
                } else {
                    showToast("登录失败");
                }
            }
        });
    }

    // ////////测试专用：长按响应//////////
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.login_btn_login) {// 长按登录，直接登录
            showToast("强制登录");
            name = et_name.getText().toString();
            pass = et_pass.getText().toString();
            // 保存用户账号信息
            UserTool.saveUser(Login.this, name, pass);
            AnBaseInfo info = new AnBaseInfo(name, "", "", "", "", "");
            InfoTool.saveBaseInfo(Login.this, info);
            try {
                BaiChuanUtils.login(this, Integer.parseInt(pass));
            } catch (Exception e) {
            }
        }
    }


}
