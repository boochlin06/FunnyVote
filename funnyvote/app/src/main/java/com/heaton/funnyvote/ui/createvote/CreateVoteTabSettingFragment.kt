package com.heaton.funnyvote.ui.createvote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.databinding.FragmentCreateVoteTabSettingsBinding
import com.heaton.funnyvote.utils.Util
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import java.util.Calendar

class CreateVoteTabSettingFragment : Fragment(), CreateVoteContract.SettingFragmentView {

    private var _binding: FragmentCreateVoteTabSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var presenter: CreateVoteContract.Presenter
    private var securityType = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateVoteTabSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtEndTimeDetail.setOnClickListener { onTimeDetailClick() }
        binding.txtEndTime.setOnClickListener { onTimeDetailClick() }
        binding.imgEndTime.setOnClickListener { onTimeDetailClick() }

        binding.txtSecurity.setOnClickListener { onSecurityDetailClick() }
        binding.txtSecurityDetail.setOnClickListener { onSecurityDetailClick() }
        binding.imgSecurity.setOnClickListener { onSecurityDetailClick() }

        presenter.setSettingFragmentView(this)
    }

    override fun setUpVoteSettings(voteSettings: VoteData) {
        _binding?.let { b ->
            b.edtMaxOption.setText(voteSettings.maxOption.toString())
            b.edtMinOption.setText(voteSettings.minOption.toString())
            b.swtUserAdd.isChecked = voteSettings.isUserCanAddOption
            b.swtPreResult.isChecked = voteSettings.isUserCanAddOption
            b.swtNeedPwd.isChecked = voteSettings.isNeedPassword
            b.swtNeedPwd.setOnCheckedChangeListener { _, isChecked -> updateSwtNeedPwd(isChecked) }
            updateSwtNeedPwd(voteSettings.isNeedPassword)
            b.txtSecurityDetail.text = getString(R.string.create_vote_tab_settings_public)
            voteSettings.endTime = System.currentTimeMillis() + DEFAULT_END_TIME * 86400 * 1000
            b.txtEndTimeDetail.text = Util.getDate(voteSettings.endTime, "yyyy/MM/dd")
            b.swtAnonymous.isChecked = false
        }
    }

    override fun updateSwtNeedPwd(isChecked: Boolean) {
        _binding?.let { b ->
            if (isChecked) {
                b.edtPwd.visibility = View.VISIBLE
                b.txtPwd.visibility = View.VISIBLE
            } else {
                b.edtPwd.visibility = View.INVISIBLE
                b.txtPwd.visibility = View.INVISIBLE
            }
        }
    }

    override fun updateUserSetting(user: User) {
        _binding?.edtAuthorName?.setText(user.userName)
    }

    override fun getFinalVoteSettings(oldVoteData: VoteData): VoteData {
        val b = _binding ?: return oldVoteData
        oldVoteData.category = ""
        oldVoteData.maxOption = if (b.edtMaxOption.text.isNullOrEmpty()) 0 else b.edtMaxOption.text.toString().toIntOrNull() ?: 0
        oldVoteData.minOption = if (b.edtMinOption.text.isNullOrEmpty()) 0 else b.edtMinOption.text.toString().toIntOrNull() ?: 0
        oldVoteData.isUserCanAddOption = b.swtUserAdd.isChecked
        oldVoteData.isCanPreviewResult = b.swtPreResult.isChecked
        oldVoteData.isNeedPassword = b.swtNeedPwd.isChecked
        if (b.swtNeedPwd.isChecked) {
            oldVoteData.password = b.edtPwd.text.toString()
        }
        return oldVoteData
    }

    private fun onTimeDetailClick() {
        val now = Calendar.getInstance()
        now.add(Calendar.DAY_OF_MONTH, DEFAULT_END_TIME.toInt())
        val timeSetting = DatePickerDialog.newInstance(
            { _, year, monthOfYear, dayOfMonth ->
                val endTime = Calendar.getInstance()
                endTime.set(year, monthOfYear, dayOfMonth)
                presenter.updateVoteEndTime(endTime.timeInMillis)
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
        timeSetting.show(requireActivity().supportFragmentManager, "End time")
    }

    private fun onSecurityDetailClick() {
        val builder = AlertDialog.Builder(requireContext())
        val allType = arrayOf(getString(R.string.create_vote_tab_settings_public_hint), getString(R.string.create_vote_tab_settings_private_hint))
        builder.setSingleChoiceItems(allType, securityType) { _, which -> securityType = which }
        builder.setPositiveButton(getString(R.string.vote_detail_dialog_sort_select)) { dialog, _ ->
            if (securityType == 0) {
                presenter.updateVoteSecurity(VoteData.SECURITY_PUBLIC)
                _binding?.txtSecurityDetail?.setText(R.string.create_vote_tab_settings_public)
            } else {
                presenter.updateVoteSecurity(VoteData.SECURITY_PRIVATE)
                _binding?.txtSecurityDetail?.setText(R.string.create_vote_tab_settings_private)
            }
            dialog.dismiss()
        }
        builder.setTitle(getString(R.string.vote_detail_dialog_security))
        builder.show()
    }

    override fun setPresenter(presenter: CreateVoteContract.Presenter) {
        this.presenter = presenter
    }

    companion object {
        private const val DEFAULT_END_TIME: Long = 30

        fun newTabFragment(): CreateVoteTabSettingFragment {
            return CreateVoteTabSettingFragment()
        }
    }
}
