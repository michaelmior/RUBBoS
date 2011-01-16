
###########################
#    RUBBoS Makefile      #
###########################


##############################
#    Environment variables   #
##############################

JAVA  = /usr/bin/java
# JAVAC = /usr/java/jdk1.3.1/bin/javac
JAVAC = /usr/bin/javac
JAVACOPTS = +E -deprecation
JAVACC = $(JAVAC) $(JAVACOPTS)
CLASSPATH = .:/usr/java/j2sdkee1.3/lib/j2ee.jar:/usr/java/jdk1.3.1/jre/lib/rt.jar:/opt/jakarta-tomcat-3.2.3/lib/servlet.jar
JAVADOC = /usr/java/jdk1.3.1/bin/javadoc

####################
#       Client     #
####################

ClientFiles = URLGenerator URLGeneratorPHP RUBBoSProperties Stats \
	      TransitionTable ClientEmulator UserSession 

all_client_sources =  $(addprefix edu/rice/rubbos/client/, $(addsuffix .java, $(ClientFiles)))
all_client_obj = $(addprefix edu/rice/rubbos/client/, $(addsuffix .class, $(ClientFiles))) edu/rice/rubbos/beans/TimeManagement.class

client: $(all_client_obj)

initDB:
	${JAVA} -classpath .:./database edu.rice.rubbos.client.InitDB ${PARAM}

emulator:
	${JAVA} -classpath .:Client/rubbos_client.jar edu.rice.rubbos.client.ClientEmulator


############################
#       Global rules       #
############################


all: client javadoc flush_cache

world: all

javadoc :
	${JAVADOC} -d ./doc/api -bootclasspath ${CLASSPATH} -version -author -windowtitle "RUBBoS API" -header "<b>RUBBoS (C)2001 Rice University/INRIA</b><br>" edu.rice.rubbos.beans edu.rice.rubbos.client

clean:
	rm -f core edu/rice/rubbos/beans/*.class edu/rice/rubbos/client/*.class

%.class: %.java
	${JAVACC} -classpath ${CLASSPATH} $<

flush_cache: bench/flush_cache.c
	gcc bench/flush_cache.c -o bench/flush_cache
