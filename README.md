Knotable NLP variant using GateApp
=======

Knotable and NLP and summarizing email threads


TO BUILD
- get ANT
- type ant jar and it will make a jar
- then go to the jar, and run it using a line like this one which tells the paths

    java -Xmx2g -cp GateApp.jar:lib/*:lib/Jung/* com.bobboau.GateApp.GateFrame

- it will run! 

TO RUN
- you need Java version 1.7 -- don't get the latest! It doesn't work yet
- get the JDK if your JRE is the wrong one
- it will process PDFs

When the console pops up, this is what you are looking at

https://www.dropbox.com/s/8lxdymwk5tgnmws/Screenshot%202014-06-20%2016.07.51.png

plus a screen that diagrams the relations

DESIRED FUNCTION
- command line to run the app (so remove the GUI)
- it automatically searches the folder named input (so remove the folder selection step)
- it picks up all files and processes them as text (so we must remove the PDF converter)
- it writes the output as separate text files in a folder called /output (so we must ignore the diagram)

