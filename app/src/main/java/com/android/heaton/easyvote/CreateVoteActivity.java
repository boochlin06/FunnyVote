package com.android.heaton.easyvote;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by heaton on 16/1/10.
 */
public class CreateVoteActivity extends Activity {
    @Bind(R.id.txtTitle)
    TextView txtTitle;
    @Bind(R.id.edtTitle)
    EditText edtTitle;
    @Bind(R.id.txtDescription)
    TextView txtDescription;
    @Bind(R.id.edtDescription)
    EditText edtDescription;
    @Bind(R.id.txtOption)
    TextView txtOption;
    @Bind(R.id.txtMaxChoice)
    TextView txtMaxChoice;
    @Bind(R.id.edtMaxOption)
    EditText edtMaxOption;
    @Bind(R.id.txtMinChoice)
    TextView txtMinChoice;
    @Bind(R.id.edtMinOption)
    EditText edtMinOption;
    @Bind(R.id.rycOption)
    RecyclerView rycOption;
    @Bind(R.id.chbSetPassword)
    CheckBox chbSetPassword;
    @Bind(R.id.chbMemberOnly)
    CheckBox chbMemberOnly;
    @Bind(R.id.btnCreate)
    Button btnCreate;
    private OptionAdapter optionAdapter;
    private List<Option> OptionList;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_cteate_vote);
        ButterKnife.bind(this);
        initOptionData();
        setUpViewComponment();

    }

    private void setUpViewComponment() {

        btnCreate = (Button) findViewById(R.id.btnCreate);

        //viewAddOption = LayoutInflater.from(this).inflate(R.layout.option_item_add,null,false);

        rycOption = (RecyclerView) findViewById(R.id.rycOption);
        rycOption.setLayoutManager(new LinearLayoutManager(this));
        optionAdapter = new OptionAdapter();
        rycOption.setAdapter(optionAdapter);

        chbMemberOnly = (CheckBox) findViewById(R.id.chbMemberOnly);
        chbSetPassword = (CheckBox) findViewById(R.id.chbSetPassword);

    }

    private void initOptionData() {
        OptionList = new ArrayList<>();
    }

    class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.OptionViewHolder> {
        public static final int TYPE_OPTION_NORMAL = 0;
        public static final int TYPE_OPTION_ADD = 1;

        @Override
        public OptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            OptionViewHolder holder = new OptionViewHolder(LayoutInflater.from(context).inflate(R.layout.item_vote_create_option, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(OptionViewHolder holder, int position) {
            holder.txtId.setText(position);
            holder.edtOptionContet.setText(OptionList.get(position).getContent());
        }

        @Override
        public int getItemCount() {
            return OptionList.size();
        }

        class OptionViewHolder extends RecyclerView.ViewHolder {
            TextView txtId;
            EditText edtOptionContet;
            Button btnDelete;


            public OptionViewHolder(View itemView) {
                super(itemView);
                txtId = (TextView) itemView.findViewById(R.id.txtId);
                edtOptionContet = (EditText) itemView.findViewById(R.id.edtContent);
                btnDelete = (Button) findViewById(R.id.btnDelete);

            }
        }

        @Override
        public int getItemViewType(int position) {
            return position == OptionList.size() ? TYPE_OPTION_ADD : TYPE_OPTION_NORMAL;
        }

    }

}
