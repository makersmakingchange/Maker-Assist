package com.makersmakingchange.maker_assist.Activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.makersmakingchange.maker_assist.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**************************************************
 **************Makers Making Change****************
 **************************************************
 ****Developed by Milad Hajihassan on 3/28/2017.***
 **************************************************
 **************************************************/

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView actionBarTitle = (TextView) findViewById(R.id.actionbar_header_title);
        actionBarTitle.setText(getString(R.string.actionbar_title_help));

        //Create 2 string arrays to store help list title and description
        String[] helpListTitle = new String[]{
                getResources().getString(R.string.help_text_title1),
                getResources().getString(R.string.help_text_title2),
                getResources().getString(R.string.help_text_title3),
        };

        String[] helpListDescription = new String[]{
                getResources().getString(R.string.help_text_description1),
                getResources().getString(R.string.help_text_description2),
                getResources().getString(R.string.help_text_description3),
        };

        //Create help list with hashmap of strings
        List<HashMap<String, String>> helpList = new ArrayList<HashMap<String, String>>();
        //Add helpListTitle and helpListDescription
        for (int i = 0; i < 3; i++) {
            HashMap<String, String> hl = new HashMap<String, String>();
            hl.put("title", helpListTitle[i]);
            hl.put("description", helpListDescription[i]);
            helpList.add(hl);
        }

        String[] from = {"title", "description"};
        int[] to = {R.id.list_view_help_title, R.id.list_view_help_description};

        //Setup the helpAdapter
        SimpleAdapter helpAdapter = new SimpleAdapter(getBaseContext(), helpList, R.layout.list_view_help, from, to);
        ListView helpListView = (ListView) findViewById(R.id.list_view_help);
        helpListView.setAdapter(helpAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
