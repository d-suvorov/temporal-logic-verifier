package org.wotopul

class Answer(
    val holds: Boolean,
    val path: List<Symbol>? = null,
    val cycleStartIndex: Int? = null
)

fun findPath(fromAutomaton: BuchiAutomaton, fromLtl: BuchiAutomaton): Answer {
    // States on a current path of dfs1
    val path1 = mutableListOf<Pair<Node, Node>>()
    val pathSet1 = mutableSetOf<Pair<Node, Node>>()
    // Transitions on a current path of dfs1
    val transitions1 = mutableListOf<Symbol>()

    // States visited by dfs2
    val visited2 = mutableSetOf<Pair<Node, Node>>()
    // Transitions on a current path of dfs2
    val transitions2 = mutableListOf<Symbol>()

    var foundPath = false
    var path: List<Symbol>? = null
    var cycleStartIndex: Int? = null

    fun constructLoop(state: Node): Map<Symbol, List<Node>> =
        mapOf(Symbol(emptySet(), emptySet()) to listOf(state))

    // TODO cut'n'paste
    fun dfs2(q: Pair<Node, Node>) {
        if (foundPath)
            return
        visited2.add(q)
        val automatonTransitions = fromAutomaton.delta.getOrDefault(
            q.first, constructLoop(q.first))
        val ltlTransitions = fromLtl.delta.getOrDefault(
            q.second, constructLoop(q.second))
        for (autoTransitionLabel in automatonTransitions.keys) {
            for (ltlTransitionLabel in ltlTransitions.keys) {
                if (autoTransitionLabel subset ltlTransitionLabel) {
                    val autoNodes = automatonTransitions.getOrDefault(autoTransitionLabel, emptyList())
                    val ltlNodes = ltlTransitions.getOrDefault(ltlTransitionLabel, emptyList())
                    for (autoNode in autoNodes) {
                        for (ltlNode in ltlNodes) {
                            val to = Pair(autoNode, ltlNode)
                            if (to in pathSet1) {
                                foundPath = true
                                path = transitions1 + transitions2 + autoTransitionLabel
                                cycleStartIndex = path1.indexOf(to)
                                return
                            }
                            if (to !in visited2) {
                                transitions2.add(autoTransitionLabel)
                                dfs2(to)
                                transitions2.removeAt(transitions2.lastIndex)
                            }
                        }
                    }
                }
            }
        }
    }

    fun dfs1(q: Pair<Node, Node>) {
        if (foundPath)
            return
        pathSet1.add(q)
        path1.add(q)
        val automatonTransitions = fromAutomaton.delta.getOrDefault(
            q.first, constructLoop(q.first))
        val ltlTransitions = fromLtl.delta.getOrDefault(
            q.second, constructLoop(q.second))
        for (autoTransitionLabel in automatonTransitions.keys) {
            for (ltlTransitionLabel in ltlTransitions.keys) {
                if (autoTransitionLabel subset ltlTransitionLabel) {
                    val autoNodes = automatonTransitions.getOrDefault(autoTransitionLabel, emptyList())
                    val ltlNodes = ltlTransitions.getOrDefault(ltlTransitionLabel, emptyList())
                    for (autoNode in autoNodes) {
                        for (ltlNode in ltlNodes) {
                            val to = Pair(autoNode, ltlNode)
                            if (to !in pathSet1) {
                                transitions1.add(autoTransitionLabel)
                                dfs1(to)
                                transitions1.removeAt(transitions1.lastIndex)
                            }
                        }
                    }
                }
            }
        }
        if (q.first in fromAutomaton.finish && q.second in fromLtl.finish) {
            visited2.clear()
            dfs2(q)
        }
        path1.removeAt(path1.lastIndex)
        pathSet1.remove(q)
    }

    for (q1 in fromAutomaton.start) {
        for (q2 in fromLtl.start) {
            dfs1(Pair(q1, q2))
            if (foundPath)
                return Answer(false, path, cycleStartIndex)
        }
    }

    return Answer(true)
}