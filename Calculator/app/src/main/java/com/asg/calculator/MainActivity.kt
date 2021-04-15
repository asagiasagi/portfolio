package com.asg.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {
    //----------------------------------------------------------
    // Field
    //----------------------------------------------------------
    private lateinit var mHistoryView: TextView     // 計算過程を表示する
    private lateinit var mResultView: TextView      // 現在入力中の値と計算結果を表示する。

    private var mCalcType = CalcType.CALC_TYPE_NONE // 計算状態
    private var mCurNum = 0                         // 現在入力中の数値
    private var mPrevNum = 0                        // 演算子選択までに入力した数値

    //----------------------------------------------------------
    // Life Cycle
    //----------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 数値
        findViewById<Button>(R.id.button_0).setOnClickListener(this)
        findViewById<Button>(R.id.button_1).setOnClickListener(this)
        findViewById<Button>(R.id.button_2).setOnClickListener(this)
        findViewById<Button>(R.id.button_3).setOnClickListener(this)
        findViewById<Button>(R.id.button_4).setOnClickListener(this)
        findViewById<Button>(R.id.button_5).setOnClickListener(this)
        findViewById<Button>(R.id.button_6).setOnClickListener(this)
        findViewById<Button>(R.id.button_7).setOnClickListener(this)
        findViewById<Button>(R.id.button_8).setOnClickListener(this)
        findViewById<Button>(R.id.button_9).setOnClickListener(this)

        // 四則演算
        findViewById<Button>(R.id.button_add).setOnClickListener(this)
        findViewById<Button>(R.id.button_sub).setOnClickListener(this)
        findViewById<Button>(R.id.button_mlt).setOnClickListener(this)
        findViewById<Button>(R.id.button_div).setOnClickListener(this)

        // その他キー
        findViewById<Button>(R.id.button_bck).setOnClickListener(this)
        findViewById<Button>(R.id.button_clr).setOnClickListener(this)
        findViewById<Button>(R.id.button_res).setOnClickListener(this)

        mHistoryView = findViewById(R.id.textview_history)
        mResultView = findViewById(R.id.textview_result)
    }

    //----------------------------------------------------------
    // Button Click
    //----------------------------------------------------------
    override fun onClick(v: View?) {
        v?.id?.also {
            when (it) {
                // 数値キー
                R.id.button_0,
                R.id.button_1,
                R.id.button_2,
                R.id.button_3,
                R.id.button_4,
                R.id.button_5,
                R.id.button_6,
                R.id.button_7,
                R.id.button_8,
                R.id.button_9 -> {
                    btnIsNumber(it)
                }
                // 四則演算キー
                R.id.button_add,
                R.id.button_sub,
                R.id.button_mlt,
                R.id.button_div -> {
                    btnIsOperator(it)
                }
                // バックキー
                R.id.button_bck -> {
                    btnIsBck()
                }
                // クリアキー
                R.id.button_clr -> {
                    btnIsClr()
                }
                // リザルトキー
                R.id.button_res -> {
                    btnIsRes()
                }
                else -> {
                    // 何もしない
                }
            }
        }
    }

    //----------------------------------------------------------
    // Private Method (Button Click Action)
    //----------------------------------------------------------
    // 数値（0-9）キー
    private fun btnIsNumber(id: Int) {
        if (mCalcType == CalcType.CALC_TYPE_RESULT) return  // 計算結果を出したあとは何もしない

        val value = when (id) {
            R.id.button_0 -> 0
            R.id.button_1 -> 1
            R.id.button_2 -> 2
            R.id.button_3 -> 3
            R.id.button_4 -> 4
            R.id.button_5 -> 5
            R.id.button_6 -> 6
            R.id.button_7 -> 7
            R.id.button_8 -> 8
            R.id.button_9 -> 9
            else -> -1
        }

        // 現在の値と表示を更新する。
        if (value >= 0) {
            mCurNum = mCurNum * 10 + value
            mResultView.text = mCurNum.toString()
        }
    }

    // 四則演算（+,-,*,/）キー
    private fun btnIsOperator(id: Int) {
        if (mCalcType == CalcType.CALC_TYPE_RESULT) return // 計算結果を出したあとは何もしない。

        var needChangeOperator = false  // 演算子の変更と表示の更新は後で行う。
        if (mCalcType == CalcType.CALC_TYPE_NONE) { // 演算子選択前
            if (mCurNum <= 0) {
                // 数値入力前(0以下) -> 何もしない。
            } else {
                // 数値入力後 -> 演算子を変更し、現在の数値を記憶する。
                needChangeOperator = true
                mPrevNum = mCurNum
                mCurNum = 0
                mResultView.text = mCurNum.toString()
            }

        } else { // 演算子選択後
            if (mCurNum <= 0) {
                // 数値入力前(0以下) -> 演算子を変更する。
                needChangeOperator = true
            } else {
                // 数値入力後 -> 何もしない。
            }
        }

        // 演算子を変更し、履歴表示を更新する。
        if (needChangeOperator) {
            mCalcType = when (id) {
                R.id.button_add -> CalcType.CALC_TYPE_ADD
                R.id.button_sub -> CalcType.CALC_TYPE_SUB
                R.id.button_mlt -> CalcType.CALC_TYPE_MLT
                R.id.button_div -> CalcType.CALC_TYPE_DIV
                else -> CalcType.CALC_TYPE_NONE
            }

            mHistoryView.text = ""
            mHistoryView.append(mPrevNum.toString())
            mHistoryView.append(" " + mCalcType.operator + " ")
        }
    }

    // バックキー
    private fun btnIsBck() {
        if (mCalcType == CalcType.CALC_TYPE_RESULT) return  // 計算結果を出した後の場合は何もしない

        mCurNum /= 10   // 10分の1にすることで、下一桁を切り落とす。
        mResultView.text = mCurNum.toString()
    }

    // クリアキー
    private fun btnIsClr() {
        mCalcType = CalcType.CALC_TYPE_NONE
        mPrevNum = 0
        mCurNum = 0
        mHistoryView.text = ""
        mResultView.text = mCurNum.toString()
    }

    // リザルトキー
    private fun btnIsRes() {
        if (mCalcType == CalcType.CALC_TYPE_RESULT) return  // 計算結果を出した後の場合は何もしない

        // 履歴表示を更新し、計算結果を表示する。
        mHistoryView.append(mCurNum.toString())
        mResultView.text = mCalcType.calculation(mPrevNum, mCurNum).toString()
        mCalcType = CalcType.CALC_TYPE_RESULT
    }
}