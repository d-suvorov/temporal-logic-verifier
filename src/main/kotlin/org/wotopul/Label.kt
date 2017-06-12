package org.wotopul

class Label(val min: Set<LtlFormula>, val max: Set<LtlFormula>) {
    constructor(label: Set<LtlFormula>) : this(label, label)
    constructor(f: LtlFormula) : this(setOf(f))
}

infix fun Label.subset(l: Label) =
    this.min.containsAll(l.min) && l.max.containsAll(this.max)
