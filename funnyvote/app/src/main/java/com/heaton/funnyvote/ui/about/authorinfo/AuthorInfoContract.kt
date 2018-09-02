package com.heaton.funnyvote.ui.about.authorinfo

import com.heaton.funnyvote.BasePresenter
import com.heaton.funnyvote.BaseView

interface AuthorInfoContract {
    interface Presenter : BasePresenter

    interface View : BaseView<Presenter>
}
