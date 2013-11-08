<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "BrowseCategories.php";
    include("PHPprinter.php");
    $startTime = getMicroTime();

    getDatabaseLink($link);

    printHTMLheader("RUBBoS available categories");

    $categories = new phpcassa\ColumnFamily($link, "Categories");
    try {
      $result = $categories->get_range();
    } catch (cassandra\NotFoundException $e) {
      $result = array();
    } catch (Exception $e) {
      die("ERROR: Query failed");
    }
    if (empty($result))
      print("<h2>Sorry, but there is no category available at this time. Database table is empty</h2><br>\n");
    else
      print("<h2>Currently available categories</h2><br>\n");

    foreach ($result as $row)
    {
      print("<a href=\"/PHP/BrowseStoriesByCategory.php?category=".$row["name"]."&categoryName=".urlencode($row["name"])."\">".$row["name"]."</a><br>\n");
    }

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
