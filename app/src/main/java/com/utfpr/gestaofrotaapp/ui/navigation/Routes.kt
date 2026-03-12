package com.utfpr.gestaofrotaapp.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val LIST = "list"
    const val DETAIL = "detail/{carId}"
    const val FORM = "form"
    const val FORM_EDIT = "form/{carId}"

    fun detail(carId: String) = "detail/$carId"
    fun formEdit(carId: String) = "form/$carId"
}
