[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/Microsoft/mssql-jdbc/master/LICENSE)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.microsoft.sqlserver/mssql-jdbc/badge.svg)](http://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc)
[![codecov.io](http://codecov.io/github/Microsoft/mssql-jdbc/coverage.svg?branch=master)](http://codecov.io/github/Microsoft/mssql-jdbc?branch=master)
[![Javadocs](http://javadoc.io/badge/com.microsoft.sqlserver/mssql-jdbc.svg)](http://javadoc.io/doc/com.microsoft.sqlserver/mssql-jdbc)
[![Gitter](https://img.shields.io/gitter/room/badges/shields.svg)](https://gitter.im/Microsoft/mssql-developers)
</br>
# Microsoft JDBC Driver for SQL Server

Welcome to the Microsoft JDBC Driver for SQL Server project!

The Microsoft JDBC Driver for SQL Server is a Type 4 JDBC driver that provides database connectivity through the standard JDBC application program interfaces (APIs) available in the Java Platform, Enterprise Editions. The Driver provides access to Microsoft SQL Server and Azure SQL Database from any Java application, application server, or Java-enabled applet.

We hope you enjoy using the Microsoft JDBC Driver for SQL Server.

SQL Server Team

## Take our survey

Let us know how you think we're doing.

<a href="https://aka.ms/mssqljdbcsurvey"><img style="float: right;"  height="67" width="156" src="https://meetsstorenew.blob.core.windows.net/contianerhd/survey.png?st=2017-02-17T22%3A03%3A00Z&se=2100-02-18T22%3A03%3A00Z&sp=rl&sv=2015-12-11&sr=b&sig=DJSFoihBptSvO%2BjvWzwpHecf8o5yfAbJoD2qW5oB8tc%3D"></a>

## Status of Most Recent Builds
| AppVeyor (Windows)       | Travis CI (Linux) |
|--------------------------|--------------------------|
| [![AppVeyor ](https://ci.appveyor.com/api/projects/status/o6fjg16678ol64d3?svg=true "Windows")](https://ci.appveyor.com/project/Microsoft-JDBC/mssql-jdbc) | [![Travis CI](https://travis-ci.org/Microsoft/mssql-jdbc.svg? "Linux")](https://travis-ci.org/Microsoft/mssql-jdbc ) |vg? "Linux"

## Announcements
What's coming next?  We will look into adding a more comprehensive set of tests, improving our javadocs, and start developing the next set of features.

## Get Started 
* [**Ubuntu + SQL Server + Java**](https://www.microsoft.com/en-us/sql-server/developer-get-started/java/ubuntu) 
* [**Red Hat + SQL Server + Java**](https://www.microsoft.com/en-us/sql-server/developer-get-started/java/rhel)
* [**Mac + SQL Server + Java**](https://www.microsoft.com/en-us/sql-server/developer-get-started/java/mac)
* [**Windows + SQL Server + Java**](https://www.microsoft.com/en-us/sql-server/developer-get-started/java/windows)

## Build
### Prerequisites
* Java 8
* [Maven](http://maven.apache.org/download.cgi)
* An instance of SQL Server or Azure SQL Database that you can connect to. 

### Build the JAR files
Maven builds automatically trigger a set of verification tests to run.  For these tests to pass, you will first need to add an environment variable in your system called `mssql_jdbc_test_connection_properties` to provide the [correct connection properties](https://msdn.microsoft.com/en-us/library/ms378428(v=sql.110).aspx) for your SQL Server or Azure SQL Database instance.

To build the jar files, you must use Java 8 with Maven.  You can choose to build a JDBC 4.1 compliant jar file (for use with JRE 7) and/or a JDBC 4.2 compliant jar file (for use with JRE 8).

* Maven:
	1. If you have not already done so, add the environment variable `mssql_jdbc_test_connection_properties` in your system with the connection properties for your SQL Server or SQL DB instance.
	2. Run one of the commands below to build a JDBC 4.1 compliant jar or JDBC 4.2 compliant jar in the \target directory. 
    	* Run `mvn install -Pbuild41`. This creates JDBC 4.1 compliant jar in \target directory
    	* Run `mvn install -Pbuild42`. This creates JDBC 4.2 compliant jar in \target directory

**NOTE**: Beginning release v6.1.7, we will no longer be maintaining the existing [Gradle build script](build.gradle) and it will be left in the repository for reference. Please refer to issue [#62](https://github.com/Microsoft/mssql-jdbc/issues/62) for this decision.

* Gradle:
	1. If you have not already done so, add the environment variable `mssql_jdbc_test_connection_properties` in your system with the connection properties for your SQL Server or SQL DB instance.
	2. Run one of the commands below to build a JDBC 4.1 compliant jar or JDBC 4.2 compliant jar in the \build\libs directory. 
    	* Run `gradle build -Pbuild=build41`. This creates JDBC 4.1 compliant jar in \build\libs directory
    	* Run `gradle build -Pbuild=build42`. This creates JDBC 4.2 compliant jar in \build\libs directory

## Resources

### Documentation
API reference documentation is available in [Javadocs](https://aka.ms/jdbcjavadocs).

This driver is documented on [Microsoft's Documentation web site](https://msdn.microsoft.com/en-us/library/mt720657).

### Sample Code
For samples, please see the src\sample directory.

### Download the DLLs
For some features (e.g. Integrated Authentication and Distributed Transactions), you may need to use the `sqljdbc_xa` and `sqljdbc_auth` DLLs. They can be downloaded from the [Microsoft Download Center](https://go.microsoft.com/fwlink/?linkid=852460)

### Download the driver
Don't want to compile anything?

We're now on the Maven Central Repository. Add the following to your POM file to get the most stable release:
```xml
<dependency>
	<groupId>com.microsoft.sqlserver</groupId>
	<artifactId>mssql-jdbc</artifactId>
	<version>6.2.1.jre8</version>
</dependency>
```
The driver can be downloaded from the [Microsoft Download Center](https://go.microsoft.com/fwlink/?linkid=852460).

To get the latest preview version of the driver, add the following to your POM file: 
```xml
<dependency>
	<groupId>com.microsoft.sqlserver</groupId>
	<artifactId>mssql-jdbc</artifactId>
	<version>6.3.2.jre8-preview</version>
</dependency>
```



## Dependencies
This project has following dependencies: 

Compile Time:
 - `azure-keyvault` : Azure Key Vault Provider for Always Encrypted feature (optional)
 - `adal4j` : Azure ActiveDirectory Library for Java for Azure Active Directory Authentication feature (optional)

Test Time:
 - `junit:jar`   : For Unit Test cases.

### Dependency Tree
One can see all dependencies including Transitive Dependency by executing following command.
``` 
mvn dependency:tree
```

### Azure Key Vault and Azure Active Directory Authentication Dependencies
Projects that require either of the two features need to explicitly declare the dependency in their pom file.

***For Example:*** If you are using *Azure Key Vault feature* then you need to redeclare *azure-keyvault* dependency in your project's pom file. Please see the following snippet: 
```xml
<dependency>
	<groupId>com.microsoft.sqlserver</groupId>
	<artifactId>mssql-jdbc</artifactId>
	<version>6.3.2.jre8-preview</version>
	<scope>compile</scope>
</dependency>

<dependency>
	<groupId>com.microsoft.azure</groupId>
	<artifactId>azure-keyvault</artifactId>
	<version>1.0.0</version>
</dependency>
```
***Please note*** as of the v6.3.0-preview, the way to construct a `SQLServerColumnEncryptionAzureKeyVaultProvider` object has changed. Please refer to this [Wiki](https://github.com/Microsoft/mssql-jdbc/wiki/New-Constructor-Definition-for-SQLServerColumnEncryptionAzureKeyVaultProvider-after-6.3.0-Preview-Release) page for more information.

## Guidelines for Creating Pull Requests
We love contributions from the community.  To help improve the quality of our code, we encourage you to use the mssql-jdbc_formatter.xml formatter provided on all pull requests.

Thank you!

## Guidelines for Reporting Issues
We appreciate you taking the time to test the driver, provide feedback and report any issues.  It would be extremely helpful if you:

- Report each issue as a new issue (but check first if it's already been reported)
- Try to be detailed in your report. Useful information for good bug reports include:
  * What you are seeing and what the expected behaviour is
  * Which jar file?
  * Environment details: e.g. Java version, client operating system?
  * Table schema (for some issues the data types make a big difference!)
  * Any other relevant information you want to share
- Try to include a Java sample demonstrating the isolated problem.

Thank you!

### Reporting security issues and security bugs
Security issues and bugs should be reported privately, via email, to the Microsoft Security Response Center (MSRC) [secure@microsoft.com](mailto:secure@microsoft.com). You should receive a response within 24 hours. If for some reason you do not, please follow up via email to ensure we received your original message. Further information, including the MSRC PGP key, can be found in the [Security TechCenter](https://technet.microsoft.com/en-us/security/ff852094.aspx).

## Contributors 
Special thanks to everyone who has contributed to the project.

Up-to-date list of contributors: https://github.com/Microsoft/mssql-jdbc/graphs/contributors

- marschall (Philippe Marschall)
- pierresouchay (Pierre Souchay)
- gordthompson (Gord Thompson)
- gstojsic
- cosmofrit
- JamieMagee (Jamie Magee)
- mfriesen (Mike Friesen)
- tonytamwk
- sehrope (Sehrope Sarkuni)
- jacobovazquez
- brettwooldridge (Brett Wooldridge)  

## License
The Microsoft JDBC Driver for SQL Server is licensed under the MIT license. See the [LICENSE](https://github.com/Microsoft/mssql-jdbc/blob/master/LICENSE) file for more details.

## Code of conduct
This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

