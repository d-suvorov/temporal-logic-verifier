package org.wotopul

class Symbol(val min: Set<LtlFormula>, val max: Set<LtlFormula>) {
    constructor(minmax: Set<LtlFormula>) : this(minmax, minmax)
    constructor(f: LtlFormula) : this(setOf(f))

    override fun toString() = "Symbol: <$min, $max>"
}

infix fun Symbol.subset(l: Symbol) =
    this.min.containsAll(l.min) && l.max.containsAll(this.max)
