package com.utfpr.gestaofrotaapp.core.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Formata apenas dígitos como "+55 XX XXXXX-XXXX" (Brasil).
 * O valor no estado deve conter só dígitos (máx. 13: 55 + DDD + 9 dígitos).
 * Ao enviar para a API, use [formatPhoneForApi] para obter "+5511912345678".
 */
object PhoneNumberTransformation : VisualTransformation {

    private const val MAX_DIGITS = 13

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(MAX_DIGITS)
        val formatted = formatDigits(digits)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val n = offset.coerceAtMost(digits.length)
                return formatDigits(digits.take(n)).length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                var count = 0
                for (i in 0 until offset.coerceAtMost(formatted.length)) {
                    if (formatted[i].isDigit()) count++
                }
                return count
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }

    private fun formatDigits(digits: String): String {
        if (digits.isEmpty()) return ""
        val sb = StringBuilder("+")
        digits.forEachIndexed { index, c ->
            when (index) {
                0, 1 -> sb.append(c)           // +55
                2 -> { sb.append(' '); sb.append(c) }
                3 -> sb.append(c)               // XX (DDD)
                4 -> { sb.append(' '); sb.append(c) }
                5, 6, 7, 8, 9 -> sb.append(c)  // XXXXX
                10 -> { sb.append('-'); sb.append(c) }
                11, 12 -> sb.append(c)         // XXXX
            }
        }
        return sb.toString()
    }
}

/**
 * Retorna o número no formato E.164 para Firebase (ex: "+5511912345678").
 * Espera [digitsOnly] com apenas dígitos (ex: "5511912345678").
 */
fun formatPhoneForApi(digitsOnly: String): String {
    val digits = digitsOnly.filter { it.isDigit() }
    return if (digits.isNotEmpty()) "+$digits" else ""
}
