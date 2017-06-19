# Верификатор LTL

Верификатор формул LTL-логики. Исходные модели задаются с помощью автоматов Харела.

# Сборка

Для сборки используется Apache Maven:

```
mvn clean install
```

# Формат формул

В LTL-формулах поддерживаются следующие логические связки:
* `!` - логическое отрицание,
* `||` - логическое ИЛИ,
* `&&` - логическое И,
* `->` - следование;

и следующие темпоральные операторы:
* `X` - neXt,
* `F` - Future,
* `G` - Globally,
* `U` - Until,
* `R` - Release.

Приоритет операторов нужно задавать явно с помощью скобок. [Antlr-граммматика.](https://github.com/wotopul/temporal-logic-verifier/blob/master/src/main/antlr4/org/wotopul/Ltl.g4)

# Формат автоматов

Смотри `tests/FSM/`

# Использование и формат аргументов командной строки

Главный артефакт - `verifier.jar`.

```
$ java -jar target/verifier.jar --help
Usage: java -jar verifier.jar <automaton> <formula> [<debug-mode>]
Where:
 <automaton> - path to a an XML-file containing automaton definition
 <formula>   - LTL-formula
```

# Примеры использования

TBD
