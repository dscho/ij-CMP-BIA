# ImageJ plugins by CMP-BIA group #

These plugins have their home on the [Fiji Wiki](http://fiji.sc/CMP-BIA_tools).

## CMP-BIA ##

We extend the ImageJ implementation by our java codes. This project mainly 
contains implementation of our methods as  plugins in ImageJ (or Fiji) 
and some other useful API for image segmentation and registration.


## Compilation using Maven ##

It is the actual way how to compile this project, we are using building tool Maven.
See brief info about Maven - http://en.wikipedia.org/wiki/Apache_Maven , http://maven.apache.org/
There are basically three ways how to run then Maven - compile, test, install

* compile - using command "mvn compile" compiles the source code
* test - using command "mvn test" runs all internal tests and show the results
* install - using command "mvn install" combines the compile + tests and also install the plugin into the ImageJ/Fiji instance. Check the location of you ImageJ/Fiji in the pom.xml file (variable imagej.app.directory).

Note: Download the ImageJ (http://rsbweb.nih.gov/ij) or Fiji (http://fiji.sc/Fiji) and chose related source to you Operation system (e.g Linux 64bit) or the Platform  independent
Note2: the installation into plugin folder is done only it does not already exist so manual removing from plugin folder is needed

### Description of config file ###
some important/interesting fakts:
* the "artifactId" has to contain a '_' to be installed among plugins, otherwise it is installed in jar folder
* "SNAPSHOT" in version marks that the actual version is "in process"
* variable "imagej.app.directory" specify the location of ImageJ/Fiji instance for an installation
* "dependencies" specifies list of used libraries and its versions
* "parent" defines the project parent - org.scijava - vhere some other staff is declared, e.g. how to install plugin, etc.

### Local Maven Repos ###
https://devcenter.heroku.com/articles/local-maven-dependencies
example: 
Java Library for Machine Learning from https://sourceforge.net/projects/jlml/
-> mvn deploy:deploy-file -Durl=file:///datagrid/personal/borovec/Dropbox/Workspace/ij-CMP-BIA/repo/ -Dfile=JML.jar -DgroupId=net.sourceforge -DartifactId=jml -Dpackaging=jar -Dversion=2.7

## Previous Compilation using Ant ##

This is a remain after previous project structure where we used a building tool Ant. 
This compilation information are related to the the building file build.xml.OLD (to be compiled this file has to be renamed back to build.xml)
See brief info about Ant - http://en.wikipedia.org/wiki/Apache_Ant ,  http://ant.apache.org/

1) ImageJ from http://rsbweb.nih.gov/ij/download.html and extract it into the same folder as this project is.
* chose related source to you Operation system (e.g Linux 64bit) or the Platform  independent
* actually we are developing for version 1.47o
* alternative option can be downloading the Fiji (http://fiji.sc/Fiji) BUT in this case the path to ij.jar bas to be changed in the ImageJ-CMP-BIA/build.xml
2) Compile and install building plugins - enter the ImageJ-CMP-BIA folder and call 'ant'
* basically the Apache Ant is a software tool for automating software build processes (http://en.wikipedia.org/wiki/Apache_Ant)
* note, the library ij.jar has to be located in the root of downloaded ImageJ
* for any path changes, open the build.xml and edit it
* note we are currently developing under Sun JDK 1.6
3) Launch executable in the ImageJ folder

NOTE: because the project structure have been little changed, there is no guarante that this compilation is runnable 



## How to use ##

Brief introduction how to use this API you can find in README in 'src' folder 

Eclipse for ImageJ
* http://imagejdocu.tudor.lu/doku.php?id=howto:plugins:the_imagej_eclipse_howto
* http://cmci.embl.de/documents/100825imagej_eclipse_debugging
* http://fiji.sc/wiki/index.php/Developing_Fiji_in_Eclipse
* http://fiji.sc/Maven
* ...

Profilling
* http://www.jvmmonitor.org/doc/

3part libraries
* Commons Math - http://commons.apache.org/proper/commons-math
* Java Library for Machine Learning - http://sourceforge.net/projects/jlml
* MAchine Learning for LanguagE Toolkit - http://mallet.cs.umass.edu

## General project structure ##
* main - all our source codes and some external single java classes
** plugins - only the runnable IJ plugins are here
** classification - clustering and classification functions
** optiomisation - optimisation algorithms such as L-BFGS
** registration - set of registration functions mainly the ASSAR
** segmentation - contains segmentation structures with own visualisation and some simple supplementary methods; there are also superpixel clustering methods (e.g. SLIC) and other segmentation methods
** tools - some useful tools for type conversion, number generators, matrix tools, logging, etc.
** transform - transformation function for images such as wavelets
* test - validation tests using JUnit templates for code testing per parts; the internal structure is similar to main folder; they can be also seen as samples how to use individual functions
* docs - folder for some related documents
* libs - external libraries mainly already compiled *.jar for JDK 1.6 and later
* resources - resources usually for testing sections 

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
a~small number of classes, the segmented images are registered, and
the process is repeated.
The segmentation calculates feature vectors on superpixels and 
then it finds a~softmax classifier maximizing mutual information between class
labels in the two images. For speed, the registration considers a~sparse set
of rectangular neighborhoods on the interfaces between
classes. A~triangulation is created with spatial regularization handled
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
