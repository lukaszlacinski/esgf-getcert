##esgf-getcert

###General Installation Instructions

#### Pre-requisite software

* Java SDK 7
* Maven

####Build
Run

`mvn clean install`

to get two jar archives in `target/`:
* `getcert-<version>.jar` that includes only the esg.security.myproxy package
* `getcert.jar` that also includes all dependency jars.

####Usage
`java -jar getcert.jar -help`
