# ImageJ plugins by CMP-BIA group #

These plugins have their home on the [Fiji Wiki](http://fiji.sc/CMP-BIA_tools).

## CMP-BIA ##

We extend the ImageJ implementation by our java codes. This project mainly
contains implementation of our methods as  plugins in ImageJ (or Fiji)
and some other useful API for image segmentation and registration.

## Compilation using Maven ##

It is the actual way how to compile this project, we are using building tool Maven.
See [brief info](http://en.wikipedia.org/wiki/Apache_Maven) about [Maven](http://maven.apache.org/).
There are basically three ways how to run then Maven - compile, test, install

* compile - using command "mvn compile" compiles the source code
* test - using command "mvn test" runs all internal tests and show the results
* install - using command "mvn install" combines the compile + tests and also install the plugin into the ImageJ/Fiji instance. If you provide the location of your ImageJ/Fiji (e.g. via -Dimagej.app.directory=/path/to/my/ImageJ.app/), the plugins will be installed there, too.

Note: Download the [ImageJ](http://imagej.net/) or [Fiji](http://fiji.sc/Fiji) as appropriate for your operation system (e.g Linux 64bit).

Note2: the installation into plugin folder is done only it does not already exist so manual removing from plugin folder is needed

### Description of the `pom.xml` file ###

some important/interesting facts:
* the "artifactId" has to contain an underscore to be installed among plugins, otherwise it is installed into the `jars/` folder
* "SNAPSHOT" in version marks that the actual version is "in process"
* the "imagej.app.directory" property specifies the location of ImageJ/Fiji instance for an installation
* "dependencies" specifies the list of used libraries and their versions
* "parent" defines the project parent - org.scijava - where some other stuff is declared, e.g. how to install the plugin, etc.

### Local Maven Repos ###

We follow [this article](https://devcenter.heroku.com/articles/local-maven-dependencies) to provide dependencies from a project-local repository that are not available from any official Maven repositories.
In particular, we provide a current version of the [Java Library for Machine Learning](https://sourceforge.net/projects/jlml/). It was "deployed" using this command-line:

```bash
mvn deploy:deploy-file -Durl=file:$(pwd)/repo/ -Dfile=JML-2.8-JRE-1.6.jar -DgroupId=net.sourceforge -DartifactId=jml -Dpackaging=jar -Dversion=2.8-SNAPSHOT
```

The same command-line needs to be used to update the locally-deployed version to a new one.

## How to use ##

Brief introduction how to use this API you can find in README in `src/` folder

Eclipse for ImageJ
* http://imagejdocu.tudor.lu/doku.php?id=howto:plugins:the_imagej_eclipse_howto
* http://cmci.embl.de/documents/100825imagej_eclipse_debugging
* http://fiji.sc/wiki/index.php/Developing_Fiji_in_Eclipse
* http://fiji.sc/Maven
* ...

Profiling
* http://www.jvmmonitor.org/doc/

3party libraries
* [Commons Math](http://commons.apache.org/proper/commons-math)
* [Java Library for Machine Learning](http://sourceforge.net/projects/jlml)
* [MAchine Learning for LanguagE Toolkit](http://mallet.cs.umass.edu)

## General project structure ##

* `src/main/java/` - all our source codes and some external single java classes.
** plugins - only the runnable IJ plugins are here.
** classification - clustering and classification functions.
** optiomisation - optimisation algorithms such as L-BFGS.
** registration - set of registration functions mainly the ASSAR.
** segmentation - contains segmentation structures with own visualisation and some simple supplementary methods; there are also superpixel clustering methods (e.g. SLIC) and other segmentation methods.
** tools - some useful tools for type conversion, number generators, matrix tools, logging, etc.
** transform - transformation function for images such as wavelets.
* `src/test/java/` - validation tests using JUnit templates for code testing per parts; the internal structure is similar to the `main/` folder; they can be also seen as samples how to use individual functions.
* `docs/` - folder for some related documents.
* `target/` - folder into which Maven will write the compiled artifacts.

## ImageJ plugins ##

* SLIC superpixel segmentation (only 2D images)
* Unsupervised multi-class segmentation (only 2D images)
* Automatic Simultaneous Segmentation And fast Registration (ASSAR)

### SLIC superpixel segmentation ###

Basically we reimplemented the powerful Simple Linear Iterative Clustering (SLIC) method in Java respectively ImageJ.
Further we did some other improvements in clustering speed and precision.

#### Reference ####

Radhakrishna Achanta, Appu Shaji, Kevin Smith, et al.,
SLIC Superpixels Compared to State-of-the-art Superpixel Methods,
IEEE Transactions on Pattern Analysis and Machine Intelligence,
vol. 34, num. 11, p. 2274 - 2282, May 2012.


### ASSAR plugin ###

#### Abstract ####

We describe an automatic method for fast registration of images with
very different appearances. The images are jointly segmented into
a small number of classes, the segmented images are registered, and
the process is repeated.

The segmentation calculates feature vectors on superpixels and
then it finds a softmax classifier maximizing mutual information between class
labels in the two images. For speed, the registration considers a sparse set
of rectangular neighborhoods on the interfaces between
classes. A triangulation is created with spatial regularization handled
by pairwise spring-like terms on the edges. The optimal transformation
is found globally using loopy belief propagation. Multiresolution
helps to improve speed and robustness.

Our main application is registering stained histological slices, which
are large and differ both in the local and global appearance. We show
that our method has comparable accuracy to standard pixel-based
registration, while being faster and more general.

#### TODO ####

* implement image transformation
* implement registration
* merge all phases together
