package com.kite.mnemoai.statistic

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kite.mnemoai.model.statistic.DateInterval
import com.kite.mnemoai.statistic.databinding.DialogFragmentDatePickerBinding
import com.kite.mnemoai.ui.widget.wheelwidget.ListWheelAdapter
import java.time.LocalDate
import java.util.Calendar

class MaiDatePickerDialog: DialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        activity?.let {
            val date = LocalDate.now()
            val maxYear = date.year + 10

            val startDate = LocalDate.parse(arguments?.getString("start_date")?: date.toString())
            val endDate = LocalDate.parse(arguments?.getString("end_date")?: date.toString())

            var selectedStartYear = startDate.year
            var selectedStartMonth = startDate.month.value
            var selectedStartDay = startDate.dayOfMonth

            var selectedEndYear = endDate.year
            var selectedEndMonth = endDate.month.value
            var selectedEndDay = endDate.dayOfMonth

            val years = (1970..maxYear).toList()
            val months = (1..12).toList()
            var startDays = (1..getMaxDaysInMonth(selectedStartYear, selectedStartMonth)).toList()
            var endDays = (1..getMaxDaysInMonth(selectedEndYear, selectedEndMonth)).toList()


            val binding = DialogFragmentDatePickerBinding.inflate(layoutInflater, null, false)
            binding.startYearView.adapter = ListWheelAdapter(years)
            binding.startYearView.setCurrentItem(years.indexOf(selectedStartYear))
            binding.startYearView.onSelectedIndexChanged = {index ->
                selectedStartYear = years[index]
                startDays = (1..getMaxDaysInMonth(selectedStartYear, selectedStartMonth)).toList()
                binding.startDayView.adapter = ListWheelAdapter(startDays) { it.toString().padStart(2, '0') }
            }

            binding.startMonthView.adapter = ListWheelAdapter(months) { it.toString().padStart(2, '0') }
            binding.startMonthView.setCurrentItem(months.indexOf(selectedStartMonth))
            binding.startMonthView.onSelectedIndexChanged = {index ->
                selectedStartMonth = months[index]
                startDays = (1..getMaxDaysInMonth(selectedStartYear, selectedStartMonth)).toList()
                binding.startDayView.adapter = ListWheelAdapter(startDays) { it.toString().padStart(2, '0') }
            }

            binding.startDayView.adapter = ListWheelAdapter(startDays) { it.toString().padStart(2, '0') }
            binding.startDayView.setCurrentItem(startDays.indexOf(selectedStartDay))

            binding.endYearView.adapter = ListWheelAdapter(years)
            binding.endYearView.setCurrentItem(years.indexOf(selectedEndYear))
            binding.endYearView.onSelectedIndexChanged = { index ->
                selectedEndYear = years[index]
                endDays = (1..getMaxDaysInMonth(selectedEndYear, selectedEndMonth)).toList()
                binding.endDayView.adapter = ListWheelAdapter(endDays) { it.toString().padStart(2, '0') }
            }

            binding.endMonthView.adapter = ListWheelAdapter(months) { it.toString().padStart(2, '0') }
            binding.endMonthView.setCurrentItem(months.indexOf(selectedEndMonth))
            binding.endMonthView.onSelectedIndexChanged = { index ->
                selectedEndMonth = months[index]
                endDays = (1..getMaxDaysInMonth(selectedEndYear, selectedEndMonth)).toList()
                binding.endDayView.adapter = ListWheelAdapter(endDays) { it.toString().padStart(2, '0') }
            }

            binding.endDayView.adapter = ListWheelAdapter(endDays) { it.toString().padStart(2, '0') }
            binding.endDayView.setCurrentItem(endDays.indexOf(selectedEndDay))


            binding.cancelBtn.setOnClickListener { dismiss() }
            binding.confirmBtn.setOnClickListener {
                val selectedStartDay = startDays[binding.startDayView.selectedIndex]
                val selectedEndDay = endDays[binding.endDayView.selectedIndex]
                setFragmentResult(REQUEST_KEY, bundleOf(
                    "start_year" to selectedStartYear,
                    "start_month" to selectedStartMonth,
                    "start_day" to selectedStartDay,
                    "end_year" to selectedEndYear,
                    "end_month" to selectedEndMonth,
                    "end_day" to selectedEndDay
                ))
                dismiss()
            }
            val builder = MaterialAlertDialogBuilder(it, com.kite.mnemoai.ui.R.style.CustomAlertDialog)
            builder.setView(binding.root)
            builder.create()
        }?:throw IllegalStateException("页面不存在")

    companion object {
        const val REQUEST_KEY = "mai_date_picker_result"

        fun newInstance(dateInterval: DateInterval): MaiDatePickerDialog{
            val bundle = Bundle().apply {
                putString("start_date", dateInterval.startDate.toString())
                putString("end_date", dateInterval.endDate.toString())
            }
            val dialog = MaiDatePickerDialog()
            dialog.arguments = bundle
            return dialog
        }
    }

    private fun getMaxDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        // month 是 1 起的（1..12），而 Calendar.MONTH 是 0 起的（0=1月）
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun dateInvalidCheck(startDate: LocalDate, endDate: LocalDate): Boolean{
        return startDate <= endDate
    }
}
