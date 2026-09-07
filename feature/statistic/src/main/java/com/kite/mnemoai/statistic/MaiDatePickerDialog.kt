package com.kite.mnemoai.statistic

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.DatePicker

import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult

class CustomDatePickerDialog: DialogFragment(), DatePickerDialog.OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dialog = DatePickerDialog(
            requireContext(),
            R.style.App_DatePickerDialog,
            this, year, month, day
        )
        return dialog
    }

    override fun onDateSet(
        p0: DatePicker?,
        p1: Int,
        p2: Int,
        p3: Int
    ) {
        setFragmentResult("datePicker",
            bundleOf("year" to p1, "month" to p2, "day" to p3))
    }
}