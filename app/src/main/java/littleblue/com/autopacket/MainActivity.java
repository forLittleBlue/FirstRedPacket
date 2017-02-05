package littleblue.com.autopacket;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private String TAG = "RedPacket.MainActivity";

    private Context mContext;
    private Button mStartButton;
    private Button mAboutButton;
    private Button mAboutTextButton;
    private EditText mWeixinNameEdit;
    private Button mEditOkButton;
    private RelativeLayout mEditOkView;
    private static String mService = "littleblue.com.firstredpacket/.OpenRedPacketService";
    private boolean isAccSeviceEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mStartButton = (Button) findViewById(R.id.button_start);
        mAboutButton = (Button) findViewById(R.id.button_about);
        mAboutTextButton = (Button) findViewById(R.id.button_about_text);
        mWeixinNameEdit = (EditText) findViewById(R.id.editText_weixin_name);
        mEditOkButton = (Button) findViewById(R.id.button_ok);
        mEditOkView = (RelativeLayout) findViewById(R.id.relativeLayout_ok);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAccSeviceEnabled = getServiceIsEnabled();
                if (!isAccSeviceEnabled) {
                    Intent intent =  new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Utils.showToastView(mContext,  getString(R.string.toast_for_start_service, getString(R.string.app_name)), Toast.LENGTH_LONG);
                } else {

                    Intent intent =  new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Utils.showToastView(mContext, getString(R.string.toast_restart_service, getString(R.string.app_name)), Toast.LENGTH_LONG);
                }
            }
        });

        mAboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator alpha = ObjectAnimator.ofFloat(mAboutButton, "alpha", 1.0f, 0.0f);
                alpha.setDuration(600).start();
                mAboutButton.setEnabled(false);

                mAboutTextButton.setVisibility(View.VISIBLE);
                ObjectAnimator alphaText = ObjectAnimator.ofFloat(mAboutTextButton, "alpha", 0.0f, 1.0f);
                ObjectAnimator yText = ObjectAnimator.ofFloat(mAboutTextButton, "translationY", 300, 0);
                AnimatorSet set = new AnimatorSet();
                set.play(alphaText).with(yText);
                set.setDuration(700).start();
            }
        });

        OpenRedPacketService.mWeiXinName = Utils.getWeiXinName(mContext);
        final boolean isSaved = !OpenRedPacketService.mWeiXinName.equals(Utils.NO_INPUT);
        if (isSaved) {
            mEditOkButton.setEnabled(true);
            mWeixinNameEdit.setCursorVisible(false);
            mWeixinNameEdit.setText(OpenRedPacketService.mWeiXinName);
        } else {
            mEditOkButton.setEnabled(false);
            //mEditOkView.setAlpha(0.5f);
        }
        mWeixinNameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "mWeixinNameEdit onClick");
                mEditOkView.setVisibility(View.VISIBLE);
                mWeixinNameEdit.setCursorVisible(true);
                if (mEditOkView.getAlpha() > 0) return;
                boolean saved = mWeixinNameEdit.getText().length() > 0;
                ObjectAnimator alphaText = ObjectAnimator.ofFloat(mEditOkView, "alpha", 0.0f, saved ? 1.0f : 0.5f);
                ObjectAnimator yText = ObjectAnimator.ofFloat(mEditOkView, "translationY", 200, 0);
                AnimatorSet set = new AnimatorSet();
                set.play(alphaText).with(yText);
                set.setDuration(800).start();
            }
        });

        mWeixinNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    mEditOkButton.setEnabled(true);
                    mEditOkView.setAlpha(1.0f);
                } else {
                    mEditOkButton.setEnabled(false);
                    mEditOkView.setAlpha(0.5f);
                }
            }
        });

        mEditOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mWeixinNameEdit.getText().toString();
                if (name.length() > 0) {
                    Utils.saveWeiXinName(mContext, name);
                    OpenRedPacketService.mWeiXinName = name;
                    Utils.showToastView(mContext, getString(R.string.weixin_name_saved_toast), Toast.LENGTH_SHORT);
                }
                mWeixinNameEdit.setCursorVisible(false);
                ObjectAnimator alphaText = ObjectAnimator.ofFloat(mEditOkView, "alpha", 1.0f, 0.0f);
                ObjectAnimator yText = ObjectAnimator.ofFloat(mEditOkView, "translationY", 0, 200);
                AnimatorSet set = new AnimatorSet();
                set.play(alphaText).with(yText);
                set.setDuration(800).start();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        Button wannotGroupMan = (Button) findViewById(R.id.button_wannot_group_man);
        wannotGroupMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WannotGroupManActivity.class);
                startActivity(intent);
            }
        });

        startService(new Intent(this, ImplService.class));
        startService(new Intent(this, RemoteService.class));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction()==MotionEvent.ACTION_DOWN){
            if (mAboutTextButton.getAlpha() == 1.0f) {
                aboutTextDispear();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void aboutTextDispear() {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mAboutButton, "alpha", 0.0f, 1.0f);
        alpha.setDuration(600).start();
        mAboutButton.setEnabled(true);

        ObjectAnimator alphaText = ObjectAnimator.ofFloat(mAboutTextButton, "alpha", 1.0f, 0.0f);
        ObjectAnimator yText = ObjectAnimator.ofFloat(mAboutTextButton, "translationY", 0, 500);
        AnimatorSet set = new AnimatorSet();
        set.play(alphaText).with(yText);
        set.setDuration(700).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAccSeviceEnabled = getServiceIsEnabled();
        if (isAccSeviceEnabled) {
            mStartButton.setText(getString(R.string.restart_service));
        } else {
            mStartButton.setText(getString(R.string.start_service));
        }
    }

    private boolean getServiceIsEnabled() {
        boolean isEnabled = false;
        /*AccessibilityManager abm = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfoList = abm.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo serviceInfo : serviceInfoList) {
            Log.i(TAG, "AccessibilityServiceInfo : " + serviceInfo.getId());
            if (mService.equals(serviceInfo.getId())) {
                isEnabled = true;
                break;
            }
        }*/
        //第二种方法
        try {
            isEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED) == 1;
        } catch (Settings.SettingNotFoundException e) {
            Log.i(TAG, e.getMessage());
        }
        return isEnabled;
    }


}
