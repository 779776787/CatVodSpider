<?php
/**
 * PHP Spider 基类
 * 所有 PHP 爬虫脚本应继承此类
 */

/**
 * 爬虫基类
 */
class Spider {
    public $siteKey = '';
    public $siteType = 0;
    
    /**
     * 初始化
     * @param string $extend 扩展参数
     * @return string 初始化结果
     */
    public function init($extend = '') {
        return '';
    }
    
    /**
     * 首页内容
     * @param bool $filter 是否过滤
     * @return string JSON 格式的首页内容
     */
    public function homeContent($filter = false) {
        return '';
    }
    
    /**
     * 首页视频内容
     * @return string JSON 格式的视频列表
     */
    public function homeVideoContent() {
        return '';
    }
    
    /**
     * 分类内容
     * @param string $tid 分类ID
     * @param string $pg 页码
     * @param bool $filter 是否过滤
     * @param array $extend 扩展参数
     * @return string JSON 格式的分类内容
     */
    public function categoryContent($tid, $pg, $filter, $extend) {
        return '';
    }
    
    /**
     * 详情内容
     * @param array $ids ID列表
     * @return string JSON 格式的详情内容
     */
    public function detailContent($ids) {
        return '';
    }
    
    /**
     * 搜索内容
     * @param string $key 搜索关键词
     * @param bool $quick 是否快速搜索
     * @param string $pg 页码
     * @return string JSON 格式的搜索结果
     */
    public function searchContent($key, $quick, $pg = '1') {
        return '';
    }
    
    /**
     * 播放内容
     * @param string $flag 播放标识
     * @param string $id 视频ID
     * @param array $vipFlags VIP标识列表
     * @return string JSON 格式的播放内容
     */
    public function playerContent($flag, $id, $vipFlags) {
        return '';
    }
    
    /**
     * 直播内容
     * @param string $url 直播URL
     * @return string JSON 格式的直播内容
     */
    public function liveContent($url) {
        return '';
    }
    
    /**
     * 代理请求
     * @param array $params 请求参数
     * @return mixed 代理响应
     */
    public function proxy($params) {
        return null;
    }
    
    /**
     * 动作
     * @param string $action 动作名称
     * @return string 动作结果
     */
    public function action($action) {
        return '';
    }
    
    /**
     * 销毁
     */
    public function destroy() {
    }
}

/**
 * 结果构建器
 */
class Result {
    /**
     * 构建分类结果
     */
    public static function classes($classes, $vods = []) {
        return json_encode([
            'class' => $classes ?: [],
            'list' => $vods ?: []
        ], JSON_UNESCAPED_UNICODE);
    }
    
    /**
     * 构建列表结果
     */
    public static function list($vods, $page = 1, $pagecount = 1, $limit = 20, $total = 0) {
        return json_encode([
            'list' => $vods ?: [],
            'page' => $page,
            'pagecount' => $pagecount,
            'limit' => $limit,
            'total' => $total
        ], JSON_UNESCAPED_UNICODE);
    }
    
    /**
     * 构建详情结果
     */
    public static function detail($vod) {
        return json_encode([
            'list' => [$vod]
        ], JSON_UNESCAPED_UNICODE);
    }
    
    /**
     * 构建播放结果
     */
    public static function player($url, $parse = 0, $header = null) {
        $result = [
            'parse' => $parse,
            'url' => $url
        ];
        if ($header) {
            $result['header'] = $header;
        }
        return json_encode($result, JSON_UNESCAPED_UNICODE);
    }
    
    /**
     * 构建错误结果
     */
    public static function error($msg) {
        return json_encode(['error' => $msg], JSON_UNESCAPED_UNICODE);
    }
}

/**
 * HTTP 请求工具类
 */
class Http {
    private static $defaultHeaders = [
        'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
    ];
    
    /**
     * GET 请求
     */
    public static function get($url, $headers = []) {
        try {
            $opts = [
                'http' => [
                    'method' => 'GET',
                    'header' => array_merge(self::$defaultHeaders, $headers),
                    'timeout' => 30
                ],
                'ssl' => [
                    'verify_peer' => false,
                    'verify_peer_name' => false
                ]
            ];
            $context = stream_context_create($opts);
            return file_get_contents($url, false, $context);
        } catch (Exception $e) {
            error_log("HTTP GET 请求失败: " . $e->getMessage());
            return '';
        }
    }
    
    /**
     * POST 请求
     */
    public static function post($url, $data = '', $headers = []) {
        try {
            if (is_array($data)) {
                $data = http_build_query($data);
            }
            
            $opts = [
                'http' => [
                    'method' => 'POST',
                    'header' => array_merge(self::$defaultHeaders, ['Content-Type: application/x-www-form-urlencoded'], $headers),
                    'content' => $data,
                    'timeout' => 30
                ],
                'ssl' => [
                    'verify_peer' => false,
                    'verify_peer_name' => false
                ]
            ];
            $context = stream_context_create($opts);
            return file_get_contents($url, false, $context);
        } catch (Exception $e) {
            error_log("HTTP POST 请求失败: " . $e->getMessage());
            return '';
        }
    }
    
    /**
     * POST JSON 请求
     */
    public static function postJson($url, $data, $headers = []) {
        $headers[] = 'Content-Type: application/json';
        return self::post($url, json_encode($data), $headers);
    }
}

/**
 * 加解密工具类
 */
class Crypto {
    /**
     * MD5 加密
     */
    public static function md5($data) {
        return md5($data);
    }
    
    /**
     * Base64 编码
     */
    public static function base64Encode($data) {
        return base64_encode($data);
    }
    
    /**
     * Base64 解码
     */
    public static function base64Decode($data) {
        return base64_decode($data);
    }
}
