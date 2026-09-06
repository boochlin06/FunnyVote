package com.heaton.funnyvote.ui.introduction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder
import com.github.paolorotolo.appintro.ISlideSelectionListener
import com.heaton.funnyvote.R
import com.heaton.funnyvote.databinding.FragmentIntroductionBinding

open class IntroductionFragment : Fragment(), ISlideSelectionListener, ISlideBackgroundColorHolder {
    private var _binding: FragmentIntroductionBinding? = null
    private val binding get() = _binding!!

    private var drawable: Int = 0
    private var bgColor: Int = 0
    private var title: String? = null
    private var description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        arguments?.let {
            if (it.size() != 0) {
                drawable = it.getInt(ARG_DRAWABLE)
                title = it.getString(ARG_TITLE)
                description = it.getString(ARG_DESC)
                bgColor = it.getInt(ARG_BG_COLOR)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.let {
            drawable = it.getInt(ARG_DRAWABLE)
            title = it.getString(ARG_TITLE)
            description = it.getString(ARG_DESC)
            bgColor = it.getInt(ARG_BG_COLOR)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIntroductionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtTitle.text = title
        binding.txtDescription.text = description
        binding.imgFragment.setImageResource(drawable)
        binding.main.setBackgroundColor(bgColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_DRAWABLE, drawable)
        outState.putString(ARG_TITLE, title)
        outState.putString(ARG_DESC, description)
        outState.putInt(ARG_BG_COLOR, bgColor)
        super.onSaveInstanceState(outState)
    }

    override fun getDefaultBackgroundColor(): Int = bgColor

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        _binding?.main?.setBackgroundColor(backgroundColor)
    }

    override fun onSlideSelected() {}

    override fun onSlideDeselected() {}

    companion object {
        const val ARG_TITLE = "title"
        const val ARG_DESC = "desc"
        const val ARG_DRAWABLE = "drawable"
        const val ARG_BG_COLOR = "bg_color"

        fun newInstance(
            title: CharSequence,
            description: CharSequence,
            @DrawableRes imageDrawable: Int,
            @ColorInt bgColor: Int
        ): IntroductionFragment {
            val slide = IntroductionFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title.toString())
            args.putString(ARG_DESC, description.toString())
            args.putInt(ARG_DRAWABLE, imageDrawable)
            args.putInt(ARG_BG_COLOR, bgColor)
            slide.arguments = args
            return slide
        }
    }
}
