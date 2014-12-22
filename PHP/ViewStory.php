<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php

// Display the nested comments
function display_follow_up($cid, $level, $display, $filter, $link, $comment_table)
{
  $follow = mysql_query("SELECT story_id,id,subject,writer,date,childs FROM $comment_table WHERE parent=$cid", $link);
  if (!$follow)
  {
	error_log("[".__FILE__."] Query 'SELECT story_id,id,subject,writer,date,childs FROM $comment_table WHERE parent=$cid' failed: " . mysql_error($link));
	die("ERROR: Query failed for comment table '$comment_table' and parent '$cid': " . mysql_error($link));
  }
  while ($follow_row = mysql_fetch_array($follow))
  {
    for ($i = 0 ; $i < $level ; $i++)
      printf("&nbsp&nbsp&nbsp");
    print("<a href=\"/PHP/ViewComment.php?comment_table=$comment_table&storyId=".$follow_row["story_id"]."&commentId=".$follow_row["id"]."&filter=$filter&display=$display\">".$follow_row["subject"]."</a> by ".getUserName($follow_row["writer"], $link)." on ".$follow_row["date"]."<br>\n");
    if ($follow_row["childs"] > 0)
      display_follow_up($follow_row["id"], $level+1, $display, $filter, $link, $comment_table);
  }
}

    $scriptName = "ViewStory.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

    // Check parameters
    $storyId = getSessionPostGetParam('storyId');
    if (!isset($storyId))
    {
      printError($scriptName, $startTime, "Viewing story", "You must provide a story identifier!");
      exit();
    }
    $filter = getSessionPostGetParam('filter', 0);
    $display = getSessionPostGetParam('display', 1);
      
    getDatabaseLink($link);
    $result = mysql_query("SELECT * FROM stories WHERE id=$storyId");
	if (!$result)
	{
		error_log("[".__FILE__."] Query 'SELECT * FROM stories WHERE id=$storyId' failed: " . mysql_error($link));
		die("ERROR: Query failed for story '$storyId': " . mysql_error($link));
	}
    if (mysql_num_rows($result) == 0)
    {
      $result = mysql_query("SELECT * FROM old_stories WHERE id=$storyId");
	  if (!$result)
	  {
		error_log("[".__FILE__."]: Query 'SELECT * FROM old_stories WHERE id=$storyId' failed: " . mysql_error($link));
		die("ERROR: Query failed for story '$storyId': " . mysql_error($link));
	  }
      $comment_table = "old_comments";
    }
    else
      $comment_table = "comments";
    if (mysql_num_rows($result) == 0)
      die("<h3>ERROR: Sorry, but this story '$storyId' does not exist.</h3><br>\n");
    $row = mysql_fetch_array($result);
    $username = getUserName($row["writer"], $link);

    // Display the story

    printHTMLheader("RUBBoS: Viewing story ".$row["title"]);

    printHTMLHighlighted($row["title"]);

    print("Posted by ".$username." on ".$row["date"]."<br>\n");
    print($row["body"]."<br>\n");
      print("<p><center><a href=\"/PHP/PostComment.php?comment_table=$comment_table&storyId=$storyId&parent=0\">Post a comment on this story</a></center><p>");

    // Display filter chooser header
    print("<br><hr><br>");
    print("<center><form action=\"/PHP/ViewComment.php\" method=POST>\n".
          "<input type=hidden name=commentId value=0>\n".
          "<input type=hidden name=storyId value=$storyId>\n".
          "<input type=hidden name=comment_table value=$comment_table>\n".
          "<B>Filter :</B>&nbsp&nbsp<SELECT name=filter>\n");
    $count_result = mysql_query("SELECT rating, COUNT(rating) AS count FROM $comment_table WHERE story_id=$storyId GROUP BY rating ORDER BY rating", $link);
	if (!$count_result)
	{
		error_log("[".__FILE__."] Query 'SELECT rating, COUNT(rating) AS count FROM $comment_table WHERE story_id=$storyId GROUP BY rating ORDER BY rating' failed: " . mysql_error($link));
		die("ERROR: Query failed for comment '$comment_table' and story '$storyId': " . mysql_error($link));
	}
    $i = -1;
    while ($count_row = mysql_fetch_array($count_result))
    {
      while (($i < 6) && ($count_row["rating"] != $i))
      {
        if ($i == $filter)
          print("<OPTION selected value=\"$i\">$i: 0 comment</OPTION>\n");
        else
          print("<OPTION value=\"$i\">$i: 0 comment</OPTION>\n");
        $i++;
      }
      if ($count_row["rating"] == $i)
      {
        if ($i == $filter)
          print("<OPTION selected value=\"$i\">$i: ".$count_row["count"]." comments</OPTION>\n");
        else
          print("<OPTION value=\"$i\">$i: ".$count_row["count"]." comments</OPTION>\n");
        $i++;
      }
    }
    while ($i < 6)
    {
      print("<OPTION value=\"$i\">$i: 0 comment</OPTION>\n");
      $i++;
    }

    print("</SELECT>&nbsp&nbsp&nbsp&nbsp<SELECT name=display>\n".
          "<OPTION value=\"0\">Main threads</OPTION>\n".
          "<OPTION selected value=\"1\">Nested</OPTION>\n".
          "<OPTION value=\"2\">All comments</OPTION>\n".
          "</SELECT>&nbsp&nbsp&nbsp&nbsp<input type=submit value=\"Refresh display\"></center><p>\n");          

    // Display the comments
    $comment = mysql_query("SELECT * FROM $comment_table WHERE story_id=$storyId AND parent=0 AND rating>=$filter", $link);
	if (!$comment)
	{
		error_log("[".__FILE__."] Query 'SELECT * FROM $comment_table WHERE story_id=$storyId AND parent=0 AND rating>=$filter' failed: " . mysql_error($link));
		die("ERROR: Query failed for comment table '$comment_table' and story '$storyId': " . mysql_error($link));
	}
    while ($comment_row = mysql_fetch_array($comment))
    {
      print("<br><hr><br>");
      $username = getUserName($comment_row["writer"], $link);
      print("<TABLE width=\"100%\" bgcolor=\"#CCCCFF\"><TR><TD><FONT size=\"4\" color=\"#000000\"><B><a href=\"/PHP/ViewComment.php?comment_table=$comment_table&storyId=$storyId&commentId=".$comment_row["id"]."&filter=$filter&display=$display\">".$comment_row["subject"]."</a></B>&nbsp</FONT> (Score:".$comment_row["rating"].")</TABLE>\n");
      print("<TABLE><TR><TD><B>Posted by ".$username." on ".$comment_row["date"]."</B><p>\n");
      print("<TR><TD>".$comment_row["comment"]);
      print("<TR><TD><p>[ <a href=\"/PHP/PostComment.php?comment_table=$comment_table&storyId=$storyId&parent=".$comment_row["id"]."\">Reply to this</a>&nbsp|&nbsp".
            "<a href=\"/PHP/ViewComment.php?comment_table=$comment_table&storyId=$storyId&commentId=".$comment_row["parent"]."&filter=$filter&display=$display\">Parent</a>".
            "&nbsp|&nbsp<a href=\"/PHP/ModerateComment.php?comment_table=$comment_table&commentId=".$comment_row["id"]."\">Moderate</a> ]</TABLE>\n");
      if ($comment_row["childs"] > 0)
        display_follow_up($comment_row["id"], 1, $display, $filter, $link, $comment_table);
    }

    mysql_free_result($result);
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
