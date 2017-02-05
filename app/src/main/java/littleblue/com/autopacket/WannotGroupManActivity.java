package littleblue.com.autopacket;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WannotGroupManActivity extends AppCompatActivity {
    private final String TAG = "RedPacket.WannotGroupManActivity";

    private Context mContext;
    private Button mGroupAddButton;
    private Button mManAddButton;
    private ArrayList<String> mGroupNameList = new ArrayList<>();
    private String mGroupNamesStr = "";
    private ListView mGroupListView;
    private ArrayList<String> mManNameList = new ArrayList<>();
    private String mManNamesStr = "";
    private ListView mManListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wannot_group_man);

        mContext = this;
        mGroupAddButton = (Button) findViewById(R.id.button_wannot_group_add);
        mManAddButton = (Button) findViewById(R.id.button_wannot_man_add);
        mGroupListView = (ListView) findViewById(R.id.wannot_group_list);
        mManListView = (ListView) findViewById(R.id.wannot_man_list);

        mGroupAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(true);
            }
        });
        mManAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(false);
            }
        });

        mGroupNamesStr = Utils.getWannotGroupNames(mContext);
        mManNamesStr = Utils.getWannotManNames(mContext);
        if (mGroupNamesStr.length() > 0) {
            String[] strArray = mGroupNamesStr.split("`");
            for (int i = 0; i < strArray.length; i++) {
                Utils.logI(TAG, "mGroupNameList " + strArray[i]);
                mGroupNameList.add(strArray[i]);
            }
        }
        if (mManNamesStr.length() > 0) {
            String[] strArray = mManNamesStr.split("`");
            for (int i = 0; i < strArray.length; i++) {
                mManNameList.add(strArray[i]);
            }
        }
        initGroupList();
        initManList();

        LinearLayout groupLayout = (LinearLayout) findViewById(R.id.group_layout);
        groupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGroupClickPosition > -1) {
                    mGroupClickPosition = -1;
                    initGroupList();
                }
            }
        });

        LinearLayout manLayout = (LinearLayout) findViewById(R.id.man_layout);
        manLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mManClickPosition > -1) {
                    mManClickPosition = -1;
                    initManList();
                }
            }
        });
    }

    private int mGroupClickPosition = -1;
    private int mManClickPosition = -1;
    private void initGroupList() {
        ImageView dividerTop = (ImageView) findViewById(R.id.group_divider_top);
        ImageView dividerBottom = (ImageView) findViewById(R.id.group_divider_bottom);
        if (mGroupNameList.size() > 0) {
            dividerTop.setVisibility(View.VISIBLE);
            dividerBottom.setVisibility(View.VISIBLE);
            ArrayAdapter mGroupArrayAdapter = new ArrayAdapter(mContext, R.layout.name_item_layout, mGroupNameList);
            mGroupListView.setAdapter(mGroupArrayAdapter);
            mGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Utils.logI(TAG, "mGroupListView onItemClick position" + position);
                    TextView textView = (TextView) view;
                    if (mGroupClickPosition == position) {
                        mGroupClickPosition = -1;
                        mGroupNameList.remove(position);
                        initGroupList();
                        saveGroupNames();
                    } else if (mGroupClickPosition > -1) {
                        mGroupClickPosition = -1;
                        initGroupList();
                    } else {
                        textView.setText(R.string.clear);
                        textView.setTextColor(Color.RED);
                        mGroupClickPosition = position;
                    }
                }
            });
        } else {
            ArrayAdapter mGroupArrayAdapter = new ArrayAdapter(mContext, R.layout.name_item_layout);
            mGroupListView.setAdapter(mGroupArrayAdapter);
            dividerTop.setVisibility(View.INVISIBLE);
            dividerBottom.setVisibility(View.INVISIBLE);
        }
    }


    private void initManList() {
        ImageView dividerTop = (ImageView) findViewById(R.id.man_divider_top);
        ImageView dividerBottom = (ImageView) findViewById(R.id.man_divider_bottom);
        if (mManNameList.size() > 0) {
            dividerTop.setVisibility(View.VISIBLE);
            dividerBottom.setVisibility(View.VISIBLE);
            ArrayAdapter mManArrayAdapter = new ArrayAdapter(mContext, R.layout.name_item_layout, mManNameList);
            mManListView.setAdapter(mManArrayAdapter);
            mManListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Utils.logI(TAG, "mManListView onItemClick position" + position);
                    TextView textView = (TextView) view;
                    if (mManClickPosition == position) {
                        mManClickPosition = -1;
                        mManNameList.remove(position);
                        initManList();
                        saveManNames();
                    } else if (mManClickPosition > -1) {
                        mManClickPosition = -1;
                        initManList();
                    } else {
                        textView.setText(R.string.clear);
                        textView.setTextColor(Color.RED);
                        mManClickPosition = position;
                    }
                }
            });
        } else {
            ArrayAdapter mManArrayAdapter = new ArrayAdapter(mContext, R.layout.name_item_layout);
            mManListView.setAdapter(mManArrayAdapter);
            dividerTop.setVisibility(View.INVISIBLE);
            dividerBottom.setVisibility(View.INVISIBLE);
        }
    }

    private void saveGroupNames() {
        mGroupNamesStr = "";
        for (String name : mGroupNameList) {
            if ("".endsWith(mGroupNamesStr)) {
                mGroupNamesStr = name;
            } else {
                mGroupNamesStr = mGroupNamesStr + "`" + name;
            }
        }
        Utils.saveWannotGroupNames(mContext, mGroupNamesStr);
    }

    private void saveManNames() {
        mManNamesStr = "";
        for (String name : mManNameList) {
            if ("".endsWith(mManNamesStr)) {
                mManNamesStr = name;
            } else {
                mManNamesStr = mManNamesStr + "`" + name;
            }
        }
        Utils.saveWannotManNames(mContext, mManNamesStr);
    }

    private void showInputDialog(final Boolean isGroup) {
        final EditText editTextView = new EditText(mContext);
        editTextView.setGravity(Gravity.CENTER);
        editTextView.setFocusable(true);
        //editTextView.setFocusableInTouchMode(true);
        //editTextView.requestFocus();
        //editTextView.setShowSoftInputOnFocus(true);
        //InputMethodManager inputManager = (InputMethodManager) editTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //inputManager.showSoftInput(editTextView, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(isGroup? R.string.input_wannot_group_list : R.string.input_wannot_man_list);
        builder.setView(editTextView);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editTextView.getText().toString();
                if (TextUtils.isEmpty(name)) return;
                if (isGroup) {
                    mGroupNameList.add(name);
                    if (mGroupNameList.size() <= 1) {
                        initGroupList();
                    }
                    mGroupNamesStr = mGroupNamesStr.length() > 0 ? mGroupNamesStr+"`"+name : name;
                    Utils.saveWannotGroupNames(mContext, mGroupNamesStr);
                } else {
                    mManNameList.add(name);
                    if (mManNameList.size() <= 1) {
                        initManList();
                    }
                    mManNamesStr = mManNamesStr.length() > 0 ? mManNamesStr+"`"+name : name;
                    Utils.saveWannotManNames(mContext, mManNamesStr);
                }
            }
        });
        builder.show();
    }


}
