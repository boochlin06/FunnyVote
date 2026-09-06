package com.heaton.funnyvote.ui.createvote

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.databinding.CardViewCreateVoteOptionBinding

class VHCreateOptionItem(
    val binding: CardViewCreateVoteOptionBinding,
    private val itemListener: CreateVoteTabOptionFragment.OptionItemListener?
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        itemView: View,
        itemListener: CreateVoteTabOptionFragment.OptionItemListener?
    ) : this(CardViewCreateVoteOptionBinding.bind(itemView), itemListener)

    private var optionEditTextListener: OptionEditTextListener? = null
    private lateinit var option: Option

    fun setLayout(viewType: Int, option: Option) {
        this.option = option
        if (viewType == OptionCreateItemAdapter.VIEW_TYPE_ADD_OPTION) {
            binding.relNormal.visibility = View.INVISIBLE
            binding.relAdd.visibility = View.VISIBLE
            binding.imgDeleteOption.visibility = View.GONE
            binding.edtOptionTitle.visibility = View.GONE
            binding.edtOptionTitle.removeTextChangedListener(optionEditTextListener)
        } else if (viewType == OptionCreateItemAdapter.VIEW_TYPE_NORMAL_OPTION) {
            binding.relNormal.visibility = View.VISIBLE
            binding.relAdd.visibility = View.INVISIBLE
            binding.imgDeleteOption.visibility = View.VISIBLE
            binding.txtOptionNumber.text = (adapterPosition + 1).toString()
            binding.edtOptionTitle.visibility = View.VISIBLE
            binding.edtOptionTitle.removeTextChangedListener(optionEditTextListener)
            binding.edtOptionTitle.setText(option.title)
            if (optionEditTextListener == null) {
                optionEditTextListener = OptionEditTextListener(itemListener)
            }
            binding.edtOptionTitle.addTextChangedListener(optionEditTextListener)
        }
        binding.relAdd.setOnClickListener { itemListener?.onOptionAddNew() }
        binding.imgDeleteOption.setOnClickListener { itemListener?.onOptionRemove(option.id) }
    }

    private inner class OptionEditTextListener(
        internal var itemListener: CreateVoteTabOptionFragment.OptionItemListener?
    ) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            itemListener?.onOptionTextChange(option.id, s.toString())
        }
        override fun afterTextChanged(s: Editable) {}
    }
}
