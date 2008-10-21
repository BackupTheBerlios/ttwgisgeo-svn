           TOC
1. Setting the GISClient and GEOServer URL
2. Compiling & running the tests
2.1 Set up
2.2 Compile the Adapter, Codec and any other auxiliary Java classes
2.3 Compile the TTCN-3 source
2.4 Run the tests

1. Setting the GISClient and GEOServer URL
In the geoserver.tri.Adapter class set the GISCLIENT constant to the URL of the main page:
private final String GISCLIENT = "http://syros.eurodyn.com:8088/GISClient";
The GEOSERVER constant must be set to the URL of the WFS endpoint of the GEOSERVER application.
private final String GEOSERVER = "http://10.250.20.164:8080/geoserver/wfs";

2. Compiling & running the tests
2.1 Set up
Copy the TTWorkGIS folder to a workspace 
From the TTWorkbech import the project in to the TTWorkbench environment.
HttpUnit 1.7 and dom4j 1.6.1 are necessary for this project. Since some of their XML APIs might be conflicting only the following jars must into the /lib directory:
httpunit.jar 
dom4j-1.6.1.jar 
xercesImpl-2.6.1.jar 
xmlParserAPIs-2.6.1.jar 
activation-1.1.jar 
js-1.6R5.jar 
jtidy-4aug2000r7-dev.jar 
junit-3.8.1.jar 
mail-1.4.jar 
servlet-api-2.4.jar 
jaxen-1.1.1.jar

2.2 Compile the Adapter and Codec
The TTWorkbench environment automatically generates an Ant build script. It generates a TA.jar which contains the Adapter and Codec classes. Some additions must be made to it in order to integrate HttpUnit and dom4j:
A Class-Path attribute must be added to the manifest of TA.jar. Its value must be all the names of the jar files of section 2.1 separated by a space: 

The property must be:
<property name="additionalLibraries" value="dom4j-1.6.1.jar httpunit.jar xercesImpl-2.6.1.jar xmlParserAPIs-2.6.1.jar activation-1.1.jar js-1.6R5.jar jtidy-4aug2000r7-dev.jar junit-3.8.1.jar mail-1.4.jar servlet-api-2.4.jar jaxen-1.1.1.jar"/>

And the jar task must be modified like this:
<target name="TA.jar" depends="init,classes">
		<echo>Create Test Adapter jar file</echo>
		<jar jarfile="${TA}" basedir="${classes}" includes="**/*.class">
			<manifest>
				<attribute name="Class-Path" value="${additionalLibraries}"/>
			</manifest>
		</jar>
</target>


To generate the Javadoc the following task can be added in the Ant build script:
<javadoc packagenames="geoservertest.*" 
				 sourcepath="javasrc"
				 destdir="javadoc"/>

The TA.jar must be placed in the same directory with the other jar files(/lib).
After running then ant script in build.xml a TA.jar will created in TTWorkGIS/lib directory. 

The adapter jar is made known to the TTMan module using the GUI: Start the Create Test Campaign wizard and in the fourth page select the Local Test Adapter radio button; then in Test Adapter Jar selection enter the TA.jar; in the Test Adapter Class selection enter the class implementing the adapter.

2.3 Compile the TTCN-3 source
To compile the TTCN-3 source files there is 'Compile' button on the toolbar whenever a TTCN-3 source file is edited. All the depended TTCN-3 files are automatically compiled. For more information see the help files of TTWorkbench.

2.4 Run the tests
This is done using TTMan module. For detailed information see the TTWorkbench help. 
To run the test first a test campaing must be created. To do this right-click on the project, in the navigation view, and select 'Create default test campaign'. Then on the Test Managment View load the test campaign, select the testcase you want to run and click on the Run button.
   

