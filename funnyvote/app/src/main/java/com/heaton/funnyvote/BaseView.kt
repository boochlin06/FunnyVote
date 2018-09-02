package com.heaton.funnyvote

interface BaseView<T : BasePresenter> {
    fun setPresenter(presenter: T)
}
