# jndi-evaluator

A JNDI validation tool which 

1. Checks duplicate entries in JNDI

2. Reports differences between JNDI configuration files

3. Provides security analysis in JNDI configuration – password change.

Depending on what feature you would like to use, start script with one of the following commands:

1. jndi-evaluator/bin/JndiEvaluator.sh --dup a.xml

2. jndi-evaluator/bin/JndiEvaluator.sh --diff a.xml b.xml

3. jndi-evaluator/bin/JndiEvaluator.sh --sec a.xml a-secure.xml sec.txt

a.xml – input xml

a-secure.xml – output xml

sec.txt – contains key(s) of JNDI properties to be replaced

Note: Additional windows script was developed, but make sure you have Java 8 installed.

