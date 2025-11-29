<?php
/**
 * PHP Runner 入口脚本
 * 加载并执行用户的爬虫脚本
 */

// 设置错误报告
error_reporting(E_ALL);
ini_set('display_errors', 0);

// 获取参数
$args = array_slice($argv, 1);
if (count($args) < 2) {
    fwrite(STDERR, "用法: php runner.php <爬虫脚本路径> <方法名> [参数...]\n");
    exit(1);
}

$spiderPath = $args[0];
$method = $args[1];
$methodArgs = array_slice($args, 2);

// 加载爬虫脚本
try {
    if (!file_exists($spiderPath)) {
        throw new Exception("爬虫脚本不存在: " . $spiderPath);
    }
    
    // 添加脚本目录到 include path
    $spiderDir = dirname(realpath($spiderPath));
    set_include_path(get_include_path() . PATH_SEPARATOR . $spiderDir);
    
    require_once $spiderPath;
    
    // 获取 spider 对象
    if (!isset($spider) && !class_exists('Spider')) {
        throw new Exception("爬虫脚本未定义 \$spider 变量或 Spider 类");
    }
    
    if (!isset($spider)) {
        $spider = new Spider();
    }
    
} catch (Exception $e) {
    fwrite(STDERR, "加载爬虫脚本失败: " . $e->getMessage() . "\n");
    exit(1);
}

/**
 * 调用爬虫方法
 */
function callMethod($spider, $method, $args) {
    try {
        switch ($method) {
            case 'init':
                $result = $spider->init($args[0] ?? '');
                break;
                
            case 'homeContent':
                $filter = isset($args[0]) && strtolower($args[0]) === 'true';
                $result = $spider->homeContent($filter);
                break;
                
            case 'homeVideoContent':
                $result = $spider->homeVideoContent();
                break;
                
            case 'categoryContent':
                $tid = $args[0] ?? '';
                $pg = $args[1] ?? '1';
                $filter = isset($args[2]) && strtolower($args[2]) === 'true';
                $extend = isset($args[3]) ? json_decode($args[3], true) : [];
                $result = $spider->categoryContent($tid, $pg, $filter, $extend ?? []);
                break;
                
            case 'detailContent':
                $ids = isset($args[0]) ? json_decode($args[0], true) : [];
                $result = $spider->detailContent($ids ?? []);
                break;
                
            case 'searchContent':
                $key = $args[0] ?? '';
                $quick = isset($args[1]) && strtolower($args[1]) === 'true';
                $pg = $args[2] ?? '1';
                $result = $spider->searchContent($key, $quick, $pg);
                break;
                
            case 'playerContent':
                $flag = $args[0] ?? '';
                $id = $args[1] ?? '';
                $vipFlags = isset($args[2]) ? json_decode($args[2], true) : [];
                $result = $spider->playerContent($flag, $id, $vipFlags ?? []);
                break;
                
            case 'liveContent':
                $url = $args[0] ?? '';
                $result = $spider->liveContent($url);
                break;
                
            case 'proxy':
                $params = isset($args[0]) ? json_decode($args[0], true) : [];
                $result = $spider->proxy($params ?? []);
                break;
                
            case 'action':
                $action = $args[0] ?? '';
                $result = $spider->action($action);
                break;
                
            case 'destroy':
                if (method_exists($spider, 'destroy')) {
                    $spider->destroy();
                }
                $result = '';
                break;
                
            default:
                throw new Exception("未知方法: " . $method);
        }
        
        return $result;
        
    } catch (Exception $e) {
        fwrite(STDERR, "执行方法失败: " . $method . " - " . $e->getMessage() . "\n");
        return json_encode(['error' => $e->getMessage()]);
    }
}

// 调用方法
$result = callMethod($spider, $method, $methodArgs);

// 输出结果
if (is_string($result)) {
    echo $result;
} elseif ($result !== null) {
    echo json_encode($result, JSON_UNESCAPED_UNICODE);
}
