package com.scientific.calculator

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.scientific.calculator.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var expr = StringBuilder()
    private var lastAnswer = 0.0
    private var useDegrees = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            expr.append(savedInstanceState.getString(KEY_EXPR, ""))
            lastAnswer = savedInstanceState.getDouble(KEY_ANS, 0.0)
            useDegrees = savedInstanceState.getBoolean(KEY_DEG, false)
        }
        updateModeLabel()
        refreshDisplay()

        val digitOps = mapOf(
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
            binding.btnDot to ".",
            binding.btnAdd to "+",
            binding.btnSub to "−",
            binding.btnMul to "×",
            binding.btnDiv to "÷",
            binding.btnPow to "^",
            binding.btnParenOpen to "(",
            binding.btnParenClose to ")",
        )
        digitOps.forEach { (btn, s) -> btn.setOnClickListener { append(s) } }

        binding.btnPi.setOnClickListener { append("π") }
        binding.btnE.setOnClickListener { append("e") }
        binding.btnAns.setOnClickListener { append("Ans") }

        binding.btnSin.setOnClickListener { appendFunc("sin(") }
        binding.btnCos.setOnClickListener { appendFunc("cos(") }
        binding.btnTan.setOnClickListener { appendFunc("tan(") }
        binding.btnLn.setOnClickListener { appendFunc("ln(") }
        binding.btnLog.setOnClickListener { appendFunc("log(") }
        binding.btnSqrt.setOnClickListener { appendFunc("sqrt(") }
        binding.btnFact.setOnClickListener { appendFunc("fact(") }

        binding.btnPercent.setOnClickListener { append("%") }

        binding.btnAc.setOnClickListener {
            expr.clear()
            refreshDisplay()
        }
        binding.btnDel.setOnClickListener {
            if (expr.isNotEmpty()) {
                expr.deleteCharAt(expr.length - 1)
                refreshDisplay()
            }
        }

        binding.btnDegRad.setOnClickListener {
            useDegrees = !useDegrees
            updateModeLabel()
        }

        binding.btnEquals.setOnClickListener { evaluate() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_EXPR, expr.toString())
        outState.putDouble(KEY_ANS, lastAnswer)
        outState.putBoolean(KEY_DEG, useDegrees)
    }

    private fun updateModeLabel() {
        binding.textMode.text = if (useDegrees) "Угол: °" else "Угол: рад"
        binding.btnDegRad.text = if (useDegrees) "DEG" else "RAD"
    }

    private fun append(s: String) {
        expr.append(s)
        refreshDisplay()
    }

    private fun appendFunc(prefix: String) {
        expr.append(prefix)
        refreshDisplay()
    }

    private fun refreshDisplay() {
        val t = expr.toString()
        binding.textDisplay.text = if (t.isEmpty()) "0" else t
        binding.scrollDisplay.post {
            binding.scrollDisplay.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    private fun evaluate() {
        val raw = expr.toString().trim()
        if (raw.isEmpty()) return
        try {
            val prepared = preprocessPercent(transformForEngine(raw))
            val factFn = object : Function("fact", 1) {
                override fun apply(args: DoubleArray): Double {
                    val x = args[0]
                    if (x < 0 || x > 170 || abs(x - floor(x)) > 1e-9) {
                        throw ArithmeticException("fact")
                    }
                    var n = x.toInt()
                    var r = 1.0
                    for (i in 2..n) r *= i.toDouble()
                    return r
                }
            }
            val log10Fn = object : Function("log10", 1) {
                override fun apply(args: DoubleArray) = log10(args[0])
            }
            val sinFn = object : Function("sin", 1) {
                override fun apply(args: DoubleArray) = if (useDegrees) {
                    kotlin.math.sin(Math.toRadians(args[0]))
                } else {
                    kotlin.math.sin(args[0])
                }
            }
            val cosFn = object : Function("cos", 1) {
                override fun apply(args: DoubleArray) = if (useDegrees) {
                    kotlin.math.cos(Math.toRadians(args[0]))
                } else {
                    kotlin.math.cos(args[0])
                }
            }
            val tanFn = object : Function("tan", 1) {
                override fun apply(args: DoubleArray) = if (useDegrees) {
                    kotlin.math.tan(Math.toRadians(args[0]))
                } else {
                    kotlin.math.tan(args[0])
                }
            }
            val expression = ExpressionBuilder(prepared)
                .function(factFn)
                .function(log10Fn)
                .function(sinFn)
                .function(cosFn)
                .function(tanFn)
                .build()
            expression.setVariable("Ans", lastAnswer)
            expression.setVariable("ans", lastAnswer)

            val value = expression.evaluate()
            if (value.isNaN() || value.isInfinite()) {
                throw ArithmeticException("nan")
            }
            lastAnswer = value
            val shown = formatResult(value)
            expr.clear()
            expr.append(shown)
            refreshDisplay()
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.error_invalid), Toast.LENGTH_SHORT).show()
        }
    }

    /** Процент: число% → число/100 (простой случай в конце выражения). */
    private fun preprocessPercent(s: String): String {
        if (!s.contains("%")) return s
        val re = Regex("""([\d.]+)%""")
        return re.replace(s) { m ->
            val num = m.groupValues[1].toDoubleOrNull() ?: return@replace m.value
            (num / 100.0).toString()
        }
    }

    private fun transformForEngine(display: String): String {
        // ln → встроенный log exp4j; log → log10 через пользовательскую функцию
        val s = display
            .replace("ln(", "\u0000NAT(")
            .replace("log(", "log10(")
            .replace("\u0000NAT(", "log(")
        return s
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("Ans", "ans")
    }

    private fun formatResult(v: Double): String {
        val r = v.roundToLong()
        if (abs(v - r) < 1e-12 && abs(r) < 1e15) return r.toString()
        return "%.12g".format(v).trimEnd('0').trimEnd('.')
    }

    companion object {
        private const val KEY_EXPR = "expr"
        private const val KEY_ANS = "ans"
        private const val KEY_DEG = "deg"
    }
}
