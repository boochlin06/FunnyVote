package com.heaton.funnyvote.ui.about.licence

import com.heaton.funnyvote.BasePresenter
import com.heaton.funnyvote.BaseView

interface LicenceContract {
    interface Presenter : BasePresenter

    interface View : BaseView<Presenter>
}
