grammar Ltl;

formula
    : '(' formula ')'              # parenthesis
    | '!' formula                  # negation
    | 'X' formula                  # next
    | 'F' formula                  # future
    | 'G' formula                  # globally
    | lhs=formula 'U' rhs=formula  # until
    | lhs=formula 'R' rhs=formula  # release
    | lhs=formula '&&' rhs=formula # conjunction
    | lhs=formula '||' rhs=formula # disjunction
    | lhs=formula '->' rhs=formula # implication
    | ID                           # variable
    | BooleanLiteral               # booleanLiteral
    ;

BooleanLiteral
    : 'true'
    | 'false'
    ;

NUM : [0-9]+;
ID  : [a-zA-Z][_a-zA-Z0-9]*;
WS  : [ \t\r\n]+ -> skip;