all: compile

compile:
	java -jar jtb133di.jar -te minijava.jj
	java -jar javacc5.jar minijava-jtb.jj
	javac Main.java

clean:
	rm -rf *.class syntaxtree visitor minijava-jtb.jj *~

distclean: clean
	rm -rf *.class *Parser*.java ParseException.java Token.java TokenMgrError.java JavaCharStream.java *~ 