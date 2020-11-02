/*
 *
 * Copyright (C) 2020 iQIYI (www.iqiyi.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.qiyi.lens.demo;

import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.qiyi.lens.LensUtil;
import com.qiyi.lens.ui.viewinfo.json.Frame;
import com.qiyi.lens.utils.LL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestViewInfoActivity extends AppCompatActivity {

    private Button button;
    public EditText editText;
    private CheckBox checkBox;
    public ImageView imageView;
    public TextView nullTextView;
    private int number = 99999;


    public List<TestClzz> testObjLst = new ArrayList<>();
    public List<String> testStrLst = new ArrayList<>();
    public int[] arr = new int[]{2, 3, 1, 4};
    public Map<String, Integer> map = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_view_info);


        testObjLst.add(new TestClzz("abc"));
        testObjLst.add(new TestClzz("def"));
        testObjLst.add(new TestClzz("ghi"));
        testObjLst.add(new TestClzz("jkl"));

        LensUtil.watchObject("test_Obj",testObjLst);

//        WatchObjectListAdapter.ref(testObjLst);



        testStrLst.add("abc");
        testStrLst.add("def");
        testStrLst.add("hij");

        map.put("a", 1);
        map.put("b", 2);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        checkBox = findViewById(R.id.checkBox);
        LensUtil.setViewDebugInfo(checkBox, "this is my check box");



        LensUtil.setViewDebugInfo(findViewById(R.id.view2),"debug info is this hahahaha; yes its a debug info" +
                "debug info is this hahahaha; yes its a debug info");
        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LensApplicationDelegate.count++;
                LensApplicationDelegate.str = LensApplicationDelegate.count + "";
                number--;
            }
        });
        ToggleButton toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog dialog = new AlertDialog.Builder(TestViewInfoActivity.this).
                        setMessage("ghoood")
                        .create();

                dialog.show();
                WindowManager.LayoutParams clp = dialog.getWindow().getAttributes();//.type;
                LL.d("","");
                clp.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;

            }
        });

        Toast.makeText(this, "Test toast", Toast.LENGTH_LONG).show();

        LensUtil.watchObject("img", imageView);


        View view = new View(this);
        view.setBackgroundColor(0xccffff);
        FrameLayout fr = findViewById(android.R.id.content);
        fr.addView(view);

    }

    static class TestClzz  {
        private String test;
        public TestClzz(String test) {
            this.test = test;
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        testObjLst.clear();
        testObjLst = null;
        System.gc();
    }

}
