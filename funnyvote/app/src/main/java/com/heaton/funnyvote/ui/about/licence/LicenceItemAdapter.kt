package com.heaton.funnyvote.ui.about.licence

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heaton.funnyvote.databinding.CardViewItemLicenceBinding

class LicenceItemAdapter(
    private var licenceList: List<LicenceActivity.LicenceItem>?
) : RecyclerView.Adapter<LicenceItemAdapter.VHLicenceItem>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHLicenceItem {
        val binding = CardViewItemLicenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VHLicenceItem(binding)
    }

    override fun onBindViewHolder(holder: VHLicenceItem, position: Int) {
        val item = licenceList?.get(position)
        holder.binding.txtLicenceTitle.text = item?.title ?: ""
        holder.binding.txtLicenceDesc.text = item?.desc ?: ""
    }

    override fun getItemCount(): Int = licenceList?.size ?: 0

    class VHLicenceItem(val binding: CardViewItemLicenceBinding) : RecyclerView.ViewHolder(binding.root)
}
