package com.heaton.funnyvote.ui.votedetail

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.heaton.funnyvote.database.Option
import kotlinx.android.synthetic.main.card_view_item_unpoll_create_new_option.view.*

/**
 * Created by heaton on 2016/9/2.
 */

class VHUnPollCreateOptionItem(
        itemView: View
        , private val itemListener: VoteDetailContentActivity.OptionItemListener
) : RecyclerView.ViewHolder(itemView) {

    private var option: Option? = null
    private var optionEditTextListener: OptionEditTextListener? = null

    fun setLayout(option: Option) {
        this.option = option
        itemView.txtOptionNumber!!.text = Integer.toString(adapterPosition + 1)
        if (itemViewType == OptionItemAdapter.OPTION_UNPOLL_VIEW_TYPE_ADD_NEW) {
            itemView.relNormal.visibility = View.INVISIBLE
            itemView.relAdd.visibility = View.VISIBLE
            itemView.imgDeleteOption.visibility = View.GONE
            itemView.imgNewOption.visibility = View.GONE
            itemView.edtOptionTitle.visibility = View.GONE
            itemView.edtOptionTitle.removeTextChangedListener(optionEditTextListener)
        } else if (itemViewType == OptionItemAdapter.OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT) {
            itemView.relNormal.visibility = View.VISIBLE
            itemView.relAdd.visibility = View.INVISIBLE
            itemView.imgNewOption.visibility = View.VISIBLE
            itemView.imgDeleteOption.visibility = View.VISIBLE
            itemView.edtOptionTitle.visibility = View.VISIBLE
            itemView.edtOptionTitle.removeTextChangedListener(optionEditTextListener)
            itemView.edtOptionTitle.setText(option.title)
            if (optionEditTextListener == null) {
                optionEditTextListener = OptionEditTextListener(itemListener)
            }
            itemView.edtOptionTitle.addTextChangedListener(optionEditTextListener)
            itemView.relAdd.setOnClickListener { itemListener.onOptionAddNew() }
            itemView.imgDeleteOption.setOnClickListener { itemListener.onOptionRemove(option.id) }
            itemView.imgNewOption.setOnClickListener {
                itemListener.onOptionAddNewCheck(itemView.edtOptionTitle.text.toString())
            }
        }
    }

    private inner class OptionEditTextListener(internal var itemListener: VoteDetailContentActivity.OptionItemListener) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            itemListener.onOptionTextChange(option!!.id!!, s.toString())
        }

        override fun afterTextChanged(s: Editable) {

        }
    }
}
