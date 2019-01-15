package com.cryptape.cita_wallet.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.view_compress_edittext.view.*
import com.cryptape.cita_wallet.R
import com.cryptape.cita_wallet.view.tool.CytonTextWatcher

/**
 * Created by BaojunCZ on 2018/12/19.
 */
class CompressEditText(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var isClick = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_compress_edittext, this)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CompressEditText)
        initView(ta)
        initAction()
    }

    fun initView(ta: TypedArray) {
        val textSize = ta.getDimension(R.styleable.CompressEditText_text_size, "0".toFloat())
        if (textSize != "0".toFloat()) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            et.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        }
        val textColor = ta.getColor(R.styleable.CompressEditText_text_color, ContextCompat.getColor(context, R.color.font_title))
        tv.setTextColor(textColor)
        et.setTextColor(textColor)
        val editHint = ta.getResourceId(R.styleable.CompressEditText_edit_hint, -1)
        if (editHint != -1) {
            et.setHint(editHint)
        }
        et.gravity = gravity
        tv.gravity = gravity
    }

    fun initAction() {
        tv.setOnClickListener {
            showEditText()
            isClick = true
            et.requestFocus()
            et.performClick()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        }
        et.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            et.getWindowVisibleDisplayFrame(r)
            val screenHeight = et.rootView.height
            val heightDifference = screenHeight - r.bottom
            if (heightDifference > 200) {
                //soft keyboard show
                if (isClick) {
                    showEditText()
                    isClick = false
                } else {
                    showText()
                }
            } else {
                //soft keyboard hide
                if (!isClick) {
                    showText()
                }
            }
        }
        et.setOnFocusChangeListener { _, b ->
            if (!b) {
                showText()
            }
        }
    }

    var gravity: Int = Gravity.START
        set(value) {
            et.gravity = value
            tv.gravity = value
        }

    var text: String?
        get() = et.text.toString().trim()
        set(value) {
            et.setText(value)
            tv.text = value
        }

    var hint: Int = 0
        set(value) {
            et.setHint(value)
        }

    var textWatcher: CytonTextWatcher? = null
        set(value) {
            et.addTextChangedListener(value)
        }

    private fun showText() {
        if (!TextUtils.isEmpty(et.text.toString().trim())) {
            tv.visibility = View.VISIBLE
            et.visibility = View.GONE
            tv.text = et.text.toString().trim()
        }
    }

    private fun showEditText() {
        tv.visibility = View.GONE
        et.visibility = View.VISIBLE
    }

}