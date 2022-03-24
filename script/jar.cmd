cd ..\..

SET pack=info\kgeorgiy\java\advanced\implementor
SET artifacts=.\java-advanced-2022\artifacts
SET impl=.\java-advanced\java-solutions\info\kgeorgiy\ja\dzestelov\implementor\Implementor.java
SET temp=.\java-advanced\script\temp

mkdir %temp%
javac -cp %artifacts%\* %impl% -d %temp%
cd %temp%
jar xf ..\..\..\%artifacts%\info.kgeorgiy.java.advanced.implementor.jar %pack%Impler.class %pack%JarImpler.class %pack%ImplerException.class
jar cfm ..\Implementor.jar ..\MANIFEST.MF .