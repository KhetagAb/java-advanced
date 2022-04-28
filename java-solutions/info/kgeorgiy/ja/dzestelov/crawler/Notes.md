commit 1106d47d117f3b46ff23c77292197cd5320bfc6e
Author: Khetag Dzestelov <dzkhetag@gmail.com>
Date:   Thu Apr 28 14:24:26 2022 +0300

    [HM-8] Fix
==================================================
Compiling 1 Java sources
Tests: running
WARNING: A command line option has enabled the Security Manager
WARNING: The Security Manager is deprecated and will be removed in a future release
Running class info.kgeorgiy.java.advanced.crawler.AdvancedCrawlerTest for info.kgeorgiy.ja.dzestelov.crawler.WebCrawler
=== Running test20_singleHost
=== Running test21_otherHost
=== Running test22_multiHosts
100 of 1444 pages downloaded, 0 error(s)
200 of 1444 pages downloaded, 5 error(s)
=== Running test23_allHosts
100 of 1444 pages downloaded, 3 error(s)
200 of 1444 pages downloaded, 5 error(s)
300 of 1444 pages downloaded, 5 error(s)
400 of 1444 pages downloaded, 10 error(s)
500 of 1444 pages downloaded, 23 error(s)
600 of 1444 pages downloaded, 28 error(s)
700 of 1444 pages downloaded, 41 error(s)
800 of 1444 pages downloaded, 82 error(s)
900 of 1444 pages downloaded, 114 error(s)
1000 of 1444 pages downloaded, 190 error(s)
1100 of 1444 pages downloaded, 228 error(s)
=== Running test24_megaHosts
=== Running test10_singleConnectionPerHost
100 of 3108 pages downloaded, 6 error(s)
200 of 3108 pages downloaded, 13 error(s)
=== Running test11_limitedConnectionsPerHost
101 of 3108 pages downloaded, 2 error(s)
200 of 3108 pages downloaded, 13 error(s)
=== Running test12_limitedConnectionsPerformance
100 of 3108 pages downloaded, 3 error(s)
200 of 3108 pages downloaded, 13 error(s)
Time: 1607
100 of 3108 pages downloaded, 1 error(s)
200 of 3108 pages downloaded, 13 error(s)
Time: 611
=== Running test01_singlePage
=== Running test02_pageAndLinks
100 of 3108 pages downloaded, 1 error(s)
200 of 3108 pages downloaded, 11 error(s)
=== Running test03_invalid
=== Running test04_deep
100 of 1444 pages downloaded, 2 error(s)
100 of 1444 pages downloaded, 1 error(s)
200 of 1444 pages downloaded, 4 error(s)
100 of 1444 pages downloaded, 1 error(s)
200 of 1444 pages downloaded, 3 error(s)
300 of 1444 pages downloaded, 5 error(s)
400 of 1444 pages downloaded, 7 error(s)
500 of 1444 pages downloaded, 12 error(s)
600 of 1444 pages downloaded, 21 error(s)
700 of 1444 pages downloaded, 27 error(s)
800 of 1444 pages downloaded, 32 error(s)
900 of 1444 pages downloaded, 39 error(s)
1000 of 1444 pages downloaded, 85 error(s)
1100 of 1444 pages downloaded, 91 error(s)
1200 of 1444 pages downloaded, 146 error(s)
1300 of 1444 pages downloaded, 214 error(s)
1400 of 1444 pages downloaded, 237 error(s)
=== Running test05_noLimits
100 of 524 pages downloaded, 4 error(s)
200 of 524 pages downloaded, 7 error(s)
300 of 524 pages downloaded, 26 error(s)
400 of 524 pages downloaded, 27 error(s)
500 of 524 pages downloaded, 37 error(s)
=== Running test06_limitDownloads
100 of 524 pages downloaded, 4 error(s)
200 of 524 pages downloaded, 15 error(s)
300 of 524 pages downloaded, 26 error(s)
400 of 524 pages downloaded, 27 error(s)
500 of 524 pages downloaded, 37 error(s)
=== Running test07_limitExtractors
100 of 524 pages downloaded, 4 error(s)
200 of 524 pages downloaded, 5 error(s)
300 of 524 pages downloaded, 14 error(s)
400 of 524 pages downloaded, 26 error(s)
500 of 524 pages downloaded, 28 error(s)
=== Running test08_limitBoth
100 of 524 pages downloaded, 5 error(s)
200 of 524 pages downloaded, 14 error(s)
300 of 524 pages downloaded, 26 error(s)
400 of 524 pages downloaded, 27 error(s)
500 of 524 pages downloaded, 37 error(s)
=== Running test09_performance
100 of 524 pages downloaded, 4 error(s)
200 of 524 pages downloaded, 4 error(s)
300 of 524 pages downloaded, 4 error(s)
400 of 524 pages downloaded, 5 error(s)
500 of 524 pages downloaded, 6 error(s)
Time: 6066
Test test24_megaHosts failed: Java heap space
java.lang.OutOfMemoryError: Java heap space
at java.base/java.util.HashMap.resize(HashMap.java:702)
at java.base/java.util.HashMap.putVal(HashMap.java:627)
at java.base/java.util.HashMap.put(HashMap.java:610)
at java.base/java.util.HashSet.add(HashSet.java:221)
at java.base/java.util.AbstractCollection.addAll(AbstractCollection.java:336)
at java.base/java.util.HashSet.<init>(HashSet.java:121)
at info.kgeorgiy.ja.dzestelov.crawler.WebCrawler.download(WebCrawler.java:95)
at info.kgeorgiy.java.advanced.crawler/info.kgeorgiy.java.advanced.crawler.AdvancedCrawlerTest.download(AdvancedCrawlerTest.java:80)
at info.kgeorgiy.java.advanced.crawler/info.kgeorgiy.java.advanced.crawler.AdvancedCrawlerTest.test(AdvancedCrawlerTest.java:67)
at info.kgeorgiy.java.advanced.crawler/info.kgeorgiy.java.advanced.crawler.AdvancedCrawlerTest.test24_megaHosts(AdvancedCrawlerTest.java:52)
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
ERROR: Tests: failed
