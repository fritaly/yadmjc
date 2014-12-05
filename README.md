yadmjc
======

[![Build Status](https://travis-ci.org/fritaly/yadmjc.svg?branch=master)](https://travis-ci.org/fritaly/yadmjc)

"Yet Another Dungeon Master Java Clone" is  Java clone of the famous RPG game "Dungeon Master".

Contrary to existing clones, I didn't write this project to fully reimplement the original game, Alan Berfield successfully did that (in Java) some years ago. I've always wanted to develop a video game (being myself a hard core gamer, an old fan of Dungeon Master and a J2EE developer in my professional career).

In 2003, I downloaded the source files for the "Dungeon Master for Java" clone by Alan Berfield and was quite disappointed by the source code. The source was a perfect example of how NOT to develop in Java (Alan was aware of that fact when he released the source files for he wrote a comment reading (in my own words) "Don't bug me with the source quality, I know it's far from being perfect, at the time I was learning Java, etc").

Then I saw the opportunity to reimplement the game "the right way" to improve my OO modeling skills. Short story long, I now realize that reimplementing the whole game is out of my grasp, I simply don't have enough free time and there are way too many features / rules inside the games that aren't documented (Note: No, I don't want to retro-engineer Paul Stevens' Chaos Strikes Back for Windows C/C++ code) to complete this project.

Since I've implemented a decent amount of the game rules, I post the source files on GitHub for anyone interested in it and also to improve my skills on Git :)

How to build
=====

This project needs Gradle 1.8 to build. It's preconfigured to use the "Gradle wrapper", a nice feature that will automatically install Gradle for you.

To build the project, simply issue a "gradlew clean build" (Unix / OSX) or a "gradlew.bat clean build" (Windows).

To mount the project in Eclipse, first generate the Eclipse files (.project, .classpath and .settings) with "gradlew eclipse" then import the projects into Eclipse.

Credits
=====

Credits go to Nerthing for his [Dungeon Master Guide](http://www.gamefaqs.com/snes/588299-dungeon-master/faqs/33244) and Christophe Fontanel for his [technical documentation](http://dmweb.free.fr/?q=taxonomy/term/39) about Dungeon Master.
