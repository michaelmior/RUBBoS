<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "BrowseCategories.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

    getDatabaseLink($link);

    printHTMLheader("RUBBoS available categories");
    
    $result = mysql_query("SELECT * FROM categories", $link);
	if (!$result)
	{
		error_log("[".__FILE__."] Query 'SELECT * FROM categories' failed: " . mysql_error($link));
		die("ERROR: Query failed: " . mysql_error($link));
	}
    if (mysql_num_rows($result) == 0)
      print("<h2>Sorry, but there is no category available at this time. Database table is empty</h2><br>\n");
    else
      print("<h2>Currently available categories</h2><br>\n");

    while ($row = mysql_fetch_array($result))
    {
      print("<a href=\"/PHP/BrowseStoriesByCategory.php?category=".$row["id"]."&categoryName=".urlencode($row["name"])."\">".$row["name"]."</a><br>\n");
    }
    mysql_free_result($result);
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
