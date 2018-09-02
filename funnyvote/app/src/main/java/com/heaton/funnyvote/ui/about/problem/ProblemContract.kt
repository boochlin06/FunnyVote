package com.heaton.funnyvote.ui.about.problem

import com.heaton.funnyvote.BasePresenter
import com.heaton.funnyvote.BaseView

interface ProblemContract {
    interface Presenter : BasePresenter

    interface View : BaseView<Presenter>
}
