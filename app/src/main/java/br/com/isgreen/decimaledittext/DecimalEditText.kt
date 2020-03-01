package br.com.isgreen.decimaledittext

import android.content.Context
import android.text.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doAfterTextChanged
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * Created by Éverdes Soares on 02/29/2020.
 */

class DecimalEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {

    companion object {
        const val PREFIX_SPACE_SIZE = 1
        const val SUFFIX_SPACE_SIZE = 1
        const val INT_MAX_VALUE = 15
    }

    private var mValue = 0.0
    private var mPrefix = ""
    private var mSuffix = ""
    private var mIsUpdating = false
    private var mMaxValue = Float.MAX_VALUE
    private var mMaxIntegerDigit = INT_MAX_VALUE
    private var mMaxDecimalDigit = INT_MAX_VALUE

    var value: Double
        get() = mValue
        set(value) {
            mValue = value
            setText(value.toString())
        }

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        if (!isInEditMode) {
            val tpArray = context.obtainStyledAttributes(
                attrs, R.styleable.DecimalEditText
            )

            val prefix = tpArray.getString(
                R.styleable.DecimalEditText_prefix
            )
            prefix?.let {
                mPrefix = it
            }

            val suffix = tpArray.getString(
                R.styleable.DecimalEditText_suffix
            )
            suffix?.let {
                mSuffix = it
            }

            mMaxValue = tpArray.getFloat(
                R.styleable.DecimalEditText_maxValue, Float.MAX_VALUE
            )

            mMaxIntegerDigit = tpArray.getInt(
                R.styleable.DecimalEditText_maxIntegerDigit, INT_MAX_VALUE
            )

            mMaxDecimalDigit = tpArray.getInt(
                R.styleable.DecimalEditText_maxDecimalDigit, INT_MAX_VALUE
            )

            this.inputType = InputType.TYPE_CLASS_NUMBER
            this.doAfterTextChanged { editable ->
                onAfterTextChanged(editable)
            }

            var maxLength = mMaxIntegerDigit + mMaxDecimalDigit + 1

            if (mPrefix.isNotEmpty()) {
                maxLength += mPrefix.length + PREFIX_SPACE_SIZE
            }

            if (mSuffix.isNotEmpty()) {
                maxLength += mSuffix.length + SUFFIX_SPACE_SIZE
            }

            filters = arrayOf(
                InputFilter.LengthFilter(maxLength)
            )

            // If has no hint set zero value as default
            if (hint.isNullOrEmpty()) {
                value = 0.0
            }

            tpArray.recycle()
        }
    }

    private fun onAfterTextChanged(editable: Editable?) {
        if (mIsUpdating) {
            mIsUpdating = false
            return
        }

        val oldValue = mValue
        mValue = convertTextToValue(editable?.toString())

        // Cleaning text to show hint
        if (mValue == 0.0 && !hint.isNullOrEmpty()) {
            mIsUpdating = true
            setText("")
            return
        }

        if (mValue > mMaxValue) {
            mValue = oldValue
        }

        val formatted = formatValue(mValue)

        mIsUpdating = true

        this@DecimalEditText.setText(formatted)
    }

    private fun formatValue(value: Double): String {
        val fmt = NumberFormat.getInstance() as DecimalFormat
        fmt.isGroupingUsed = true
        fmt.positiveSuffix = if (mSuffix.isNotEmpty()) " $mSuffix" else ""
        fmt.negativeSuffix = if (mSuffix.isNotEmpty()) " $mSuffix" else ""
        fmt.positivePrefix = if (mPrefix.isNotEmpty()) "$mPrefix " else ""
        fmt.negativePrefix = if (mPrefix.isNotEmpty()) "$mPrefix -" else ""
        fmt.minimumFractionDigits = mMaxDecimalDigit
        fmt.maximumFractionDigits = mMaxDecimalDigit
        fmt.maximumIntegerDigits = mMaxIntegerDigit
        return fmt.format(value)
    }

    private fun convertTextToValue(text: String?): Double {
        if (text.isNullOrEmpty()) {
            return 0.0
        }

        val symbols = "[$mPrefix$mSuffix,.€]"

        val cleanString = text.replace(symbols.toRegex(), "")
            .replace("\\s".toRegex(), "")

        val parsed = cleanString.toDouble()

        var divisor = 10
        for (i in 0 until mMaxDecimalDigit - 1) {
            divisor *= 10
        }

        return parsed / divisor
    }

    override fun onSelectionChanged(start: Int, end: Int) {
        if (isLaidOut) {
            val text = text
            if (text != null) {
                var position = if (mSuffix.isEmpty()) text.length
                else text.length - SUFFIX_SPACE_SIZE - mSuffix.length

                if (position < 0) {
                    position = 0
                }

                if (start != position || end != position) {
                    Selection.setSelection(text, position)
                    return
                }
            }
        }
    }

}