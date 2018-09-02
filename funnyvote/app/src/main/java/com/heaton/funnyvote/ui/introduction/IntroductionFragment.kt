package com.heaton.funnyvote.ui.introduction

import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder
import com.github.paolorotolo.appintro.ISlideSelectionListener
import com.heaton.funnyvote.R

import kotlinx.android.synthetic.main.fragment_introduction.*

/**
 * Created by heaton on 2017/2/25.
 */

open class IntroductionFragment : Fragment(), ISlideSelectionListener, ISlideBackgroundColorHolder {

    private var drawable: Int = 0
    private var bgColor: Int = 0
    private var title: String? = null
    private var description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        if (arguments != null && arguments!!.size() != 0) {
            drawable = arguments!!.getInt(ARG_DRAWABLE)
            title = arguments!!.getString(ARG_TITLE)
            description = arguments!!.getString(ARG_DESC)
            bgColor = arguments!!.getInt(ARG_BG_COLOR)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            drawable = savedInstanceState.getInt(ARG_DRAWABLE)
            title = savedInstanceState.getString(ARG_TITLE)
            description = savedInstanceState.getString(ARG_DESC)
            bgColor = savedInstanceState.getInt(ARG_BG_COLOR)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_introduction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtTitle.text = title
        txtDescription.text = description
        imgFragment.setImageResource(drawable)
        main.setBackgroundColor(bgColor)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_DRAWABLE, drawable)
        outState.putString(ARG_TITLE, title)
        outState.putString(ARG_DESC, description)
        outState.putInt(ARG_BG_COLOR, bgColor)
        super.onSaveInstanceState(outState)
    }

    override fun getDefaultBackgroundColor(): Int {
        return bgColor
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        main.setBackgroundColor(backgroundColor)
    }

    override fun onSlideSelected() {

    }

    override fun onSlideDeselected() {

    }

    companion object {
        const val ARG_TITLE = "title"
        protected const val ARG_DESC = "desc"
        protected const val ARG_DRAWABLE = "drawable"
        protected const val ARG_BG_COLOR = "bg_color"

        fun newInstance(title: CharSequence, description: CharSequence,
                        @DrawableRes imageDrawable: Int, @ColorInt bgColor: Int): IntroductionFragment {
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
