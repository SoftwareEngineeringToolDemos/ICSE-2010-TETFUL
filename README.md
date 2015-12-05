# ICSE-2010-Testful
This repository contains information related to the tool Testful presented in ICSE, 2010.
The tool was originally presented in <a href="http://dl.acm.org/citation.cfm?id=1810353&CFID=735785384&CFTOKEN=96328388">this paper.</a>

This repository _is not_ the original repository for this tool.Here are some links to the original project:
* <a href="https://code.google.com/p/testful/">The Official Project Page.</a>
* <a href="https://github.com/matteomiraz/testful">The Official Github Page.</a>
* <a href="https://code.google.com/p/testful/downloads/list">The Official Download Link.</a>


In this repository, for Testful you will find:
* :white_check_mark: <a href="https://code.google.com/p/testful/source/checkout">Source code (available).</a>
* :white_check_mark: <a href="https://code.google.com/p/testful/downloads/detail?name=testful-2.0.0.alpha.jar&can=2&q=">Executable tool (available).</a>
* :white_check_mark: <a href="https://drive.google.com/a/ncsu.edu/file/d/0B4H6x7rqcFw3R2Itc1dPem5QVWs/view">Virtual machine containing tool (available).</a>


Thanks to Mr.Matteo Miraz for helping me in establishing this repository. 

This repository was constructed by <a href="https://github.com/saileshbvk">Venkata Krishna Sailesh Bommisetti</a> under the supervision of <a href="https://github.com/CaptainEmerson">Dr.Emerson Murphy-Hill.</a>


# TestFul
*Testful* is an evolutionary testing framework for Java programs.
It is based on the idea of _search-based testing_, working both at class and method level. The former puts objects in useful states, used by the latter to exercise the uncovered parts of the class. Read more about the approach in [Matteo Miraz's publications](http://matteo.miraz.it/research/papers).

*Testful* is packaged into two forms
 * as command-line tools (usable for batch test generation, or simulations)
 * as an Eclipse Plugin: add the update site http://testful.sourceforge.net/updateSite/

## Open-source policy
*Testful* is a _research prototype_, released with an open-source licence. We use this prototype to empirically validate our proposal. Despite most of the other approaches, we publicly release Testful and we allow third-party researchers to compare their approaches against our. Additionally, we release Testful as an open-source software, allowing others to implement their ideas on top of our framework.
