package com.example.basicskotlin

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import kotlin.math.*

data class CalculatorState(
    val currentOperation: String = "",
    val result: String = "0",
    val isError: Boolean = false,
    val isScientificMode: Boolean = false,
    val lastResult: Double = 0.0
)

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    data class Operation(val operation: String) : CalculatorAction()
    object Calculate : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object ToggleScientific : CalculatorAction()
    data class ScientificOperation(val operation: String) : CalculatorAction()
}

class CalculatorStateHolder {
    var state by mutableStateOf(CalculatorState())
        private set

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Operation -> enterOperation(action.operation)
            is CalculatorAction.Calculate -> {
                calculate()
                return
            }
            is CalculatorAction.Clear -> clear()
            is CalculatorAction.Delete -> deleteLast()
            is CalculatorAction.ToggleScientific -> toggleScientificMode()
            is CalculatorAction.ScientificOperation -> performScientificOperation(action.operation)
        }

        if (!state.currentOperation.contains('=')) {
            val visualCloses = getVisualCloses(state.result)

            val isCurrentResultANumber = state.result.toDoubleOrNull() != null
            if (!isCurrentResultANumber && !state.isError && state.result != "0" && visualCloses.isNotEmpty()) {
                state = state.copy(
                    currentOperation = state.result + visualCloses
                )
            } else {
                state = state.copy(
                    currentOperation = ""
                )
            }
        }
    }


    private fun enterNumber(number: Int) {
        if (state.isError) {
            clear()
        }

        val currentState = state
        var newResult = currentState.result

        if (newResult.endsWith('!') && newResult.length > 1) {
            return
        } else if (newResult.endsWith('!')) {
            newResult = number.toString() + "!"
        }
        else if (currentState.currentOperation.isNotEmpty() && currentState.currentOperation.contains('=')) {
            clear()
            newResult = ""
        }
        else if (newResult.endsWith(')')) {
            newResult += "X" + number.toString()
        }
        else if (newResult == "0" && number != 0) {
            newResult = ""
        } else if (newResult == "0" && number == 0) {
            return
        }

        if (newResult.length >= 20 && !newResult.endsWith('!')) return

        if (!newResult.endsWith('!')) {
            newResult += number.toString()
        }

        state = currentState.copy(
            result = newResult,
            currentOperation = if (currentState.currentOperation.contains('=')) "" else currentState.currentOperation,
            isError = false
        )
    }

    private fun enterOperation(operation: String) {
        if (state.isError) {
            clear()
        }

        var currentExpression = state.result
        val operatorChars = if (state.isScientificMode) "+-X/^" else "+-X/"
        val lastChar = currentExpression.lastOrNull()
        val isOperator = lastChar != null && operatorChars.contains(lastChar)
        val isBinaryOperator = operation in listOf("+", "X", "/", "^")

        if (currentExpression.endsWith("1/")) {
            currentExpression = currentExpression.dropLast(2)
        }

        if (currentExpression == "0" || currentExpression.isEmpty()) {
            if (operation == "-") {
                currentExpression = "-"
            } else if (operation == ".") {
                currentExpression = "0."
            } else {
                return
            }
        }

        else if (isOperator) {

            if (currentExpression == "-" && isBinaryOperator) {
                return
            }

            if (isBinaryOperator && currentExpression.length >= 2) {
                val secondLastChar = currentExpression[currentExpression.length - 2]

                if (lastChar == '-' && secondLastChar in listOf('+', 'X', '/', '^')) {
                    currentExpression = currentExpression.dropLast(2) + operation
                    state = state.copy(result = currentExpression, isError = false)
                    return
                }
            }

            if (operation == "-") {
                if (lastChar in listOf('+', 'X', '/', '^')) {
                    currentExpression += operation
                } else {
                    return
                }
            } else {
                currentExpression = currentExpression.dropLast(1) + operation
            }
        }

        else if (lastChar == '(') {
            if (operation == "-" || operation == ".") {
                currentExpression += operation
            } else {
                return
            }
        }

        else if (currentExpression.isNotEmpty()) {
            currentExpression += operation
        }

        state = state.copy(result = currentExpression, isError = false)
    }

    private fun calculate() {
        if (state.isError || state.result.isEmpty()) return

        try {
            var expression = state.result.replace("X", "*")
            if (expression.isEmpty()) return

            val openCount = expression.count { it == '(' }
            val closeCount = expression.count { it == ')' }

            val expressionWithCloses = StringBuilder(expression)

            if (openCount > closeCount) {
                val neededCloses = openCount - closeCount
                repeat(neededCloses) {
                    expression += ")"
                    expressionWithCloses.append(")")
                }
            }
            val resultValue = evaluateExpression(expression)

            val formattedResult = if (resultValue % 1.0 == 0.0) {
                resultValue.toLong().toString()
            } else {
                resultValue.toString()
            }

            state = state.copy(
                currentOperation = "${expressionWithCloses} =",
                result = formattedResult,
                isError = false,
                lastResult = resultValue
            )
        } catch (e: Exception) {
            state = state.copy(result = "Error", currentOperation = "", isError = true)
        }
    }

    private fun evaluateExpression(expression: String): Double {
        var currentExpression = expression

        if (currentExpression.isBlank()) return 0.0

        var simplifiedExpression = currentExpression
        val scientificOps = listOf("asin", "acos", "atan", "sqrt", "sin", "cos", "tan", "log", "ln")
        while (true) {
            var foundOperation = false

            val factorialIndex = simplifiedExpression.indexOf('!')
            if (factorialIndex != -1) {
                val numberStr = simplifiedExpression.substring(0, factorialIndex)
                val number = numberStr.toDoubleOrNull()

                if (number != null && number % 1.0 == 0.0 && number >= 0) {
                    val resultValue = factorial(number.toLong())
                    simplifiedExpression = resultValue.toString() + simplifiedExpression.substring(factorialIndex + 1)
                    foundOperation = true
                } else if (number != null && (number % 1.0 != 0.0 || number < 0)) {
                    throw IllegalArgumentException("Invalid input for factorial")
                }
            }

            else if (simplifiedExpression.startsWith("1/")) {
                val expressionToEvaluate = simplifiedExpression.substring(2)
                val operators = listOf('+', '-', '*', '/')
                val firstOperatorIndex = expressionToEvaluate.indexOfAny(operators.toCharArray())

                val argumentStr = if (firstOperatorIndex != -1) {
                    expressionToEvaluate.substring(0, firstOperatorIndex)
                } else {
                    expressionToEvaluate
                }

                val argument = argumentStr.toDoubleOrNull()

                if (argument != null && argument != 0.0) {
                    val resultValue = 1.0 / argument
                    simplifiedExpression = resultValue.toString() +
                            if (firstOperatorIndex != -1) expressionToEvaluate.substring(firstOperatorIndex) else ""
                    foundOperation = true
                } else if (argument == 0.0) {
                    throw ArithmeticException("Division by zero")
                }
            }

            for (op in scientificOps) {
                val opWithParen = "$op("
                val index = simplifiedExpression.indexOf(opWithParen)

                if (index != -1) {
                    val startOfArgument = index + opWithParen.length

                    var openCount = 1
                    var endParenIndex = -1

                    for (k in startOfArgument until simplifiedExpression.length) {
                        if (simplifiedExpression[k] == '(') openCount++
                        else if (simplifiedExpression[k] == ')') openCount--

                        if (openCount == 0) {
                            endParenIndex = k
                            break
                        }
                    }

                    if (endParenIndex != -1) {
                        val argumentStr = simplifiedExpression.substring(startOfArgument, endParenIndex)

                        val argumentValue = evaluateExpression(argumentStr)

                        val resultValue = when (op) {
                            "sqrt" -> sqrt(argumentValue)
                            "sin" -> sin(degreesToRadians(argumentValue)).roundToZeroIfClose()
                            "cos" -> cos(degreesToRadians(argumentValue)).roundToZeroIfClose()
                            "tan" -> tan(degreesToRadians(argumentValue))
                            "asin" -> radiansToDegrees(asin(argumentValue.coerceIn(-1.0, 1.0)))
                            "acos" -> radiansToDegrees(acos(argumentValue.coerceIn(-1.0, 1.0)))
                            "atan" -> radiansToDegrees(atan(argumentValue))
                            "log" -> log10(argumentValue)
                            "ln" -> ln(argumentValue)
                            else -> argumentValue
                        }

                        simplifiedExpression = simplifiedExpression.substring(0, index) +
                                resultValue.toString() +
                                simplifiedExpression.substring(endParenIndex + 1)
                        foundOperation = true
                        break
                    }
                }
            }

            val powerIndex = simplifiedExpression.lastIndexOf('^')
            if (powerIndex != -1) {
                val part1Str = simplifiedExpression.substring(0, powerIndex)
                val part2Str = simplifiedExpression.substring(powerIndex + 1)

                val part1 = part1Str.toDoubleOrNull() ?: 0.0
                val part2 = part2Str.toDoubleOrNull() ?: 0.0

                simplifiedExpression = part1.pow(part2).toString()
                foundOperation = true
            }

            if (!foundOperation) break
        }

        currentExpression = simplifiedExpression

        if (currentExpression.toDoubleOrNull() != null) return currentExpression.toDouble()

        return solveArithmetic(currentExpression)
    }

    private fun solveArithmetic(expression: String): Double {

        if (expression.isBlank()) return 0.0

        val tokens = mutableListOf<String>()
        var i = 0
        val n = expression.length

        while (i < n) {
            val char = expression[i]

            if ("+-*/".contains(char)) {
                val isUnary = (i == 0) || (tokens.isNotEmpty() && "+-*/".contains(tokens.last().last()))

                if (isUnary && (char == '-' || char == '+')) {
                    var j = i + 1
                    while (j < n && (expression[j].isDigit() || expression[j] == '.' || expression[j].equals('e', ignoreCase = true))) {
                        j++
                    }
                    tokens.add(expression.substring(i, j))
                    i = j
                } else {
                    tokens.add(char.toString())
                    i++
                }
            }
            else if (char.isDigit() || char == '.') {
                var j = i
                while (j < n && (expression[j].isDigit() || expression[j] == '.' || expression[j].equals('e', ignoreCase = true) || ((expression[j] == '-' || expression[j] == '+') && expression[j-1].equals('e', ignoreCase = true)))) {
                    j++
                }
                tokens.add(expression.substring(i, j))
                i = j
            }
            else {
                i++
            }
        }

        val values = mutableListOf<Double>()
        val ops = mutableListOf<Char>()

        for (token in tokens) {
            if (token.length == 1 && "+-*/".contains(token)) {
                ops.add(token[0])
            } else if (token.isNotBlank()) {
                values.add(token.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number in expression: $token"))
            }
        }

        if (values.isEmpty()) return 0.0

        var index = 0
        while (index < ops.size) {
            val op = ops[index]
            if (op == '*' || op == '/') {
                val val1 = values[index]
                val val2 = values[index + 1]
                val result = if (op == '*') val1 * val2 else {
                    if (val2 == 0.0) throw ArithmeticException("Division by zero") else val1 / val2
                }

                values[index] = result
                values.removeAt(index + 1)
                ops.removeAt(index)
            } else {
                index++
            }
        }

        var finalResult = values.first()
        for (j in 0 until ops.size) {
            finalResult = when (ops[j]) {
                '+' -> finalResult + values[j + 1]
                '-' -> finalResult - values[j + 1]
                else -> throw IllegalStateException("Unexpected operator: ${ops[j]}")
            }
        }

        return finalResult
    }


    private fun clear() {
        state = CalculatorState()
    }

    private fun deleteLast() {
        if (state.isError) {
            clear()
            return
        }

        val currentState = state

        if (currentState.result.isNotEmpty() && currentState.result != "0") {
            var resultAfterDeletion = currentState.result.dropLast(1)

            if (resultAfterDeletion.endsWith("1")) {
                resultAfterDeletion = resultAfterDeletion.dropLast(1)
            }
            val scientificOps = listOf("sqrt(", "asin(", "acos(", "atan(", "sin(", "cos(", "tan(", "log(", "ln(")
            if (resultAfterDeletion.endsWith('(')) {
                for (op in scientificOps) {
                    if (resultAfterDeletion.endsWith(op)) {
                        resultAfterDeletion = resultAfterDeletion.dropLast(op.length)
                        break
                    }
                }
            }

            state = currentState.copy(
                result = if (resultAfterDeletion.isEmpty()) "0" else resultAfterDeletion,
                currentOperation = if (currentState.currentOperation.contains('=')) "" else currentState.currentOperation,
                isError = false
            )
        } else {
            state = currentState.copy(currentOperation = "")
        }
    }

    private fun toggleScientificMode() {
        state = state.copy(isScientificMode = !state.isScientificMode)
    }

    private fun performScientificOperation(op: String) {
        if (state.isError) {
            clear()
            return
        }

        val currentState = state
        var currentExpression = currentState.result

        val isAfterCalculate = currentState.currentOperation.contains('=')

        val pendingOps = listOf("sqrt(x)", "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "1/x", "x^y", "x!")
        val isPendingOp = pendingOps.contains(op)

        val wrappingOps = listOf("sqrt(x)", "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "1/x")

        val isCurrentResultANumber = currentExpression.toDoubleOrNull() != null || isAfterCalculate

        if (isPendingOp) {
            val textToAdd = when (op) {
                "x^y" -> "^"
                "x!" -> "!"
                "1/x" -> "1/"
                "sqrt(x)" -> "sqrt("
                "sin" -> "sin("
                "cos" -> "cos("
                "tan" -> "tan("
                "asin" -> "asin("
                "acos" -> "acos("
                "atan" -> "atan("
                "log" -> "log("
                "ln" -> "ln("
                else -> ""
            }

            var newResult = currentExpression

            if (op == "x^y" || op == "x!") {
                if (op == "x^y") {
                    val operatorChars = "+-X/^"
                    val lastChar = currentExpression.lastOrNull()
                    if (currentExpression.isNotEmpty() && (lastChar == null || !operatorChars.contains(lastChar))) {
                        newResult = currentExpression + textToAdd
                    }
                }
                else if (currentExpression != "0" || isAfterCalculate) {
                    newResult = currentExpression + textToAdd
                }
            }
            else if (wrappingOps.contains(op)) {

                if (isCurrentResultANumber) {

                    if (currentExpression == "0") {
                        newResult = textToAdd
                    }
                    else {
                        newResult = textToAdd + currentExpression + (if(op != "1/x") ")" else "")
                    }
                } else {
                    newResult = currentExpression + textToAdd
                }
            }



            state = currentState.copy(
                result = newResult,
                currentOperation = if (isAfterCalculate) "" else currentState.currentOperation,
                isError = false
            )
            return
        }
    }

    private fun factorial(n: Long): Double {
        return if (n < 0) {
            Double.NaN
        } else if (n == 0L || n == 1L) {
            1.0
        } else {
            var result = 1.0
            for (i in 2..n) {
                result *= i
            }
            result
        }
    }

    private fun degreesToRadians(degrees: Double): Double {
        return degrees * PI / 180.0
    }

    private fun radiansToDegrees(radians: Double): Double {
        return radians * 180.0 / PI
    }

    private val EPSILON = 1E-14

    private fun Double.roundToZeroIfClose(): Double {
        return if (abs(this) < EPSILON) 0.0 else this
    }

    private fun getVisualCloses(expression: String): String {
        val openCount = expression.count { it == '(' }
        val closeCount = expression.count { it == ')' }

        return if (openCount > closeCount) {
            ")".repeat(openCount - closeCount)
        } else {
            ""
        }
    }

    companion object {
        val Saver = Saver<CalculatorStateHolder, List<Any>>(
            save = { listOf(it.state.currentOperation, it.state.result, it.state.isError, it.state.isScientificMode, it.state.lastResult) },
            restore = {
                val holder = CalculatorStateHolder()
                holder.state = CalculatorState(
                    currentOperation = it[0] as String,
                    result = it[1] as String,
                    isError = it[2] as Boolean,
                    isScientificMode = it[3] as Boolean,
                    lastResult = it[4] as Double
                )
                holder
            }
        )
    }
}