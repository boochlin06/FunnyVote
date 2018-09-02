package com.heaton.funnyvote.ui.createvote

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.Option


/**
 * Created by heaton on 2016/9/2.
 */

class OptionCreateItemAdapter(private val optionList: List<Option>?, private val itemListener: CreateVoteTabOptionFragment.OptionItemListener) : RecyclerView.Adapter<VHCreateOptionItem>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHCreateOptionItem {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_create_vote_option, parent, false)
        return VHCreateOptionItem(v, itemListener)
    }

    override fun onBindViewHolder(holder: VHCreateOptionItem, position: Int) {
        if (position == optionList!!.size) {
            holder.setLayout(VIEW_TYPE_ADD_OPTION, Option())
        } else {
            holder.setLayout(VIEW_TYPE_NORMAL_OPTION, optionList[position])
        }
    }


    override fun getItemCount(): Int {
        return if (optionList == null) 0 else optionList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == optionList!!.size) VIEW_TYPE_ADD_OPTION else VIEW_TYPE_NORMAL_OPTION
    }

    companion object {

        var VIEW_TYPE_NORMAL_OPTION = 1
        var VIEW_TYPE_ADD_OPTION = 2
    }
}
