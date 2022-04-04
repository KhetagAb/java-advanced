Прошло тесты 1 из 5 раз
commit ad2d800629af82ae2eb86d14138ac03a2867e44a
Author: Khetag <dzkhetag@gmail.com>
Date:   Sun Apr 3 18:23:17 2022 +0300

    [HM-6] Fix
==================================================
Compiling 1 Java sources
Tests: running
WARNING: A command line option has enabled the Security Manager
WARNING: The Security Manager is deprecated and will be removed in a future release
Running class info.kgeorgiy.java.advanced.concurrent.AdvancedIPTest for info.kgeorgiy.ja.dzestelov.concurrent.IterativeParallelism
=== Running test71_reduce
    --- Size 10000
        add, threads:  1 2 3 4 5 6 7 8 9 10
        mul, threads:  1 2 3 4 5 6 7 8 9 10
        min, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        add, threads:  1 2 3 4 5 6 7 8 9 10
        mul, threads:  1 2 3 4 5 6 7 8 9 10
        min, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        add, threads:  1 2 3 4 5 6 7 8 9 10
        mul, threads:  1 2 3 4 5 6 7 8 9 10
        min, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        add, threads:  1 2 3 4 5 6 7 8 9 10
        mul, threads:  1 2 3 4 5 6 7 8 9 10
        min, threads:  1 2 3 4 5 6 7 8 9 10

=== Running test72_mapReduce
    --- Size 10000
        *2-add, threads:  1 2 3 4 5 6 7 8 9 10
        +2-mul, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        *2-add, threads:  1 2 3 4 5 6 7 8 9 10
        +2-mul, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        *2-add, threads:  1 2 3 4 5 6 7 8 9 10
        +2-mul, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        *2-add, threads:  1 2 3 4 5 6 7 8 9 10
        +2-mul, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 10000
        toString-min, threads:  1 2 3 4 5 6 7 8 9 10
        toString-concat, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        toString-min, threads:  1 2 3 4 5 6 7 8 9 10
        toString-concat, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        toString-min, threads:  1 2 3 4 5 6 7 8 9 10
        toString-concat, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        toString-min, threads:  1 2 3 4 5 6 7 8 9 10
        toString-concat, threads:  1 2 3 4 5 6 7 8 9 10

=== Running test51_join
    --- Size 10000
        Common, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        Common, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        Common, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        Common, threads:  1 2 3 4 5 6 7 8 9 10

=== Running test52_filter
    --- Size 10000
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

=== Running test53_map
    --- Size 10000
        * 2, threads:  1 2 3 4 5 6 7 8 9 10
        is even, threads:  1 2 3 4 5 6 7 8 9 10
        toString, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        * 2, threads:  1 2 3 4 5 6 7 8 9 10
        is even, threads:  1 2 3 4 5 6 7 8 9 10
        toString, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        * 2, threads:  1 2 3 4 5 6 7 8 9 10
        is even, threads:  1 2 3 4 5 6 7 8 9 10
        toString, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        * 2, threads:  1 2 3 4 5 6 7 8 9 10
        is even, threads:  1 2 3 4 5 6 7 8 9 10
        toString, threads:  1 2 3 4 5 6 7 8 9 10

=== Running test54_mapMaximum
    --- Size 10000
        * 2, threads:  1 2 3 4 5 6 7 8 9 10
        is even, threads:  1 2 3 4 5 6 7 8 9 10
        toString, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        * 2, threads:  1 2 3 4 5 6 7 8 9 10
        is even, threads:  1 2 3 4 5 6 7 8 9 10
        toString, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        * 2, threads:  1 2 3 4 5 6 7 8 9 10
        is even, threads:  1 2 3 4 5 6 7 8 9 10
        toString, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        * 2, threads:  1 2 3 4 5 6 7 8 9 10
        is even, threads:  1 2 3 4 5 6 7 8 9 10
        toString, threads:  1 2 3 4 5 6 7 8 9 10

=== Running test01_maximum
    --- Size 10000
        Natural order, threads:  1 2 3 4 5 6 7 8 9 10
        Reverse order, threads:  1 2 3 4 5 6 7 8 9 10
        Div 100, threads:  1 2 3 4 5 6 7 8 9 10
        Even first, threads:  1 2 3 4 5 6 7 8 9 10
        All equal, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        Natural order, threads:  1 2 3 4 5 6 7 8 9 10
        Reverse order, threads:  1 2 3 4 5 6 7 8 9 10
        Div 100, threads:  1 2 3 4 5 6 7 8 9 10
        Even first, threads:  1 2 3 4 5 6 7 8 9 10
        All equal, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        Natural order, threads:  1 2 3 4 5 6 7 8 9 10
        Reverse order, threads:  1 2 3 4 5 6 7 8 9 10
        Div 100, threads:  1 2 3 4 5 6 7 8 9 10
        Even first, threads:  1 2 3 4 5 6 7 8 9 10
        All equal, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        Natural order, threads:  1 2 3 4 5 6 7 8 9 10
        Reverse order, threads:  1 2 3 4 5 6 7 8 9 10
        Div 100, threads:  1 2 3 4 5 6 7 8 9 10
        Even first, threads:  1 2 3 4 5 6 7 8 9 10
        All equal, threads:  1 2 3 4 5 6 7 8 9 10

=== Running test02_minimum
    --- Size 10000
        Natural order, threads:  1 2 3 4 5 6 7 8 9 10
        Reverse order, threads:  1 2 3 4 5 6 7 8 9 10
        Div 100, threads:  1 2 3 4 5 6 7 8 9 10
        Even first, threads:  1 2 3 4 5 6 7 8 9 10
        All equal, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        Natural order, threads:  1 2 3 4 5 6 7 8 9 10
        Reverse order, threads:  1 2 3 4 5 6 7 8 9 10
        Div 100, threads:  1 2 3 4 5 6 7 8 9 10
        Even first, threads:  1 2 3 4 5 6 7 8 9 10
        All equal, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        Natural order, threads:  1 2 3 4 5 6 7 8 9 10
        Reverse order, threads:  1 2 3 4 5 6 7 8 9 10
        Div 100, threads:  1 2 3 4 5 6 7 8 9 10
        Even first, threads:  1 2 3 4 5 6 7 8 9 10
        All equal, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        Natural order, threads:  1 2 3 4 5 6 7 8 9 10
        Reverse order, threads:  1 2 3 4 5 6 7 8 9 10
        Div 100, threads:  1 2 3 4 5 6 7 8 9 10
        Even first, threads:  1 2 3 4 5 6 7 8 9 10
        All equal, threads:  1 2 3 4 5 6 7 8 9 10

=== Running test03_all
    --- Size 10000
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

=== Running test04_any
    --- Size 10000
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 5
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 2
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

    --- Size 1
        Equal 0, threads:  1 2 3 4 5 6 7 8 9 10
        Greater than 0, threads:  1 2 3 4 5 6 7 8 9 10
        Even, threads:  1 2 3 4 5 6 7 8 9 10
        True, threads:  1 2 3 4 5 6 7 8 9 10
        False, threads:  1 2 3 4 5 6 7 8 9 10

=== Running test05_sleepPerformance
    Warm up
    Measurement
    Performance ratio 1,0 for 10 threads (108,9 108,8 ms/op)
=== Running test06_burnPerformance
    Warm up
    Measurement
    Performance ratio 1,5 for 8 threads (2,5 3,8 ms/op)
Test test06_burnPerformance failed: Upper bound hit
java.lang.AssertionError: Upper bound hit
	at junit@4.11/org.junit.Assert.fail(Assert.java:88)
	at junit@4.11/org.junit.Assert.assertTrue(Assert.java:41)
	at info.kgeorgiy.java.advanced.concurrent/info.kgeorgiy.java.advanced.concurrent.ScalarIPTest.testPerformance(ScalarIPTest.java:81)
	at info.kgeorgiy.java.advanced.concurrent/info.kgeorgiy.java.advanced.concurrent.ScalarIPTest.test06_burnPerformance(ScalarIPTest.java:75)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:568)
	at junit@4.11/org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)
	at junit@4.11/org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at junit@4.11/org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:44)
	at junit@4.11/org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at junit@4.11/org.junit.rules.TestWatcher$1.evaluate(TestWatcher.java:55)
	at junit@4.11/org.junit.rules.RunRules.evaluate(RunRules.java:20)
	at junit@4.11/org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)
	at junit@4.11/org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)
	at junit@4.11/org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
	at junit@4.11/org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
	at junit@4.11/org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
	at junit@4.11/org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
	at junit@4.11/org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
	at junit@4.11/org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
	at junit@4.11/org.junit.runners.ParentRunner.run(ParentRunner.java:309)
	at junit@4.11/org.junit.runners.Suite.runChild(Suite.java:127)
	at junit@4.11/org.junit.runners.Suite.runChild(Suite.java:26)
	at junit@4.11/org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
	at junit@4.11/org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
	at junit@4.11/org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
	at junit@4.11/org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
	at junit@4.11/org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
	at junit@4.11/org.junit.runners.ParentRunner.run(ParentRunner.java:309)
	at junit@4.11/org.junit.runner.JUnitCore.run(JUnitCore.java:160)
	at junit@4.11/org.junit.runner.JUnitCore.run(JUnitCore.java:138)
	at junit@4.11/org.junit.runner.JUnitCore.run(JUnitCore.java:117)
	at info.kgeorgiy.java.advanced.base/info.kgeorgiy.java.advanced.base.BaseTester.test(BaseTester.java:55)
	at info.kgeorgiy.java.advanced.base/info.kgeorgiy.java.advanced.base.BaseTester.lambda$add$0(BaseTester.java:95)
	at info.kgeorgiy.java.advanced.base/info.kgeorgiy.java.advanced.base.BaseTester.test(BaseTester.java:48)
	at info.kgeorgiy.java.advanced.base/info.kgeorgiy.java.advanced.base.BaseTester.run(BaseTester.java:39)
	at info.kgeorgiy.java.advanced.concurrent/info.kgeorgiy.java.advanced.concurrent.Tester.main(Tester.java:17)
ERROR: Tests: failed
