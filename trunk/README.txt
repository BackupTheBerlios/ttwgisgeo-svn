           TOC
1. Prerequisites
2. Checkout the project from SVN repository
3. Add the necessary libraries
4. Compiling & running the tests
4.1 Modify the build.xml
4.2 Compile the Adapter and Codec

1. Prerequisites
To execute the test cases the following are necessary:
  a. TTWorkbench Professional v. 1.1.4
  b. HttpUnit 1.7
  c. dom4j 1.6.1

2. Checkout the project from SVN repository
Check out from the SVN repository the project files into a new TTCN-3 project. The URLs that can be used are:
svn+ssh://svn.berlios.de/svnroot/repos/ttwgisgeo/trunk
or
https://loginname@svn.berlios.de/svnroot/repos/ttwgisgeo/trunk

In the qualipso.ttworkgisgeo.tri.Adapter class set the URLs for the web interface for GISClient and the web services interface of GEOServer:
private final String GEOSERVER_URL = "http://150.254.173.202:8090/geoserver/wfs";
private final String GISCLIENT_URL = "http://syros.eurodyn.com:18088/GISClient3/";

3. Add the necessary libraries
In the root directory of the project create a lib folder. 
The Adapter and CODEC use HttpUnit 1.7 and dom4j 1.6.1. 
From these projects the following jar files must be added to the lib folder:
From HttpUnit:
  httpunit.jar
  xercesImpl-2.6.1.jar
  xmlParserAPIs-2.6.1.jar
  activation-1.1.jar
  js-1.6R5.jar;
  jtidy-4aug2000r7-dev.jar
  junit-3.8.1.jar
  mail-1.4.jar
  servlet-api-2.4.jar
From dom4j:
  dom4j-1.6.1.jar
  jaxen-1.1-beta-6.jar 

4. Compiling & running the tests
4.1 Modify the build.xml
TTWorkbench automatically generates a build.xml file. In order for the Adapter to compile and run properly some modifications must be made:
a. In target "init" remove the task that creates the lib directory

b. In target "classes" add the following to the classpath of the javac task
	<pathelement location="${lib}/httpunit.jar"/>
	<pathelement location="${lib}/dom4j-1.6.1.jar"/>        

c. In the "init task add the following property (the names of the additional jars separated by a space):
	<!-- HttpUnit Dependencies -->    	
    <property name="additionalLibraries" value="dom4j-1.6.1.jar httpunit.jar xercesImpl-2.6.1.jar xmlParserAPIs-2.6.1.jar activation-1.1.jar js-1.6R5.jar jtidy-4aug2000r7-dev.jar junit-3.8.1.jar mail-1.4.jar servlet-api-2.4.jar jaxen-1.1-beta-6.jar"/>

d. In the "TA.jar" target modify the jar task by adding a manifest like this:
<jar jarfile="${TA}" basedir="${classes}" includes="**/*.class">
	<manifest>
		<attribute name="Class-Path" value="${additionalLibraries}"/>
	</manifest>
 </jar>
 
e. Add the following properties to target "init". These properties are used to generate the JavaDoc. If you use a proxy to access the internet modify accordingly. 

   	<!-- folder for JavaDoc -->
	<property name="javadoc.outdir" location="javadoc"/>
	<!-- use proxy for creating JavaDoc? -->
	<property name="proxy.use" value="false"/>
	<!-- proxy properties -->
	<property name="proxy.host" value="example.proxy"/>
	<property name="proxy.port" value="8080"/>
	<!-- Is Java package list available offline? where is it? -->
	<property name="java.packageList.offline" value="false"/>
	<property name="java.packageList.location" value="http://java.sun.com/j2se/1.5.0/docs/api/package-list"/>
	<!-- Is HttpUnit package list available online? where is it? -->
	<property name="httpunit.packageList.offline" value="false"/>
	<property name="httpunit.packageList.location" value="http://httpunit.sourceforge.net/doc/api/package-list"/>
	<!-- Is dom4j package list available online? where is it? -->
	<property name="dom4j.packageList.offline" value="false"/> 
	<property name="dom4j.packageList.location" value="http://www.dom4j.org/dom4j-1.6.1/apidocs/package-list"/>
 
f. Add a target "doc" in order to create the JavaDoc:
	<target name="doc" depends="init">
	   <echo>Creating Javadoc from ${classes} to ${javadoc.outdir}</echo>
	   <mkdir dir="${javadoc.outdir}" />
	   <!-- javadoc.additionalparam.proxy -->
	   <condition property="javadoc.additionalparam.proxy" value="-J-DproxySet=${proxy.use} -J-DproxyHost=${proxy.host} -J-DproxyPort=${proxy.port}">
	     <equals arg1="${proxy.use}" arg2="true" />
	   </condition>
	   <condition property="javadoc.additionalparam.proxy" value="">
	     <not>
	       <isset property="javadoc.additionalparam.proxy" />
	     </not>
	   </condition>
	   <echo>using additionalparam.proxy: ${javadoc.additionalparam.proxy}</echo>
	   
		<!--Create Javadoc-->
	   <javadoc sourcepath="${src}" classpath="${classes}" packagenames="qualipso.*" destdir="${javadoc.outdir}" version="true" author="true" windowtitle="OpenTTCN Adapter and CODEC for GISClient-GEOServer Testbed" additionalparam="-J-Xmx32m ${javadoc.additionalparam.proxy}">
	     <doctitle>OpenTTCN Adapter and CODEC for GISClient-GEOServer Testbed</doctitle>
	     <bottom>Copyright &#169; 2008 Qualipso Project. All Rights Reserved.</bottom>
	     <tag name="todo" scope="all" description="To do:" />
	     <!-- links -->	     
	     <link offline="${java.packageList.offline}" href="http://java.sun.com/j2se/1.5.0/docs/api/" packagelistLoc="${java.packageList.location}" />
	     <link offline="${httpunit.packageList.offline}" href="http://httpunit.sourceforge.net/doc/api/" packagelistLoc="${httpunit.packageList.location}" />
	     <link offline="${dom4j.packageList.offline}" href="http://www.dom4j.org/dom4j-1.6.1/apidocs/" packagelistLoc="${dom4j.packageList.location}" />
	   </javadoc>
	 </target> 


4.2 Compile the Adapter and Codec
Simply run the "all" target.

5. Run the test cases
To run the test cases the following must be done:
a. Set the main TTCN-3 module and set the JAR that holds the Adapter. 
To do this right click on the project then click on Properties. In the window that appears on the left side tab select TTCN-3 properties.
On Main Module Name select browse to GeoServerMain.ttcn3
On Jar file of Test Adaptor browse to /lib/TA.jar
On Class name of test adaptor select qualipso.ttworkgisgeo.tri.Adapter

b. A custom Test Campaign must be created that will execute the control part of GeoServerMain module.

