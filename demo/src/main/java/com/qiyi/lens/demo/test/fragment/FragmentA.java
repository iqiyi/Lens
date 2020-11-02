package com.qiyi.lens.demo.test.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentA extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){

        TextView view = new TextView(getActivity());
        view.setText(this.getClass().getSimpleName());
        view.setGravity(Gravity.CENTER);
        view.setTextSize(30);
        view.setTextColor(Color.WHITE);
        view.setBackgroundColor(Color.BLACK);
        return view;
    }

}
