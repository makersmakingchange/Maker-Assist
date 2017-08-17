package com.makersmakingchange.maker_assist.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.makersmakingchange.maker_assist.Manager.FeatureItem;
import com.makersmakingchange.maker_assist.R;

import java.util.ArrayList;

/**************************************************
 **************Makers Making Change****************
 **************************************************
 ****Developed by Milad Hajihassan on 3/28/2017.***
 **************************************************
 **************************************************/

public class QuickMenuAdapter  extends ArrayAdapter<FeatureItem> {

    ArrayList<FeatureItem> featureList = new ArrayList<>();

    public QuickMenuAdapter(Context context, int textViewResourceId, ArrayList<FeatureItem> objects) {
        super(context, textViewResourceId, objects);
        featureList = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.list_item_quickmenu, null);

        final TextView textViewQuickMenuIcon = (TextView) v.findViewById(R.id.textView_quickmenu_icon);
        final TextView textViewQuickMenuFeature = (TextView) v.findViewById(R.id.textView_quickmenu_feature);

        Typeface font = Typeface.createFromAsset( getContext().getAssets(), "lipsync-icons20.ttf" );
        textViewQuickMenuIcon.setTypeface(font);

        //Set textViewQuickMenuIcon and textViewQuickMenuFeature texts
        textViewQuickMenuIcon.setText(featureList.get(position).getFeatureFont());
        textViewQuickMenuFeature.setText(featureList.get(position).getFeatureName());
        return v;

    }

}