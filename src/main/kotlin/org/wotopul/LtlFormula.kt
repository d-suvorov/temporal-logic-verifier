package org.wotopul

import org.wotopul.LtlFormula.*

sealed class LtlFormula {
    object False : LtlFormula()
    object True : LtlFormula()

    class Variable(val name: String) : LtlFormula() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Variable

            if (name != other.name) return false

            return true
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }

    class Not(val sub: LtlFormula) : LtlFormula() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Not

            if (sub != other.sub) return false

            return true
        }

        override fun hashCode(): Int {
            return sub.hashCode()
        }
    }

    class And(val lhs: LtlFormula, val rhs: LtlFormula) : LtlFormula() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as And

            if (lhs != other.lhs) return false
            if (rhs != other.rhs) return false

            return true
        }

        override fun hashCode(): Int {
            var result = lhs.hashCode()
            result = 31 * result + rhs.hashCode()
            return result
        }
    }

    class Or(val lhs: LtlFormula, val rhs: LtlFormula) : LtlFormula() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Or

            if (lhs != other.lhs) return false
            if (rhs != other.rhs) return false

            return true
        }

        override fun hashCode(): Int {
            var result = lhs.hashCode()
            result = 31 * result + rhs.hashCode()
            return result
        }
    }

    class Next(val sub: LtlFormula) : LtlFormula() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Next

            if (sub != other.sub) return false

            return true
        }

        override fun hashCode(): Int {
            return sub.hashCode()
        }
    }

    class Future(val sub: LtlFormula) : LtlFormula() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Future

            if (sub != other.sub) return false

            return true
        }

        override fun hashCode(): Int {
            return sub.hashCode()
        }
    }

    class Globally(val sub: LtlFormula) : LtlFormula() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Globally

            if (sub != other.sub) return false

            return true
        }

        override fun hashCode(): Int {
            return sub.hashCode()
        }
    }

    class Until(val lhs: LtlFormula, val rhs: LtlFormula) : LtlFormula() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Until

            if (lhs != other.lhs) return false
            if (rhs != other.rhs) return false

            return true
        }

        override fun hashCode(): Int {
            var result = lhs.hashCode()
            result = 31 * result + rhs.hashCode()
            return result
        }
    }

    class Release(val lhs: LtlFormula, val rhs: LtlFormula) : LtlFormula() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Release

            if (lhs != other.lhs) return false
            if (rhs != other.rhs) return false

            return true
        }

        override fun hashCode(): Int {
            var result = lhs.hashCode()
            result = 31 * result + rhs.hashCode()
            return result
        }
    }

    fun inNNF(): Boolean = when (this) {
        is False, True -> true
        is Variable -> true

        is Not -> sub is Variable

        is And -> lhs.inNNF() && rhs.inNNF()
        is Or -> lhs.inNNF() && rhs.inNNF()

        is Next -> sub.inNNF()

        is Future -> false
        is Globally -> false

        is Until -> lhs.inNNF() && rhs.inNNF()
        is Release -> lhs.inNNF() && rhs.inNNF()
    }

    fun isAtomOrNegAtom() = this is Variable || (this is Not && this.sub is Variable)

    fun atomPropositions(): Set<LtlFormula> = when(this) {
        is False, True -> emptySet()
        is Variable -> setOf(this)

        is Not -> this.sub.atomPropositions()
        is And -> this.lhs.atomPropositions() + this.rhs.atomPropositions()
        is Or -> this.lhs.atomPropositions() + this.rhs.atomPropositions()

        is Next -> this.sub.atomPropositions()
        is Future -> this.sub.atomPropositions()
        is Globally -> this.sub.atomPropositions()
        is Until -> this.lhs.atomPropositions() + this.rhs.atomPropositions()
        is Release -> this.lhs.atomPropositions() + this.rhs.atomPropositions()
    }

    fun closure(): Set<LtlFormula> {
        val res = mutableSetOf<LtlFormula>()

        fun closureImpl(f: LtlFormula) {
            res.add(f)
            res.add(neg(f))
            when (f) {
                is Not -> closureImpl(f.sub)
                is And -> {
                    closureImpl(f.lhs)
                    closureImpl(f.rhs)
                }
                is Or -> {
                    closureImpl(f.lhs)
                    closureImpl(f.rhs)
                }
                is Next -> closureImpl(f.sub)
                is Future -> closureImpl(f.sub)
                is Globally -> closureImpl(f.sub)
                is Until -> {
                    closureImpl(f.lhs)
                    closureImpl(f.rhs)
                }
                is Release -> {
                    closureImpl(f.lhs)
                    closureImpl(f.rhs)
                }
            }
        }

        res.add(False)
        res.add(True)
        return res
    }
}

fun neg(f: LtlFormula): LtlFormula = when (f) {
    is False -> True
    is True -> False
    is Not -> f.sub
    else -> Not(f)
}

fun toNNF(formula: LtlFormula): LtlFormula {
    fun substituteFuture(f: Future) = Until(True, f.sub)
    fun substituteGlobally(f: Globally) = Release(False, f.sub)

    val result = when (formula) {
        is False, True -> formula
        is Variable -> formula

        is Not -> when (formula.sub) {
            is False -> True
            is True -> False
            is Variable -> formula

            is Not -> toNNF(formula.sub.sub)
            is And -> Or(toNNF(Not(formula.sub.lhs)), toNNF(Not(formula.sub.rhs)))
            is Or -> And(toNNF(Not(formula.sub.lhs)), toNNF(Not(formula.sub.rhs)))

            is Next -> Next(Not(formula.sub.sub))
            is Future -> toNNF(Not(substituteFuture(formula.sub)))
            is Globally -> toNNF(Not(substituteGlobally(formula.sub)))

            is Until -> Release(toNNF(Not(formula.sub.lhs)), toNNF(Not(formula.sub.rhs)))
            is Release -> Until(toNNF(Not(formula.sub.lhs)), toNNF(Not(formula.sub.rhs)))
        }

        is And -> And(toNNF(formula.lhs), toNNF(formula.rhs))
        is Or -> Or(toNNF(formula.lhs), toNNF(formula.rhs))

        is Next -> Next(toNNF(formula.sub))
        is Future -> toNNF(substituteFuture(formula))
        is Globally -> toNNF(substituteGlobally(formula))

        is Until -> Until(toNNF(formula.lhs), toNNF(formula.rhs))
        is Release -> Release(toNNF(formula.lhs), toNNF(formula.rhs))
    }

    assert(result.inNNF())

    return result
}