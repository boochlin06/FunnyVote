package com.heaton.funnyvote.ui.about;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.heaton.funnyvote.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by heaton on 2017/3/4.
 */

public class LicenceItemAdapter extends RecyclerView.Adapter<LicenceItemAdapter.VHLicenceItem> {
    private List<LicenceActivity.LicenceItem> licenceList;

    private void setLicenceList(List<LicenceActivity.LicenceItem> licenceList) {
        this.licenceList = licenceList;
    }

    public LicenceItemAdapter(List<LicenceActivity.LicenceItem> licenceList) {
        this.licenceList = licenceList;
    }

    @Override
    public VHLicenceItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item_licence
                , parent, false);
        return new VHLicenceItem(v);
    }

    @Override
    public void onBindViewHolder(VHLicenceItem holder, int position) {
        holder.txtLicenceTitle.setText(licenceList.get(position).getTitle());
        holder.txtLicenceDesc.setText(licenceList.get(position).getDesc());
    }


    @Override
    public int getItemCount() {
        return licenceList.size();
    }

    static class VHLicenceItem extends RecyclerView.ViewHolder {
        @BindView(R.id.txtLicenceTitle)
        TextView txtLicenceTitle;
        @BindView(R.id.txtLicenceDesc)
        TextView txtLicenceDesc;

        VHLicenceItem(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
