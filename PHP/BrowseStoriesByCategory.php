<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "StoriesOfTheDay.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

	if (isset($_POST['categoryName']))
	{
    	$categoryName = $_POST['categoryName'];
	}
	elseif (isset($_GET['categoryName']))
    {
      $categoryName = $_GET['categoryName'];
	}
	else
	{
      printError($scriptName, $startTime, "Browse Stories By Category", "You must provide a category name!<br>");
      exit();
    }
      
	if (isset($_POST['category']))
	{
    	$categoryId = $_POST['category'];
	}
    elseif (isset($_GET['category']))
    {
      $categoryId = $_GET['category'];
	}
	else
	{
      printError($scriptName, $startTime, "Browse Stories By Category", "You must provide a category identifier!<br>");
      exit();
    }
      
	if (isset($_POST['page']))
	{
    	$page = $_POST['page'];
	}
	elseif (isset($_GET['page']))
    {
      $page = $_GET['page'];
	}
	else
	{
      $page = 0;
    }
      
	if (isset($_POST['nbOfStories']))
	{
    	$nbOfStories = $_POST['nbOfStories'];
	}
    elseif (isset($_GET['nbOfStories']))
    {
      $nbOfStories = $_GET['nbOfStories'];
	}
	else
	{
      $nbOfStories = 25;
    }

    printHTMLheader("RUBBoS Browse Stories By Category");
    print("<br><h2>Stories in category $categoryName</h2><br>");

    getDatabaseLink($link);
    $result = mysql_query("SELECT * FROM stories WHERE category=$categoryId ORDER BY date DESC LIMIT ".$page*$nbOfStories.",$nbOfStories", $link) or die("ERROR: Query failed");
    if (mysql_num_rows($result) == 0)
    {
      if ($page == 0)
        print("<h2>Sorry, but there is no story available in this category !</h2>");
      else
      {
        print("<h2>Sorry, but there are no more stories available at this time.</h2><br>\n");
        print("<p><CENTER>\n<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
              "&categoryName=".urlencode($categoryName)."&page=".($page-1)."&nbOfStories=$nbOfStories\">Previous page</a>\n</CENTER>\n");
      }
      mysql_free_result($result);
      mysql_close($link);
      printHTMLfooter($scriptName, $startTime);
      exit();
    }

    // Print the story titles and author
    while ($row = mysql_fetch_array($result))
    {
      $username = getUserName($row["writer"], $link);
      print("<a href=\"/PHP/ViewStory.php?storyId=".$row["id"]."\">".$row["title"]."</a> by ".$username." on ".$row["date"]."<br>\n");
    }

    // Previous/Next links
    if ($page == 0)
      print("<p><CENTER>\n<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
           "&categoryName=".urlencode($categoryName)."&page=".($page+1)."&nbOfStories=$nbOfStories\">Next page</a>\n</CENTER>\n");
    else
      print("<p><CENTER>\n<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
            "&categoryName=".urlencode($categoryName)."&page=".($page-1)."&nbOfStories=$nbOfStories\">Previous page</a>\n&nbsp&nbsp&nbsp".
            "<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
            "&categoryName=".urlencode($categoryName)."&page=".($page+1)."&nbOfStories=$nbOfStories\">Next page</a>\n\n</CENTER>\n");

    mysql_free_result($result);
    mysql_close($link);
    
    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
