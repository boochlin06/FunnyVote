package com.android.heaton.easyvote;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by heaton on 2016/8/21.
 */

public class VoteDetailContent extends AppCompatActivity {

    @BindView(R.id.imgAuthorIcon)
    ImageView imgAuthorIcon;
    @BindView(R.id.txtAuthorName)
    TextView txtAuthorName;
    @BindView(R.id.txtPubTime)
    TextView txtPubTime;
    @BindView(R.id.imgFavorite)
    ImageView imgFavorite;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.imgClockIcon)
    ImageView imgClockIcon;
    @BindView(R.id.imgMain)
    ImageView imgMain;
    @BindView(R.id.ryOptionArea)
    RecyclerView ryOptionArea;
    ArrayList OptionList;
    private Toolbar toolbar;

    private OptionItemAdapter optionItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_detail);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("Detail");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setElevation(10);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txtAuthorName.setText("Heaton Lin");
        txtPubTime.setText("10/03 ~ 10/21");
        txtTitle.setText("Do your mother know what you do in front of computer?");

        optionItemAdapter = new OptionItemAdapter(this , VoteDataLoader.getInstance(this).queryOptionsByVoteId(10));
        ryOptionArea.setAdapter(optionItemAdapter);

        ryOptionArea.setLayoutManager(new LinearLayoutManager(this));
        ryOptionArea.setVisibility(View.VISIBLE);
        ryOptionArea.setNestedScrollingEnabled(false);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_content_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }
}
