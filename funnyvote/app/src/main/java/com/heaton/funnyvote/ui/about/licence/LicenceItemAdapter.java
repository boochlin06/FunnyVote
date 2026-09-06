package com.heaton.funnyvote.ui.about.licence;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.heaton.funnyvote.databinding.CardViewItemLicenceBinding;

import java.util.List;

/**
 * Created by heaton on 2017/3/4.
 */
public class LicenceItemAdapter extends RecyclerView.Adapter<LicenceItemAdapter.VHLicenceItem> {
    private List<LicenceActivity.LicenceItem> licenceList;

    public LicenceItemAdapter(List<LicenceActivity.LicenceItem> licenceList) {
        this.licenceList = licenceList;
    }

    @NonNull
    @Override
    public VHLicenceItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardViewItemLicenceBinding binding = CardViewItemLicenceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VHLicenceItem(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VHLicenceItem holder, int position) {
        holder.binding.txtLicenceTitle.setText(licenceList.get(position).getTitle());
        holder.binding.txtLicenceDesc.setText(licenceList.get(position).getDesc());
    }

    @Override
    public int getItemCount() {
        return licenceList.size();
    }

    static class VHLicenceItem extends RecyclerView.ViewHolder {
        final CardViewItemLicenceBinding binding;

        VHLicenceItem(CardViewItemLicenceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
