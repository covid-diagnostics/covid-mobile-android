package com.example.coronadiagnosticapp.utils

import androidx.annotation.IdRes
import com.afollestad.vvalidator.checkAttached
import com.afollestad.vvalidator.field.FieldBuilder
import com.afollestad.vvalidator.form.Form
import com.afollestad.vvalidator.form.GenericFormField
import com.afollestad.vvalidator.getViewOrThrow
import com.example.coronadiagnosticapp.ui.views.YesNoQuestionView

//fun Form.yesNoQuestion(
//    @IdRes id:Int,
//    name: String? = null,
//    builder: FieldBuilder<YesNoQuestionView>
//) = YesNoQuestionView(
//    container.getViewOrThrow(id),
//    name,builder)
//
//fun Form.yesNoQuestion(
//    view: YesNoQuestionView,
//    name: String? = null,
//    builder: FieldBuilder<YesNoQuestionView>
//): GenericFormField {
//    val newField = YesNoQuestionView(
//        container = container.checkAttached(),
//        view = view,
//        name = name
//    )
//    builder(newField)
//    return appendField(newField)
//}
//TODO implement form capabilities on YesNoQuestionView