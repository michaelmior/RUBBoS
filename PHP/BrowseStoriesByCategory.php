<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "StoriesOfTheDay.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

    $categoryName = $_POST['categoryName'];
    if ($categoryName == null)
    {
      $categoryName = $_GET['categoryName'];
      if ($categoryName == null)
      {
         printError($scriptName, $startTime, "Browse Stories By Category", "You must provide a category name!<br>");
         exit();
      }
    }
      
    $categoryId = $_POST['category'];
    if ($categoryId == null)
    {
      $categoryId = $_GET['category'];
      if ($categoryId == null)
      {
         printError($scriptName, $startTime, "Browse Stories By Category", "You must provide a category identifier!<br>");
         exit();
      }
    }
      
    $page = $_POST['page'];
    if ($page == null)
    {
      $page = $_GET['page'];
      if ($page == null)
        $page = "";
    }
      
    $nbOfStories = $_POST['nbOfStories'];
    if ($nbOfStories == null)
    {
      $nbOfStories = $_GET['nbOfStories'];
      if ($nbOfStories == null)
        $nbOfStories = 25;
    }

    printHTMLheader("RUBBoS Browse Stories By Category");
    print("<br><h2>Stories in category $categoryName</h2><br>");

    getDatabaseLink($link);
    $category_stories = new phpcassa\ColumnFamily($link, "CategoryStories");
    try {
      $result = $category_stories->get($categoryId, new phpcassa\ColumnSlice($page, "", $nbOfStories + 1, true));
    } catch (cassandra\NotFoundException $e) {
      $result = array();
    } catch (Exception $e) {
      die("ERROR: Query failed");
    }

    $oldestTime = min(array_keys($result));
    $hasMore = count($result) > $nbOfStories;
    if ($hasMore) unset($result[$oldestTime]);

    if (empty($result))
    {
      if (empty($page))
        print("<h2>Sorry, but there is no story available in this category !</h2>");
      else
      {
        print("<h2>Sorry, but there are no more stories available at this time.</h2><br>\n");
        /* TODO Fix pagination
        print("<p><CENTER>\n<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
              "&categoryName=".urlencode($categoryName)."&page=".($page-1)."&nbOfStories=$nbOfStories\">Previous page</a>\n</CENTER>\n");
         */
      }
      printHTMLfooter($scriptName, $startTime);
      exit();
    }

    // Print the story titles and author
    $stories = new phpcassa\ColumnFamily($link, "Stories");
    try {
      $result = $stories->multiget(array_values($result));
    } catch (Exception $e) {
      die("ERROR: Query failed");
    }
    foreach ($result as $storyId => $row)
    {
      $username = $row["writer"];
      print("<a href=\"/PHP/ViewStory.php?storyId=".$row["id"]."\">".$row["title"]."</a> by ".$username." on ".$row["date"]."<br>\n");
    }

    // Previous/Next links
    if ($hasMore) {
      print("<p><CENTER>\n<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
          "&categoryName=".urlencode($categoryName)."&page=".($oldestTime)."&nbOfStories=$nbOfStories\">Next page</a>\n</CENTER>\n");
    }

    /* TODO Fix pagination
    if ($page == 0)
      print("<p><CENTER>\n<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
           "&categoryName=".urlencode($categoryName)."&page=".($page+1)."&nbOfStories=$nbOfStories\">Next page</a>\n</CENTER>\n");
    else
      print("<p><CENTER>\n<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
            "&categoryName=".urlencode($categoryName)."&page=".($page-1)."&nbOfStories=$nbOfStories\">Previous page</a>\n&nbsp&nbsp&nbsp".
            "<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
            "&categoryName=".urlencode($categoryName)."&page=".($page+1)."&nbOfStories=$nbOfStories\">Next page</a>\n\n</CENTER>\n");
     */

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
