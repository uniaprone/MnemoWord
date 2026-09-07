package com.kite.mnemoai.mine

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kite.mnemoai.mine.databinding.DialogFragmentReminderTimeSettingBinding
import com.kite.mnemoai.ui.widget.wheelwidget.ListWheelAdapter
import java.time.LocalTime

class ReminderTimerDialogFragment: DialogFragment() {
    private var _binding: DialogFragmentReminderTimeSettingBinding? = null
    private val binding get() = _binding!!
    private val hours = (0..23).toList()
    private val minutes = (0..59).toList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        activity?.let {
            val selectedHour = arguments?.getInt("hour", 0)
            val selectedMinute = arguments?.getInt("minute", 0)

            _binding = DialogFragmentReminderTimeSettingBinding.inflate(
                layoutInflater, null, false)

            binding.hour.adapter = ListWheelAdapter(hours){it.toString().padStart(2, '0')}
            binding.hour.setCurrentItem(hours.indexOf(selectedHour))
            binding.minute.adapter = ListWheelAdapter(minutes){it.toString().padStart(2, '0')}
            binding.minute.setCurrentItem(minutes.indexOf(selectedMinute))

            binding.cancelBtn.setOnClickListener { dismiss() }
            binding.confirmBtn.setOnClickListener {
                val hour = hours[binding.hour.selectedIndex]
                val minute = minutes[binding.minute.selectedIndex]
                val time = LocalTime.of(hour, minute).toString()
                setFragmentResult(REQUEST_KEY,
                    bundleOf("time" to time)
                )
                dismiss()
            }

            val builder = MaterialAlertDialogBuilder(it, com.kite.mnemoai.ui.R.style.CustomAlertDialog)
            builder.setView(binding.root)
            return builder.create()
        }?: throw IllegalArgumentException("页面不存在")

    companion object{
        const val REQUEST_KEY = "SET_REMINDER_TIME"

        fun newInstance(reminderTime: LocalTime?): ReminderTimerDialogFragment{
            val time = reminderTime ?: LocalTime.now()
            val dialog = ReminderTimerDialogFragment()
            dialog.arguments = bundleOf("hour" to time.hour, "minute" to time.minute)
            return dialog
        }
    }
}