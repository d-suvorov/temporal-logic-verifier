package org.wotopul

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream

fun main(args: Array<String>) {
    val usage = """
    |Usage: java -jar verifier.jar <automaton> <formula>
    |Where:
    | <automaton> - path to a an XML-file containing automaton definition
    | <formula>   - LTL-formula
    """.trimMargin()
    if (args.size != 2) {
        print(usage)
        System.exit(0)
    }

    val automaton = Automaton.readAutomatonFromFile(args[0])
    val ltlFormula = parseLtlFormula(args[1])

    val buchiAutomaton = BuchiAutomaton(automaton)
    val generalizedBuchiAutomatonByLtl = GeneralizedLabeledBuchiAutomaton(ltlFormula)
    val buchiAutomatonByLtl = BuchiAutomaton(generalizedBuchiAutomatonByLtl)

    val answer = findPath(buchiAutomaton, buchiAutomatonByLtl)
    if (answer.holds)
        println("Formula ${args[1]} holds")
    else
        println("Formula ${args[1]} does not hold")
}

fun parseLtlFormula(input: String): LtlFormula {
    val lexer = LtlLexer(ANTLRInputStream(input))
    val parser = LtlParser(CommonTokenStream(lexer))
    val formula = parser.formula()
    return LtlFormulaBuilder().visit(formula)
}
