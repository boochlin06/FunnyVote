package com.heaton.funnyvote.ui.about.licence


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.heaton.funnyvote.R
import kotlinx.android.synthetic.main.card_view_item_licence.view.*

/**
 * Created by heaton on 2017/3/4.
 */

class LicenceItemAdapter(
        private var licenceList: List<LicenceActivity.LicenceItem>?
) : RecyclerView.Adapter<LicenceItemAdapter.VHLicenceItem>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHLicenceItem {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_item_licence, parent, false)
        return VHLicenceItem(v)
    }

    override fun onBindViewHolder(holder: VHLicenceItem, position: Int) {
        holder.itemView.txtLicenceTitle.text = licenceList!![position].title
        holder.itemView.txtLicenceDesc.text = licenceList!![position].desc
    }


    override fun getItemCount(): Int {
        return licenceList!!.size
    }

    class VHLicenceItem(view: View) : RecyclerView.ViewHolder(view) {
        var txtLicenceTitle: TextView = view.findViewById(R.id.txtLicenceTitle)
        var txtLicenceDesc: TextView? = view.findViewById(R.id.txtLicenceDesc)
    }
}
