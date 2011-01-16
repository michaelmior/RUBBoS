##############################
#    Environment variables   #
##############################

JAVA  = $(JAVA_HOME)/bin/java
JAVAC = $(JAVA_HOME)/bin/javac
JAVACOPTS = -deprecation
JAVACC = $(JAVAC) $(JAVACOPTS)
RMIC = $(JAVA_HOME)/bin/rmic
RMIREGISTRY= $(JAVA_HOME)/bin/rmiregistry
CLASSPATH = .:$(J2EE_HOME)/lib/j2ee.jar:$(JAVA_HOME)/jre/lib/rt.jar
JAVADOC = $(JAVA_HOME)/bin/javadoc
JAR = $(JAVA_HOME)/bin/jar

MAKE = gmake
CP = /bin/cp
RM = /bin/rm
MKDIR = /bin/mkdir

# DB server: supported values are MySQL or PostgreSQL
DB_SERVER = MySQL

%.class: %.java
	${JAVACC} -classpath ${CLASSPATH} $<

