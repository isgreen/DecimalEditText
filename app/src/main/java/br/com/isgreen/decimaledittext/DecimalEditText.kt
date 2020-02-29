package br.com.isgreen.decimaledittext

import android.content.Context
import android.text.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
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

    private var mPrefix = ""
    private var mSuffix = ""
    private var mMaxValue = Float.MAX_VALUE
    private var mMaxIntegerDigit = INT_MAX_VALUE
    private var mMaxDecimalDigit = INT_MAX_VALUE

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
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
        this.addTextChangedListener(onTextChange)

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

        tpArray.recycle()
    }

    fun getValue(): Double {
        return if (this.text.isNullOrBlank()) 0.0 else getValue(this.text.toString())
    }

    fun getValue(text: String): Double {
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
        val text = text
        if (text != null) {
            if (start != text.length || end != text.length) {
                if (mSuffix.isNotEmpty()) {
                    Selection.setSelection(text, text.length - SUFFIX_SPACE_SIZE - mSuffix.length)
                } else {
                    setSelection(text.length, text.length)
                }
                return
            }
        }

        super.onSelectionChanged(start, end)
    }

    private fun valueFormatter(value: Double): String {
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

    private val onTextChange = object : TextWatcher {

        private var oldValue: Double = 0.toDouble()
        private var isUpdating: Boolean = false

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (!TextUtils.isEmpty(s)) {
                oldValue = getValue(s.toString())
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (isUpdating) {
                isUpdating = false
                return
            }

            val text =
                if (TextUtils.isEmpty(s.toString())) valueFormatter(0.0) else s.toString()

            var value = getValue(text)

            if (value > mMaxValue) {
                value = oldValue
            }

            val formatted = valueFormatter(value)

            isUpdating = true

            this@DecimalEditText.setText(formatted)

            if (mSuffix.isNotEmpty()) {
                this@DecimalEditText.setSelection(formatted.length - SUFFIX_SPACE_SIZE - mSuffix.length)
            } else {
                this@DecimalEditText.setSelection(formatted.length)
            }
        }
    }
}