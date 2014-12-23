<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "RejectStory.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

    $storyId = getSessionPostGetParam('storyId');
    if (!isset($storyId))
	{
      printError($scriptName, $startTime, "RejectStory", "You must provide a story identifier!");
      exit();
    }

    getDatabaseLink($link);

    printHTMLheader("RUBBoS: Story submission result");

    print("<center><h2>Story submission result:</h2></center><p>\n");

    $result = mysql_query("SELECT id FROM submissions WHERE id=$storyId");
	if (!$result)
	{
		error_log("[".__FILE__."] Query 'SELECT id FROM submissions WHERE id=$storyId' failed: " . mysql_error($link));
		die("ERROR: Query failed for story '$storyId': " . mysql_error($link));
	}
    if (mysql_num_rows($result) == 0)
      die("<h3>ERROR: Sorry, but this story '$storyId' does not exist.</h3><br>\n");

    // Delete entry from database
    $result = mysql_query("DELETE FROM submissions WHERE id=$storyId", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Failed to delete 'DELETE FROM submissions WHERE id=$storyId': " . mysql_error($link));
		die("ERROR: Failed to delete story '$storyId': " . mysql_error($link));
	}

    print("The story has been successfully removed from the submissions database table<br>\n");
    
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
