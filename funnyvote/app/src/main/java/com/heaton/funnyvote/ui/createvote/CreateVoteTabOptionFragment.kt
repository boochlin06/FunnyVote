package com.heaton.funnyvote.ui.createvote

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.databinding.FragmentCreateVoteTabOptionsBinding
import com.theartofdev.edmodo.cropper.CropImage

class CreateVoteTabOptionFragment : Fragment(), CreateVoteContract.OptionFragmentView {
    private var _binding: FragmentCreateVoteTabOptionsBinding? = null
    private val binding get() = _binding!!

    private var optionItemAdapter: OptionCreateItemAdapter? = null
    private var itemListener: OptionItemListener? = null
    private lateinit var presenter: CreateVoteContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateVoteTabOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        binding.imgMain.setOnClickListener(pickImageListener)
        binding.imgPick.setOnClickListener(pickImageListener)
        presenter.setOptionFragmentView(this)
    }

    override fun setUpOptionAdapter(optionList: List<Option>) {
        _binding?.let { b ->
            optionItemAdapter = OptionCreateItemAdapter(optionList, itemListener!!)
            b.ryOptions.adapter = optionItemAdapter
        }
    }

    override fun setVoteImage(imageUri: Uri) {
        _binding?.let { b ->
            b.imgMain.visibility = View.VISIBLE
            b.imgPick.visibility = View.GONE
            Glide.with(this)
                .load(imageUri)
                .into(b.imgMain)
        }
    }

    override fun setPresenter(presenter: CreateVoteContract.Presenter) {
        this.presenter = presenter
    }

    override fun refreshOptions() {
        optionItemAdapter?.notifyDataSetChanged()
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
