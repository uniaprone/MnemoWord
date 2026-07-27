package com.kite.mnemoai.ui.dialog.vocabularyselect

import android.app.Dialog
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kite.mnemoai.R
import com.kite.mnemoai.adapter.VocabularySelectAdapter
import com.kite.mnemoai.databinding.DialogFragmentVocabularySelectBinding

class VocabularySelectDialogFragment: DialogFragment() {
    private lateinit var vocabularySelectInfos: MutableList<VocabularySelectInfo?>
    companion object{
        const val VOCABULARY_BOOK_SELECT = "VOCABULARYBOOKSELECT"

        fun newInstance(vocabularySelectInfos: MutableList<VocabularySelectInfo?>): VocabularySelectDialogFragment{
            val bundle = Bundle().apply {
                putParcelableArrayList("vocabulary_select_infos", ArrayList(vocabularySelectInfos))
            }
            val dialogFragment = VocabularySelectDialogFragment()
            dialogFragment.arguments = bundle
            return dialogFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vocabularySelectInfos =
            arguments?.getParcelableArrayList<VocabularySelectInfo>(
                "vocabulary_select_infos",
            ) ?: mutableListOf()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val binding = DialogFragmentVocabularySelectBinding.inflate(layoutInflater, null, false)
            val vocabularySelectAdapter = VocabularySelectAdapter(vocabularySelectInfos)
            binding.vocabularyRV.apply {
                setLayoutManager(LinearLayoutManager(context))
                setAdapter(vocabularySelectAdapter)
            }
            binding.cancelBtn.setOnClickListener {
                dismiss()
            }
            binding.confirmBtn.setOnClickListener {
                val ids = vocabularySelectInfos
                    .filter { vocabularySelectInfo -> vocabularySelectInfo?.isSelect == true }
                    .mapNotNull { vocabularySelectInfo -> vocabularySelectInfo?.id }
                    .toLongArray()

                val bundle = Bundle().apply {
                    putLongArray("ids", ids)
                }
                parentFragmentManager.setFragmentResult(VOCABULARY_BOOK_SELECT, bundle)
                dismiss()
            }
            val builder = MaterialAlertDialogBuilder(it, R.style.CustomAlertDialog)
            builder.setView(binding.root)
            builder.create()
        }?:throw IllegalStateException("页面不存在")
    }

    data class VocabularySelectInfo(var id: Long, @JvmField var name: String?, @JvmField var description: String?, ):
        Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )
        @JvmField
        var isSelect: Boolean = false
        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(p0: Parcel, p1: Int) {
            p0.apply {
                writeLong(id)
                writeString(name)
                writeString(description)
                writeInt(if (isSelect) 1 else 0)
            }
        }

        companion object CREATOR:  Parcelable.Creator<VocabularySelectInfo>{
            override fun createFromParcel(p0: Parcel?): VocabularySelectInfo? {
                return p0?.let { VocabularySelectInfo(p0) }
            }

            override fun newArray(p0: Int): Array<out VocabularySelectInfo?>? {
                return arrayOfNulls(p0)
            }

        }
    }
}
