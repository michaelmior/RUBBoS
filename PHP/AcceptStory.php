<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "AcceptStory.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

    $storyId = getSessionPostGetParam('storyId');
    if (!isset($storyId))
	{
      printError($scriptName, $startTime, "AcceptStory", "You must provide a story identifier!");
      exit();
    }

    getDatabaseLink($link);

    printHTMLheader("RUBBoS: Story submission result");

    print("<center><h2>Story submission result:</h2></center><p>\n");

    $result = mysql_query("SELECT * FROM submissions WHERE id=$storyId");
	if (!$result)
	{
		error_log("[".__FILE__."] Query 'SELECT * FROM submissions WHERE id=$storyId' failed: " . mysql_error($link));
		die("ERROR: Query failed for story '$storyId': " . mysql_error($link));
	}
    if (mysql_num_rows($result) == 0)
      die("<h3>ERROR: Sorry, but this story '$storyId' does not exist.</h3><br>\n");
    $row = mysql_fetch_array($result);

    // Add story to database
    $result = mysql_query("INSERT INTO stories VALUES (NULL, \"".$row["title"]."\", \"".$row["body"]."\", '".$row["date"]."', ".$row["writer"].", ".$row["category"].")", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Failed to insert new story in database 'INSERT INTO stories VALUES (NULL, \"".$row["title"]."\", \"".$row["body"]."\", '".$row["date"]."', ".$row["writer"].", ".$row["category"].")': " . mysql_error($link));
		die("ERROR: Failed to insert new story in database for writer '".$row["writer"]."' and category '".$row["category"]."': " . mysql_error($link));
	}
    $result = mysql_query("DELETE FROM submissions WHERE id=$storyId", $link); 
	if (!$result)
	{
		error_log("[".__FILE__."] Failed to delete 'DELETE FROM submissions WHERE id=$storyId': " . mysql_error($link));
		die("ERROR: Failed to delete this story '$storyId' from database: " . mysql_error($link));
	}

    print("The story has been successfully moved from the submission to the stories database table<br>\n");
    
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
