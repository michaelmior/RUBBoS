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
      if (!empty($page)) {
        $prev_result = $category_stories->get($categoryId, new phpcassa\ColumnSlice($page, "", $nbOfStories + 1));
      } else {
        $prev_result = array();
      }
    } catch (cassandra\NotFoundException $e) {
      $result = array();
      $prev_result = array();
    } catch (Exception $e) {
      die("ERROR: Query failed");
    }

    $oldestTime = min(array_keys($result));
    $hasMore = count($result) > $nbOfStories;
    if ($hasMore) unset($result[$oldestTime]);

    // Get the start value for the previous page
    if (count($prev_result) > 1) {
      $prevStart = array_keys($prev_result)[count($prev_result) - 1];
    } else {
      $prevStart = null;
    }

    if (empty($result))
    {
      if (empty($page))
        print("<h2>Sorry, but there is no story available in this category !</h2>");
      else
      {
        print("<h2>Sorry, but there are no more stories available at this time.</h2><br>\n");
        if ($prevStart) {
          print("<p><CENTER>\n<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
                "&categoryName=".urlencode($categoryName)."&page=".$prevStart."&nbOfStories=$nbOfStories\">Previous page</a>\n</CENTER>\n");
        }
      }
      printHTMLfooter($scriptName, $startTime);
      exit();
    }

    // Print the story titles and author
    $stories = new phpcassa\ColumnFamily($link, "Stories");
    $stories->return_format = phpcassa\ColumnFamily::ARRAY_FORMAT;
    try {
      $result = $stories->multiget(array_values($result));
    } catch (Exception $e) {
      die("ERROR: Query failed");
    }
    foreach ($result as $story)
    {
      $storyId = $story[0]->string;
      $row = array();
      foreach ($story[1] as $column) {
        $row[$column[0]] = $column[1];
      }
      $username = $row["writer"];
      print("<a href=\"/PHP/ViewStory.php?storyId=".$storyId."\">".$row["title"]."</a> by ".$username." on ".$row["date"]."<br>\n");
    }

    // Previous/Next links
    print("<p><CENTER>\n");
    if ($prevStart) {
      print("<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
          "&categoryName=".urlencode($categoryName)."&page=".$prevStart."&nbOfStories=$nbOfStories\">Previous page</a>\n&nbsp&nbsp&nbsp");
    }
    if ($hasMore) {
      print("<a href=\"/PHP/BrowseStoriesByCategory.php?category=$categoryId".
          "&categoryName=".urlencode($categoryName)."&page=".($oldestTime)."&nbOfStories=$nbOfStories\">Next page</a>\n</CENTER>\n");
    }
    print("</p></CENTER>\n");


    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
