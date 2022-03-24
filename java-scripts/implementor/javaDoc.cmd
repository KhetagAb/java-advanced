
cd C:\Users\Khetag\Desktop\Java

SET implTest=.\java-advanced-2022\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor
SET impl=.\java-advanced\java-solutions\info\kgeorgiy\ja\dzestelov\implementor\Implementor.java
SET out=C:\Users\Khetag\Desktop\Java\java-advanced\out

javadoc -private %impl% %implTest%\JarImpler.java %implTest%\Impler.java %implTest%\ImplerException.java -d %out% 