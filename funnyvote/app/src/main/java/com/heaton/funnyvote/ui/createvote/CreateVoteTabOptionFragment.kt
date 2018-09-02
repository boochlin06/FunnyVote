package com.heaton.funnyvote.ui.createvote

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.Option
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_create_vote_tab_options.*

/**
 * Created by heaton on 2016/9/1.
 */

class CreateVoteTabOptionFragment : Fragment(), CreateVoteContract.OptionFragmentView {
    private lateinit var rootView: View
    private lateinit var optionItemAdapter: OptionCreateItemAdapter
    private var itemListener: OptionItemListener? = null
    private lateinit var presenter: CreateVoteContract.Presenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_create_vote_tab_options, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemListener = object : OptionItemListener {
            override fun onOptionTextChange(optionId: Long, newOptionText: String) {
                presenter.reviseOption(optionId, newOptionText)
            }

            override fun onOptionAddNew() {
                presenter.addNewOption()
            }

            override fun onOptionRemove(optionId: Long) {
                presenter.removeOption(optionId)
            }
        }

        val pickImageListener = View.OnClickListener { CropImage.startPickImageActivity(requireActivity()) }
        imgMain.setOnClickListener(pickImageListener)
        imgPick.setOnClickListener(pickImageListener)
        presenter.setOptionFragmentView(this)
    }

    override fun setUpOptionAdapter(optionList: List<Option>) {
        optionItemAdapter = OptionCreateItemAdapter(optionList, itemListener!!)
        ryOptions.adapter = optionItemAdapter
    }

    override fun setVoteImage(imageUri: Uri) {
        imgMain.visibility = View.VISIBLE
        imgPick.visibility = View.GONE
        Glide.with(this)
                .load(imageUri)
                .into(imgMain)
    }

    override fun setPresenter(presenter: CreateVoteContract.Presenter) {
        this.presenter = presenter
    }

    override fun refreshOptions() {
        optionItemAdapter.notifyDataSetChanged()
    }

    interface OptionItemListener {
        fun onOptionTextChange(optionId: Long, newOptionText: String)

        fun onOptionAddNew()

        fun onOptionRemove(optionId: Long)
    }

    companion object {

        fun newTabFragment(): CreateVoteTabOptionFragment {
            return CreateVoteTabOptionFragment()
        }
    }
}
