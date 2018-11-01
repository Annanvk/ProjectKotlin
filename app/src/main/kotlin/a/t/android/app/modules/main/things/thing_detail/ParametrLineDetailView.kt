package io.tage.android.app.modules.main.things.thing_detail

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.tage.android.app.R

class ParametrLineDetailView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    private lateinit var textKey: TextView
    private lateinit var textValue: TextView

    init {
        init()
    }

    private fun init() {
        val v = View.inflate(context, R.layout.layout_for_detail, this)
        textKey = v.findViewById(R.id.text_key)
        textValue = v.findViewById(R.id.text_value)
    }

    @SuppressLint("SetTextI18n")
    fun fillParamsKey(key: String, value: String) {
        textKey.text = "$key: "
        textValue.text = "$value"
    }
}
