# Main Makefile #

include ../config.mk

#########################
#    Servlets version   #
#########################

Servlets = ServletPrinter StoriesOfTheDay BrowseCategories AcceptStory SubmitStory StoreStory RegisterUser Author BrowseStoriesByCategory ViewStory ViewComment PostComment OlderStories RejectStory ReviewStories Search StoreModeratorLog ModerateComment StoreComment RubbosHttpServlet TimeManagement Config

all_servlets_sources =  $(addprefix edu/rice/rubbos/servlets/, $(addsuffix .java, $(Servlets)))
all_servlets_obj = $(addprefix edu/rice/rubbos/servlets/, $(addsuffix .class, $(Servlets)))

servlets: $(all_servlets_obj)
	${JAR} cvf rubbos_servlets.jar $(all_servlets_obj)

all: servlets

javadoc :
	${MKDIR} -p doc
	${JAVADOC} -d ./doc -bootclasspath ${CLASSPATH} -version -author -windowtitle "RUBBoS Servlets API" -header "<b>RUBBoS (C)2004 Rice University/INRIA</b><br>"  edu.rice.rubbos.servlets

clean:
	${RM} -Rf edu/rice/rubbos/servlets/*.class doc/* rubbos_servlets.jar
