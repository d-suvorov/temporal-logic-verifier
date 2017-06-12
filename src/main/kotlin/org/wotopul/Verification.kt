package org.wotopul

class Answer(val holds: Boolean)

fun findPath(fromAutomaton: BuchiAutomaton, fromLtl: BuchiAutomaton): Answer {
    val used1 = mutableSetOf<Pair<Node, Node>>()
    val used2 = mutableSetOf<Pair<Node, Node>>()

    var foundPath = false

    // TODO cut'n'paste
    fun dfs2(q: Pair<Node, Node>) {
        if (foundPath)
            return
        used2.add(q)
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
                            if (to in used1) {
                                foundPath = true
                                return
                            }
                            if (to !in used2) {
                                dfs2(to)
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
        used1.add(q)
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
                            if (to !in used1) {
                                dfs1(to)
                            }
                        }
                    }
                }
            }
        }
        if (q.first in fromAutomaton.finish && q.second in fromLtl.finish) {
            dfs2(q)
        }
    }

    for (q1 in fromAutomaton.start) {
        for (q2 in fromLtl.start) {
            dfs1(Pair(q1, q2))
            if (foundPath)
                return Answer(true)
        }
    }

    return Answer(false)
}