package com.heaton.funnyvote.ui.createvote

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.utils.Util
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.fragment_create_vote_tab_settings.*
import java.util.*

/**
 * Created by heaton on 2016/9/1.
 */

class CreateVoteTabSettingFragment : Fragment(), CreateVoteContract.SettingFragmentView {

    private lateinit var presenter: CreateVoteContract.Presenter
    private var securityType = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_vote_tab_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtEndTimeDetail.setOnClickListener { onTimeDetailClick() }
        txtEndTime.setOnClickListener { onTimeDetailClick() }
        imgEndTime.setOnClickListener { onTimeDetailClick() }

        txtSecurity.setOnClickListener { onSecurityDetailClick() }
        txtSecurityDetail.setOnClickListener { onSecurityDetailClick() }
        imgSecurity.setOnClickListener { onSecurityDetailClick() }

        presenter.setSettingFragmentView(this)
    }

    override fun setUpVoteSettings(voteSettings: VoteData) {
        edtMaxOption.setText(voteSettings.maxOption.toString())
        edtMinOption.setText(voteSettings.minOption.toString())
        swtUserAdd.isChecked = voteSettings.isUserCanAddOption
        swtPreResult.isChecked = voteSettings.isUserCanAddOption
        swtNeedPwd.isChecked = voteSettings.isNeedPassword
        swtNeedPwd.setOnCheckedChangeListener { _, isChecked -> updateSwtNeedPwd(isChecked) }
        updateSwtNeedPwd(voteSettings.isNeedPassword)
        txtSecurityDetail.text = getString(R.string.create_vote_tab_settings_public)
        voteSettings.endTime = System.currentTimeMillis() + DEFAULT_END_TIME * 86400 * 1000
        txtEndTimeDetail.text = Util.getDate(voteSettings.endTime, "yyyy/MM/dd")

        swtAnonymous.isChecked = false
    }

    override fun updateSwtNeedPwd(isChecked: Boolean) {
        if (isChecked) {
            edtPwd.visibility = View.VISIBLE
            txtPwd.visibility = View.VISIBLE
        } else {
            edtPwd.visibility = View.INVISIBLE
            txtPwd.visibility = View.INVISIBLE
        }
    }

    override fun updateUserSetting(user: User) {
        edtAuthorName.setText(user.userName)
    }

    override fun getFinalVoteSettings(oldVoteData: VoteData): VoteData {

        oldVoteData.category = ""
        oldVoteData.maxOption = if (edtMaxOption.text.isEmpty())
            0
        else
            Integer.parseInt(edtMaxOption.text.toString())
        oldVoteData.minOption = if (edtMinOption.text.isEmpty())
            0
        else
            Integer.parseInt(edtMinOption.text.toString())
        oldVoteData.isUserCanAddOption = swtUserAdd.isChecked
        oldVoteData.isCanPreviewResult = swtPreResult.isChecked
        oldVoteData.isNeedPassword = swtNeedPwd.isChecked
        if (swtNeedPwd.isChecked) {
            oldVoteData.password = edtPwd.text.toString()
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
                now.get(Calendar.DAY_OF_MONTH))
        timeSetting.show(requireActivity().fragmentManager, "End time")
    }


    private fun onSecurityDetailClick() {
        val builder = AlertDialog.Builder(requireContext())
        val allType = arrayOf(getString(R.string.create_vote_tab_settings_public_hint), getString(R.string.create_vote_tab_settings_private_hint))
        builder.setSingleChoiceItems(allType, securityType) { _, which -> securityType = which }
        builder.setPositiveButton(getString(R.string.vote_detail_dialog_sort_select)) { dialog, _ ->
            if (securityType == 0) {
                presenter.updateVoteSecurity(VoteData.SECURITY_PUBLIC)
                txtSecurityDetail.setText(R.string.create_vote_tab_settings_public)
            } else {
                presenter.updateVoteSecurity(VoteData.SECURITY_PRIVATE)
                txtSecurityDetail.setText(R.string.create_vote_tab_settings_private)
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
