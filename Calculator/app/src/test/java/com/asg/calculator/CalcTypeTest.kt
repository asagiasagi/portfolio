package com.asg.calculator

import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class CalcTypeTest : TestCase() {
    private var mCalcType = CalcType.CALC_TYPE_NONE

    @After
    override fun tearDown() {
        mCalcType = CalcType.CALC_TYPE_NONE
    }

    fun testCalcAdd() {
        mCalcType = CalcType.CALC_TYPE_ADD

        assertEquals(mCalcType.calculation(0, 0), 0)
        assertEquals(mCalcType.calculation(0, 1), 1)
        assertEquals(mCalcType.calculation(1, 0), 1)
        assertEquals(mCalcType.calculation(1, 1), 2)
    }

    fun testCalcSub() {
        mCalcType = CalcType.CALC_TYPE_SUB

        assertEquals(mCalcType.calculation(0, 0), 0)
        assertEquals(mCalcType.calculation(0, 1), -1)
        assertEquals(mCalcType.calculation(1, 0), 1)
        assertEquals(mCalcType.calculation(1, 1), 0)
    }

    fun testCalcMlt() {
        mCalcType = CalcType.CALC_TYPE_MLT

        assertEquals(mCalcType.calculation(0, 0), 0)
        assertEquals(mCalcType.calculation(0, 1), 0)
        assertEquals(mCalcType.calculation(1, 0), 0)
        assertEquals(mCalcType.calculation(1, 1), 1)
    }

    fun testCalcDiv() {
        mCalcType = CalcType.CALC_TYPE_DIV

        assertEquals(mCalcType.calculation(0, 0), 0)
        assertEquals(mCalcType.calculation(0, 1), 0)
        assertEquals(mCalcType.calculation(1, 0), 1)
        assertEquals(mCalcType.calculation(1, 1), 1)
    }
}