package org.wotopul

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream

fun main(args: Array<String>) {
    val usage = """
    |Usage: java -jar verifier.jar <automaton> <formula> [<debug-mode>]
    |Where:
    | <automaton> - path to a an XML-file containing automaton definition
    | <formula>   - LTL-formula
    """.trimMargin()
    if (args.size !in 2 .. 3) {
        print(usage)
        System.exit(0)
    }

    val automatonFile = args[0]
    val formula = args[1]
    val debugMode = if (args.size == 3) args[2].toBoolean() else false

    val automaton = Automaton.readAutomatonFromFile(automatonFile)
    val ltlFormula = LtlFormula.Not(parseLtlFormula(formula))

    val buchiAutomaton = BuchiAutomaton(automaton)
    val generalizedBuchiAutomatonByLtl = GeneralizedLabeledBuchiAutomaton(ltlFormula, buchiAutomaton.sigma)
    val buchiAutomatonByLtl = BuchiAutomaton(generalizedBuchiAutomatonByLtl)

    if (debugMode) {
        println("From automaton:\n")
        println(buchiAutomaton)
        println()
        println("From LTL formula:\n")
        println(buchiAutomatonByLtl)
    }

    val answer = findPath(buchiAutomaton, buchiAutomatonByLtl)
    if (answer.holds)
        println("Formula $formula holds")
    else
        println("Formula $formula does not hold")
}

fun parseLtlFormula(input: String): LtlFormula {
    val lexer = LtlLexer(ANTLRInputStream(input))
    val parser = LtlParser(CommonTokenStream(lexer))
    val formula = parser.formula()
    return LtlFormulaBuilder().visit(formula)
}
