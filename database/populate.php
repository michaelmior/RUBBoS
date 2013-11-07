<?
require dirname(__FILE__) . "/../vendor/autoload.php";

use phpcassa\SystemManager;
use phpcassa\Schema\StrategyClass;
use cassandra\NotFoundException;

$sys = new SystemManager("127.0.0.1");
try {
    $sys->describe_keyspace("RUBBoS");
    $sys->drop_keyspace("RUBBoS");
    die("Keyspace already exists");
} catch (NotFoundException $e) {
    // Good to go
}

$sys->create_keyspace("RUBBoS", array(
    "strategy_class" => StrategyClass::SIMPLE_STRATEGY,
    "strategy_options" => array('replication_factor' => '1')));

$sys->create_column_family("RUBBoS", "Users");
