package org.wotopul

import org.wotopul.LtlFormula.*

open class Node(
    val name: String = "",
    val incoming: MutableSet<Node> = mutableSetOf(),
    val now: MutableSet<LtlFormula> = mutableSetOf(),
    val next: MutableSet<LtlFormula> = mutableSetOf()
)

class GeneralizedLabeledBuchiAutomaton(
    val states: List<Node>,
    val labels: Map<Node, Label>,
    val delta: Map<Node, List<Node>>,
    val start: List<Node>,
    val finish: List<Set<Node>>
)

fun GeneralizedLabeledBuchiAutomaton(ltlFormula: LtlFormula) =
    GeneralizedLabeledBuchiAutomatonImpl(toNNF(ltlFormula))

fun GeneralizedLabeledBuchiAutomatonImpl(ltlFormula: LtlFormula): GeneralizedLabeledBuchiAutomaton {
    fun curr1(f: LtlFormula): Set<LtlFormula> = when (f) {
        is Until -> setOf(f.lhs)
        is Release -> setOf(f.rhs)
        is Or -> setOf(f.rhs)
        else -> throw AssertionError("curr1 of $f is undefined")
    }

    fun next1(f: LtlFormula): Set<LtlFormula> = when (f) {
        is Until -> setOf(f)
        is Release -> setOf(f)
        is Or -> emptySet()
        else -> throw AssertionError("next1 of $f is undefined")
    }

    fun curr2(f: LtlFormula): Set<LtlFormula> = when (f) {
        is Until -> setOf(f.rhs)
        is Release -> setOf(f.lhs, f.rhs)
        is Or -> setOf(f.lhs)
        else -> throw AssertionError("curr2 of $f is undefined")
    }

    val init = Node()
    val nodes: MutableList<Node> = mutableListOf()

    fun expand(curr: Set<LtlFormula>, old: Set<LtlFormula>, next: Set<LtlFormula>, incoming: Set<Node>) {
        if (curr.isEmpty()) {
            val r = nodes.find { it.next == next && it.now == old }
            if (r != null) {
                r.incoming.addAll(incoming)
                return
            } else {
                val q = Node()
                nodes.add(q)
                q.incoming.addAll(incoming)
                q.now.addAll(old)
                q.next.addAll(next)
                expand(q.next, mutableSetOf(), emptySet(), setOf(q))
            }
        } else {
            val f: LtlFormula = curr.first()
            var nCurr: Set<LtlFormula> = curr - f
            val nOld: Set<LtlFormula> = old + f

            fun base(f: LtlFormula): Boolean {
                if (f is False || f is True)
                    return true
                if (f is Variable)
                    return true
                if (f is Not && f.sub is Variable)
                    return true
                return false
            }

            if (base(f)) {
                if (f is False || neg(f) in nOld)
                    return
                expand(nCurr, nOld, next, incoming)
            } else if (f is And) {
                nCurr += (setOf(f.lhs, f.rhs) - nOld)
                expand(nCurr, nOld, next, incoming)
            } else if (f is Next) {
                expand(nCurr, nOld, next + f.sub, incoming)
            } else if (f is Or || f is Until || f is Release) {
                val curr1 = nCurr + (curr1(f) - nOld)
                expand(curr1.toMutableSet(), nOld, next + next1(f), incoming)
                val curr2 = nCurr + (curr2(f) - nOld)
                expand(curr2.toMutableSet(), nOld, next, incoming)
            } else {
                throw AssertionError("not in negative normal form")
            }
        }
    }

    expand(mutableSetOf(ltlFormula), mutableSetOf(), emptySet(), setOf(init))

    return GeneralizedLabeledBuchiAutomaton(ltlFormula, nodes, init)
}

fun GeneralizedLabeledBuchiAutomaton(ltlFormula: LtlFormula, nodes: List<Node>, init: Node):
    GeneralizedLabeledBuchiAutomaton
{
    val states = nodes

    val atomPropositions = ltlFormula.atomPropositions()
    val labels = mutableMapOf<Node, Label>()
    for (node in nodes) {
        val min = node.now intersect atomPropositions
        val max = mutableSetOf<LtlFormula>()
        atomPropositions
            .filter { Not(it) !in node.now }
            .forEach { max.add(it) }
        labels[node] = Label(min, max)
    }

    val delta = mutableMapOf<Node, List<Node>>()
    for (node in nodes) {
        for (from in node.incoming) {
            delta.merge(from, listOf(node), { t, u -> t + u })
        }
    }

    val start = nodes.filter { init in it.incoming }

    val finish = mutableListOf<Set<Node>>()
    for (g in ltlFormula.closure()) {
        if (g !is Until)
            continue
        val Fg = nodes.filter { g.rhs in it.now || g !in it.now }.toSet()
        finish.add(Fg)
    }

    return GeneralizedLabeledBuchiAutomaton(states, labels, delta, start, finish)
}

class BuchiAutomaton(
    val sigma: Set<String>,
    val states: Set<Node>,
    val start: Set<Node>,
    val finish: Set<Node>,
    val delta: Map<Node, Map<Label, List<Node>>>
)

fun addTransition(delta: MutableMap<Node, MutableMap<Label, MutableList<Node>>>,
                  start: Node, label: Label, end: Node)
{
    val transitionsFromStart = delta.computeIfAbsent(start, { mutableMapOf() })
    val endNodes = transitionsFromStart.computeIfAbsent(label, { mutableListOf() })
    endNodes.add(end)
}

fun BuchiAutomaton(glba: GeneralizedLabeledBuchiAutomaton): BuchiAutomaton {
    class CountingNode(
        node: Node,
        val n: Int
    ) : Node(node.name, node.incoming, node.now, node.next)

    val sigma = mutableSetOf<String>()
    val nodes = mutableSetOf<Node>()
    val start = mutableSetOf<Node>()
    val finish = mutableSetOf<Node>()
    val delta = mutableMapOf<Node, MutableMap<Label, MutableList<Node>>>()

    for (node in glba.states) {
        for (i in 1 .. glba.finish.size) {
            val countingNode = CountingNode(node, i)
            nodes.add(countingNode)
            if (i == 1 && node in glba.start) {
                start.add(countingNode)
            }
            if (i == 1 && node in glba.finish[0]) {
                finish.add(countingNode)
            }
        }
    }

    for (state in glba.states) {
        val label = glba.labels[state]
            ?: throw AssertionError("No label for $state")
        val toList: List<Node> = glba.delta[state]
            ?: throw AssertionError("No transition for $state")

        for ((i, f) in glba.finish.withIndex()) {
            val fromCount = i + 1
            val toCount = if (state !in f) fromCount else
                (fromCount % glba.finish.size) + 1

            for (toNode in toList) {
                val from = CountingNode(state, fromCount)
                val to = CountingNode(toNode, toCount)
                addTransition(delta, from, label, to)
            }
        }
    }

    return BuchiAutomaton(sigma, nodes, start, finish, delta)
}

fun BuchiAutomaton(automaton: Automaton): BuchiAutomaton {
    // Construct variables
    val sigma: MutableSet<String> = mutableSetOf()
    with (sigma) {
        automaton.states
            .map { it.name }
            .forEach { add(it) }
        automaton.events
            .map { it.name }
            .forEach { add(it) }
        automaton.transitions
            .flatMap { it.actions }
            .forEach { add(it) }
    }

    val states = mutableSetOf<Node>()
    val start: MutableSet<Node> = mutableSetOf()

    // We split states to add transition on state name
    val nodesByStateId: MutableMap<Int, Pair<Node, Node>> = mutableMapOf()
    for (state in automaton.states) {
        val id = state.id
        val name = state.name
        val from = Node("${name}_enter")
        val to = Node(name)
        nodesByStateId[id] = Pair(from, to)
        states.add(from)
        states.add(to)
        if (state.type == 1)
            start.add(from)
    }

    val delta = mutableMapOf<Node, MutableMap<Label, MutableList<Node>>>()
    for ((enterState, state) in nodesByStateId.values) {
        addTransition(delta, enterState, Label(Variable(state.name)), state)
    }
    for (transition in automaton.transitions) {
        val automatonTransitionStart = automaton.states.find { transition.id in it.outgoing }
            ?: throw AssertionError("no start for $transition")
        val automatonTransitionEnd = automaton.states.find { transition.id in it.incoming }
            ?: throw AssertionError("no end for $transition")
        val from = nodesByStateId[automatonTransitionStart.id]!!.second
        val to = nodesByStateId[automatonTransitionEnd.id]!!.first

        var transitionStart = from
        val eventVariable = Variable(transition.event)
        for ((i, action) in transition.actions.withIndex()) {
            val actionVariable = Variable(action)
            val transitionEnd: Node
            if (i == transition.actions.lastIndex) {
                transitionEnd = to
            } else {
                transitionEnd = Node("temp_$action")
                states.add(transitionEnd)
            }
            val label = Label(setOf(
                Variable(from.name),
                eventVariable,
                actionVariable
            ))
            addTransition(delta, transitionStart, label, transitionEnd)
            transitionStart = transitionEnd
        }
    }

    return BuchiAutomaton(sigma, states, start, states, delta)
}
