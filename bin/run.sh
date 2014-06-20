
cd $(dirname $(readlink -f $0))/..
ant jar
java -Xmx2g -cp GateApp.jar:lib/*:lib/Jung/* com.bobboau.GateApp.GateFrame
