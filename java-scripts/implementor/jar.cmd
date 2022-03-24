
SET lib=..\..\..\java-advanced-2022\libs
SET implTest=..\..\..\java-advanced-2022\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\*.java
SET impl=..\..\..\java-advanced\java-solutions\info\kgeorgiy\ja\dzestelov\implementor\Implementor.java

mkdir temp

javac -cp %libs% %implTest%
