package org.wotopul

class Answer(val holds: Boolean, val path: List<Label>?)

fun findPath(fromAutomaton: BuchiAutomaton, fromLtl: BuchiAutomaton): Answer {
    // States on a current path of dfs1
    val pathSet1 = mutableSetOf<Pair<Node, Node>>()
    // Transitions on a current path of dfs1
    val transitions1 = mutableListOf<Label>()

    // States visited by dfs2
    val visited2 = mutableSetOf<Pair<Node, Node>>()
    // Transitions on a current path of dfs2
    val transitions2 = mutableListOf<Label>()

    var foundPath = false
    var path: List<Label>? = null

    // TODO cut'n'paste
    fun dfs2(q: Pair<Node, Node>) {
        if (foundPath)
            return
        visited2.add(q)
        val automatonTransitions = fromAutomaton.delta.getOrDefault(q.first, emptyMap())
        val ltlTransitions = fromLtl.delta.getOrDefault(q.second, emptyMap())
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
        val automatonTransitions = fromAutomaton.delta.getOrDefault(q.first, emptyMap())
        val ltlTransitions = fromLtl.delta.getOrDefault(q.second, emptyMap())
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
            dfs2(q)
        }
        pathSet1.remove(q)
    }

    for (q1 in fromAutomaton.start) {
        for (q2 in fromLtl.start) {
            dfs1(Pair(q1, q2))
            if (foundPath)
                return Answer(false, path)
        }
    }

    return Answer(true, null)
}