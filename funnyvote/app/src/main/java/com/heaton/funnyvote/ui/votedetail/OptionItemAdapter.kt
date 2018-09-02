package com.heaton.funnyvote.ui.votedetail

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.VoteData
import java.util.*

/**
 * Created by heaton on 2016/8/22.
 */

class OptionItemAdapter(
        optionType: Int
        , private var optionList: List<Option>
        , private val data: VoteData
        , private val itemListener: VoteDetailContentActivity.OptionItemListener
) : Adapter<RecyclerView.ViewHolder>() {
    private var searchList: List<Option>

    var choiceList: List<Long>
    val choiceCodeList: List<String>
    private var expandOptionList: List<String>
    private var pollCount = 0
    private var optionChoiceType = OPTION_UNPOLL
    var isSearchMode = false

    private val currentList: List<Option>
        get() = if (isSearchMode) searchList else optionList

    fun setOptionList(optionList: List<Option>) {
        this.optionList = optionList
    }

    fun setExpandOptionList(expandOptionList: List<String>) {
        this.expandOptionList = expandOptionList
    }

    init {
        this.optionChoiceType = optionType
        this.pollCount = data.pollCount
        this.choiceList = ArrayList()
        this.choiceCodeList = ArrayList()
        this.expandOptionList = ArrayList()
        this.searchList = ArrayList()
    }

    fun setSearchList(searchList: List<Option>) {
        this.searchList = searchList
    }

    fun setOptionType(optionType: Int) {
        this.optionChoiceType = optionType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (optionChoiceType == OPTION_UNPOLL) {
            if (viewType == OPTION_UNPOLL_VIEW_TYPE_ADD_NEW || viewType == OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT) {
                VHUnPollCreateOptionItem(LayoutInflater.from(parent.context)
                        .inflate(R.layout.card_view_item_unpoll_create_new_option, parent, false), itemListener)
            } else {
                VHUnPollOptionItem(LayoutInflater.from(parent.context)
                        .inflate(R.layout.card_view_item_unpoll_options, parent, false)
                        , data.isMultiChoice, itemListener)
            }
        } else if (optionChoiceType == OPTION_SHOW_RESULT) {
            VHResultOptionItem(LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_view_item_result_option, parent, false), pollCount, itemListener)
        } else {
            VHResultOptionItem(LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_view_item_result_option, parent, false), pollCount, itemListener)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var isChoice: Boolean
        var isExpand: Boolean
        if (holder is VHUnPollOptionItem) {
            isChoice = choiceList.contains(currentList[position].id)
            isExpand = expandOptionList.contains(currentList[position].code)
            holder.setLayout(isChoice, isExpand, currentList[position])
        } else if (holder is VHResultOptionItem) {
            isChoice = choiceList.contains(currentList[position].id)
            isExpand = expandOptionList.contains(currentList[position].code)
            holder.setLayout(isChoice, isExpand, currentList[position].count
                    == data.optionTopCount && data.pollCount != 0, currentList[position])
        } else if (holder is VHUnPollCreateOptionItem) {
            if (holder.getItemViewType() == OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT) {
                choiceList.contains(currentList[position].id)
                holder.setLayout(currentList[position])
            } else {
                holder.setLayout(currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return if (OPTION_UNPOLL == optionChoiceType && data.isUserCanAddOption && !isSearchMode) {
            currentList.size + 1
        } else {
            currentList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        val type = -1

        if (optionChoiceType == OPTION_UNPOLL) {
            if (position == currentList.size) {
                return OPTION_UNPOLL_VIEW_TYPE_ADD_NEW
            } else if (currentList[position].id < 0) {
                return OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT
            }
        }
        return type
    }

    companion object {

        const val OPTION_UNPOLL_VIEW_TYPE_ADD_NEW = 20
        const val OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT = 21
        const val OPTION_UNPOLL = 2
        const val OPTION_SHOW_RESULT = 3
    }

}
