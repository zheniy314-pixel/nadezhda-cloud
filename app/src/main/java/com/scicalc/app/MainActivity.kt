package com.scicalc.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.scicalc.app.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val expr = StringBuilder()
    private var lastAnswer: Double? = null
    private var degreeMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateAngleHint()
        wireButtons()
    }

    private fun updateAngleHint() {
        binding.textHint.text = if (degreeMode) {
            getString(R.string.hint_degrees)
        } else {
            getString(R.string.hint_radians)
        }
        binding.btnDegRad.text = if (degreeMode) "Deg" else "Rad"
    }

    private fun wireButtons() {
        val digitButtons = listOf(
            binding.btn0 to "0",
            binding.btn1 to "1",
            binding.btn2 to "2",
            binding.btn3 to "3",
            binding.btn4 to "4",
            binding.btn5 to "5",
            binding.btn6 to "6",
            binding.btn7 to "7",
            binding.btn8 to "8",
            binding.btn9 to "9",
            binding.btnDot to "."
        )
        digitButtons.forEach { (btn, s) -> btn.setOnClickListener { append(s) } }

        binding.btnAdd.setOnClickListener { append("+") }
        binding.btnSub.setOnClickListener { append("-") }
        binding.btnMul.setOnClickListener { append("*") }
        binding.btnDiv.setOnClickListener { append("/") }
        binding.btnPow.setOnClickListener { append("^") }
        binding.btnOpen.setOnClickListener { append("(") }
        binding.btnClose.setOnClickListener { append(")") }

        binding.btnPi.setOnClickListener { append("pi") }
        binding.btnE.setOnClickListener { append("e") }

        binding.btnSin.setOnClickListener { appendTrig("sin") }
        binding.btnCos.setOnClickListener { appendTrig("cos") }
        binding.btnTan.setOnClickListener { appendTrig("tan") }
        binding.btnAsin.setOnClickListener { appendInvTrig("asin") }
        binding.btnAcos.setOnClickListener { appendInvTrig("acos") }

        binding.btnLn.setOnClickListener { append("log(") }
        binding.btnLog10.setOnClickListener { append("log10(") }
        binding.btnSqrt.setOnClickListener { append("sqrt(") }
        binding.btnExp.setOnClickListener { append("exp(") }

        binding.btnAc.setOnClickListener {
            expr.clear()
            show("0")
        }
        binding.btnDel.setOnClickListener {
            if (expr.isNotEmpty()) {
                expr.deleteCharAt(expr.length - 1)
                refreshDisplayFromExpr()
            }
        }
        binding.btnAns.setOnClickListener {
            val v = lastAnswer ?: return@setOnClickListener Toast.makeText(
                this,
                getString(R.string.no_ans_yet),
                Toast.LENGTH_SHORT
            ).show()
            val s = formatNumber(v)
            append(s)
        }
        binding.btnEq.setOnClickListener { evaluate() }
        binding.btnDegRad.setOnClickListener {
            degreeMode = !degreeMode
            updateAngleHint()
        }
    }

    private fun appendTrig(name: String) {
        if (degreeMode) {
            append("${name}Deg(")
        } else {
            append("$name(")
        }
    }

    private fun appendInvTrig(name: String) {
        if (degreeMode) {
            append("${name}Deg(")
        } else {
            append("$name(")
        }
    }

    private fun append(s: String) {
        if (expr.isEmpty() && s == ".") {
            expr.append("0.")
        } else {
            expr.append(s)
        }
        refreshDisplayFromExpr()
    }

    private fun refreshDisplayFromExpr() {
        if (expr.isEmpty()) {
            show("0")
            return
        }
        val pretty = expr.toString()
            .replace("*", "×")
            .replace("/", "÷")
            .replace("log10(", "log(")
            .replace("asinDeg(", "asin(")
            .replace("acosDeg(", "acos(")
            .replace("sinDeg(", "sin(")
            .replace("cosDeg(", "cos(")
            .replace("tanDeg(", "tan(")
        show(pretty)
    }

    private fun show(text: String) {
        binding.textDisplay.text = text
    }

    private fun evaluate() {
        if (expr.isEmpty()) return
        val forEval = expr.toString()
        try {
            val value = ExpressionBuilder(forEval)
                .function(object : Function("sinDeg", 1) {
                    override fun apply(args: DoubleArray): Double =
                        kotlin.math.sin(Math.toRadians(args[0]))
                })
                .function(object : Function("cosDeg", 1) {
                    override fun apply(args: DoubleArray): Double =
                        kotlin.math.cos(Math.toRadians(args[0]))
                })
                .function(object : Function("tanDeg", 1) {
                    override fun apply(args: DoubleArray): Double =
                        kotlin.math.tan(Math.toRadians(args[0]))
                })
                .function(object : Function("asinDeg", 1) {
                    override fun apply(args: DoubleArray): Double =
                        Math.toDegrees(kotlin.math.asin(args[0]))
                })
                .function(object : Function("acosDeg", 1) {
                    override fun apply(args: DoubleArray): Double =
                        Math.toDegrees(kotlin.math.acos(args[0]))
                })
                .build()
                .evaluate()
            if (value.isNaN() || value.isInfinite()) {
                Toast.makeText(this, getString(R.string.error_domain), Toast.LENGTH_SHORT).show()
                return
            }
            lastAnswer = value
            val out = formatNumber(value)
            show(out)
            expr.clear()
            expr.append(out)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_syntax, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    private fun formatNumber(v: Double): String {
        if (kotlin.math.abs(v - v.toLong()) < 1e-12 && kotlin.math.abs(v) < 1e15) {
            return v.toLong().toString()
        }
        return "%.12g".format(v).trimEnd('0').trimEnd('.').ifEmpty { "0" }
    }
}
