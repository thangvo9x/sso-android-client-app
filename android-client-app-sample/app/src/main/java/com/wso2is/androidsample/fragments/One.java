package com.wso2is.androidsample.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wso2is.androidsample.R;


@SuppressLint("ValidFragment")
public class One extends Fragment{

    private int layout;
    public One(int layout) {
        // Required empty public constructor
        super();
        this.layout = layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(this.layout, container, false);
    }
}
