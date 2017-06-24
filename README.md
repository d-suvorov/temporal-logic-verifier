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

Операторы в порядке убывания приоритета: `!`, `X`, `F`, `G`, `U`, `R`, `&&`, `||`, `->`.
Приоритет операторов можно задавать явно с помощью скобок. [Antlr-граммматика.](https://github.com/wotopul/temporal-logic-verifier/blob/master/src/main/antlr4/org/wotopul/Ltl.g4)

# Формат автоматов

Смотри `tests/FSM/`

NB:

* Порядок объявления выходных воздействий важен: верификатор считает, что они выполняются в порядке объявления.
* Переменные не поддерживаются.

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

## Автомат `tests/simple/test0.xstd`

Автомат состоит из четырех состояний: A, B, C и D. Состояние A - стартовое.
Автомат может совершать следующие переходы по событию GO: A -> B, B -> A, C -> D, D -> C.
По событию JUMP автомат может совершить переход A -> C.

```
$ java -jar target/verifier.jar tests/simple/test0.xstd "G (JUMP -> (F C))"
Formula G (JUMP -> (F C)) holds
$ java -jar target/verifier.jar tests/simple/test0.xstd "G (JUMP -> (F D))"
Formula G (JUMP -> (F D)) holds
$ java -jar target/verifier.jar tests/simple/test0.xstd "G (A -> (F B))"
Formula G (A -> (F B)) does not hold
Counter-example path:
0 :	[A]
1 :	[A, GO]
2 :	[B]
3 :	[B, GO]
4 :	[A]
5 :	[A, JUMP]
6 :	[C]
7 :	[C, GO]
8 :	[D]
9 :	[D, GO]
Back to 6 and it's a cycle
$ java -jar target/verifier.jar tests/simple/test0.xstd "G (C -> (F (A || B)))"
Formula G (C -> (F (A || B))) does not hold
Counter-example path:
0 :	[A]
1 :	[A, JUMP]
2 :	[C]
3 :	[C, GO]
4 :	[D]
5 :	[D, GO]
6 :	[C]
7 :	[C, GO]
8 :	[D]
9 :	[D, GO]
10 :	[C]
Back to 7 and it's a cycle
```

# Автомат `Switcher` из ТЗ

```
$ java -jar target/verifier.jar tests/FSM/Switcher.xstd "G (PRESTART -> (PRESTART U POWER_ON))"
Formula G (PRESTART -> (PRESTART U POWER_ON)) holds
$ java -jar target/verifier.jar tests/FSM/Switcher.xstd "G (hal_init -> (F tim4_enable))"
Formula G (hal_init -> (F tim4_enable)) holds
$ java -jar target/verifier.jar tests/FSM/Switcher.xstd "G (F POWER_ON)"
Formula G (F POWER_ON) does not hold
Counter-example path:
0 :	[Start]
1 :	[Start, tick]
2 :	[Start, tick, hal_init]
3 :	[Start, tick, tim4_enable]
4 :	[PRESTART]
5 :	[PRESTART, tick]
6 :	[PRESTART, tick, shell_deinit]
7 :	[PRESTART, tick, bq_deinit]
8 :	[PRESTART, tick, pin_reset_s1]
9 :	[PRESTART, tick, pin_reset_s2]
10 :	[PRESTART, tick, pin_reset_s3]
11 :	[PRESTART, tick, delay_5000]
12 :	[POWER_ON]
13 :	[POWER_ON, CHG]
14 :	[CPU_ON]
15 :	[CPU_ON, CHG]
16 :	[BAT_ONLY]
17 :	[BAT_ONLY, CHG]
18 :	[CPU_ON]
19 :	[CPU_ON, CHG]
20 :	[BAT_ONLY]
21 :	[BAT_ONLY, CHG]
22 :	[CPU_ON]
Back to 19 and it's a cycle
```
