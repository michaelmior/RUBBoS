use rubbos;
load data local infile "./users.data" into table users fields terminated by "\t";
load data local infile "./stories.data" into table stories fields terminated by "\t";
load data local infile "./comments.data" into table comments fields terminated by "\t";
load data local infile "./old_stories.data" into table old_stories fields terminated by "\t";
load data local infile "./old_comments.data" into table old_comments fields terminated by "\t";
load data local infile "./submissions.data" into table submissions fields terminated by "\t";
load data local infile "./moderator_log.data" into table moderator_log fields terminated by "\t";

