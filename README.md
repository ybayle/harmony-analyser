# harmony-analyser
Cross-platform Java application for analysing harmonies and chord progressions of musical pieces or MIDI input.

Project website: [harmony-analyser.org](http://harmony-analyser.org)

## Installation
Pre-requisites:
* Running [JRE](https://www.java.com/en/download/) 8 or higher on your machine
* Download and install [Chordino and NNLS Chroma](http://www.isophonics.net/nnls-chroma) Vamp plugins 1.1 or higher (Linux: compile and place `nnls-chroma.so` to `usr/local/lib/vamp` folder)

To run the application:
* Download and open `harmony-analyser-<version>.jar` (where version is the currently available version) located in `target` folder, using your Java Virtual Machine (or invoke `java -jar out/harmony_analyser-<version>.jar` in command-line)

## Development
In addition to installation pre-requisites, a functional [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) needs to be installed in order to compile the source.
Project uses [Maven](https://maven.apache.org/) as the project management tool. Dependencies available in the public Maven repositories are pulled in automatically.
Project uses a local dependency in form of JAR located in `lib`:
* [jVamp](https://code.soundsoftware.ac.uk/projects/jvamp): Since the Vamp plugins are typically written in C++, we are using jVamp wrappers to load them in Java (jVamp uses JNI to work with native C++ code)

To install the dependency properly, issue from the project root:
`mvn install:install-file -DgroupId=org.vamp_plugins -DartifactId=jvamp -Dversion=1.3 -Dpackaging=jar -Dfile=./lib/jvamp.jar`

In addition to this, jVamp, Vamp Plugin SDK and libsndfile binaries need to be installed locally in order to build application. Download and install [jVamp](https://code.soundsoftware.ac.uk/projects/jvamp)
(Linux: compile and place `libvamp-jni.so` to your `java.library.path` folder (run `java -XshowSettings:properties` if you are unsure of the location), [Vamp Plugins SDK](https://code.soundsoftware.ac.uk/projects/vamp-plugin-sdk)
(Linux: Supported by package manager, or `./configure && make sdk && make install`) and [libsndfile](http://www.mega-nerd.com/libsndfile/) (Linux: Usually supported by package manager).

## Documentation
For more details and sample analysis please refer to the documentation located in `documentation` folder.
For any questions and comments please contact the author on GitHub or [mail](mailto: marsik@ksi.mff.cuni.cz).
