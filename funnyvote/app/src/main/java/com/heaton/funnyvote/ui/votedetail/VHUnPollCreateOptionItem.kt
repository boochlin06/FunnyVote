package com.heaton.funnyvote.ui.votedetail

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.databinding.CardViewItemUnpollCreateNewOptionBinding

class VHUnPollCreateOptionItem(
    val binding: CardViewItemUnpollCreateNewOptionBinding,
    private val itemListener: VoteDetailContentActivity.OptionItemListener
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        itemView: View,
        itemListener: VoteDetailContentActivity.OptionItemListener
    ) : this(CardViewItemUnpollCreateNewOptionBinding.bind(itemView), itemListener)

    private var option: Option? = null
    private var optionEditTextListener: OptionEditTextListener? = null

    fun setLayout(option: Option) {
        this.option = option
        binding.txtOptionNumber.text = (adapterPosition + 1).toString()
        if (itemViewType == OptionItemAdapter.OPTION_UNPOLL_VIEW_TYPE_ADD_NEW) {
            binding.relNormal.visibility = View.INVISIBLE
            binding.relAdd.visibility = View.VISIBLE
            binding.imgDeleteOption.visibility = View.GONE
            binding.imgNewOption.visibility = View.GONE
            binding.edtOptionTitle.visibility = View.GONE
            binding.edtOptionTitle.removeTextChangedListener(optionEditTextListener)
            binding.relAdd.setOnClickListener { itemListener.onOptionAddNew() }
        } else if (itemViewType == OptionItemAdapter.OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT) {
            binding.relNormal.visibility = View.VISIBLE
            binding.relAdd.visibility = View.INVISIBLE
            binding.imgNewOption.visibility = View.VISIBLE
            binding.imgDeleteOption.visibility = View.VISIBLE
            binding.edtOptionTitle.visibility = View.VISIBLE
            binding.edtOptionTitle.removeTextChangedListener(optionEditTextListener)
            binding.edtOptionTitle.setText(option.title)
            if (optionEditTextListener == null) {
                optionEditTextListener = OptionEditTextListener(itemListener)
            }
            binding.edtOptionTitle.addTextChangedListener(optionEditTextListener)
            binding.imgDeleteOption.setOnClickListener { itemListener.onOptionRemove(option.id) }
            binding.imgNewOption.setOnClickListener {
                itemListener.onOptionAddNewCheck(binding.edtOptionTitle.text.toString())
            }
        }
    }

    private inner class OptionEditTextListener(internal var itemListener: VoteDetailContentActivity.OptionItemListener) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            option?.let {
                itemListener.onOptionTextChange(it.id, s.toString())
            }
        }
        override fun afterTextChanged(s: Editable) {}
    }
}
