package com.heaton.funnyvote.ui.createvote

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View

import com.heaton.funnyvote.database.Option

import kotlinx.android.synthetic.main.card_view_create_vote_option.view.*

/**
 * Created by heaton on 2016/9/2.
 */

class VHCreateOptionItem(itemView: View
                         , private val itemListener: CreateVoteTabOptionFragment.OptionItemListener?) : RecyclerView.ViewHolder(itemView) {

    private var optionEditTextListener: OptionEditTextListener? = null
    private lateinit var option: Option


    fun setLayout(viewType: Int, option: Option) {
        this.option = option
        if (viewType == OptionCreateItemAdapter.VIEW_TYPE_ADD_OPTION) {
            itemView.relNormal.visibility = View.INVISIBLE
            itemView.relAdd.visibility = View.VISIBLE
            itemView.imgDeleteOption.visibility = View.GONE
            itemView.edtOptionTitle.visibility = View.GONE
            itemView.edtOptionTitle.removeTextChangedListener(optionEditTextListener)
        } else if (viewType == OptionCreateItemAdapter.VIEW_TYPE_NORMAL_OPTION) {
            itemView.relNormal.visibility = View.VISIBLE
            itemView.relAdd.visibility = View.INVISIBLE
            itemView.imgDeleteOption.visibility = View.VISIBLE
            itemView.txtOptionNumber.text = ((adapterPosition + 1).toString())
            itemView.edtOptionTitle.visibility = View.VISIBLE
            itemView.edtOptionTitle.removeTextChangedListener(optionEditTextListener)
            itemView.edtOptionTitle.setText(option.title)
            if (optionEditTextListener == null) {
                optionEditTextListener = OptionEditTextListener(itemListener!!)
            }
            itemView.edtOptionTitle.addTextChangedListener(optionEditTextListener)
        }
        itemView.relAdd.setOnClickListener { itemListener!!.onOptionAddNew() }
        itemView.imgDeleteOption.setOnClickListener { itemListener!!.onOptionRemove(option.id) }
    }

    private inner class OptionEditTextListener(
            internal var itemListener: CreateVoteTabOptionFragment.OptionItemListener?) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            itemListener!!.onOptionTextChange(option.id, s.toString())
        }

        override fun afterTextChanged(s: Editable) {

        }
    }
}
