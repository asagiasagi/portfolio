package com.asg.calculator

enum class CalcType(val operator: String) {
    CALC_TYPE_NONE("") {
        override fun calculation(i1: Int, i2: Int): Int {
            return 0
        }
    },
    CALC_TYPE_ADD("+") {
        override fun calculation(i1: Int, i2: Int): Int {
            return i1 + i2
        }
    },
    CALC_TYPE_SUB("-") {
        override fun calculation(i1: Int, i2: Int): Int {
            return i1 - i2
        }
    },
    CALC_TYPE_MLT("×") {
        override fun calculation(i1: Int, i2: Int): Int {
            return i1 * i2
        }
    },
    CALC_TYPE_DIV("÷") {
        override fun calculation(i1: Int, i2: Int): Int {
            if (i2 == 0) return i1
            return i1 / i2
        }
    },
    CALC_TYPE_RESULT("") {
        override fun calculation(i1: Int, i2: Int): Int {
            return 0
        }
    };

    abstract fun calculation(i1: Int, i2: Int): Int
}