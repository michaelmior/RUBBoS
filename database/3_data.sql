use rubbos;
load data infile "./users.data" into table users fields terminated by "\t";
load data infile "./stories.data" into table stories fields terminated by "\t";
load data infile "./comments.data" into table comments fields terminated by "\t";
load data infile "./old_stories.data" into table old_stories fields terminated by "\t";
load data infile "./old_comments.data" into table old_comments fields terminated by "\t";
load data infile "./submissions.data" into table submissions fields terminated by "\t";
load data infile "./moderator_log.data" into table moderator_log fields terminated by "\t";

