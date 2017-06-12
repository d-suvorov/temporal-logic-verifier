package org.wotopul

import org.wotopul.LtlFormula.*

class LtlFormulaBuilder : LtlBaseVisitor<LtlFormula>() {
    override fun visitParenthesis(ctx: LtlParser.ParenthesisContext?): LtlFormula =
        // children by their indices must be:
        // 0 - open parenthesis, 1 - expression, 2 - close parenthesis
        visit(ctx!!.getChild(1))

    override fun visitNegation(ctx: LtlParser.NegationContext?) =
        Not(visit(ctx!!.formula()))

    override fun visitConjunction(ctx: LtlParser.ConjunctionContext?) =
        And(visit(ctx!!.lhs), visit(ctx.rhs))

    override fun visitDisjunction(ctx: LtlParser.DisjunctionContext?) =
        Or(visit(ctx!!.lhs), visit(ctx.rhs))

    override fun visitImplication(ctx: LtlParser.ImplicationContext?) =
        Or(Not(visit(ctx!!.lhs)), visit(ctx.rhs))

    override fun visitNext(ctx: LtlParser.NextContext?) =
        Next(visit(ctx!!.formula()))

    override fun visitFuture(ctx: LtlParser.FutureContext?) =
        Future(visit(ctx!!.formula()))

    override fun visitGlobally(ctx: LtlParser.GloballyContext?) =
        Globally(visit(ctx!!.formula()))

    override fun visitUntil(ctx: LtlParser.UntilContext?) =
        Until(visit(ctx!!.lhs), visit(ctx.rhs))

    override fun visitRelease(ctx: LtlParser.ReleaseContext?) =
        Release(visit(ctx!!.lhs), visit(ctx.rhs))

    override fun visitVariable(ctx: LtlParser.VariableContext?) =
        Variable(ctx!!.ID().text)

    override fun visitBooleanLiteral(ctx: LtlParser.BooleanLiteralContext?) =
        when (ctx!!.text) {
            "false" -> False
            "true" -> True
            else -> throw AssertionError("unknown boolean value: ${ctx.text}")
        }
}