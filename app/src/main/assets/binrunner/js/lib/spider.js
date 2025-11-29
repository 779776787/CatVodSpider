/**
 * 爬虫加载器
 * 提供爬虫基础类和工具方法
 */

/**
 * 爬虫基类
 */
var Spider = function() {
    this.siteKey = '';
    this.siteType = 0;
};

Spider.prototype = {
    /**
     * 初始化
     * @param {string} extend - 扩展参数
     */
    init: function(extend) {
        return '';
    },
    
    /**
     * 首页内容
     * @param {boolean} filter - 是否过滤
     */
    homeContent: function(filter) {
        return '';
    },
    
    /**
     * 首页视频内容
     */
    homeVideoContent: function() {
        return '';
    },
    
    /**
     * 分类内容
     * @param {string} tid - 分类ID
     * @param {string} pg - 页码
     * @param {boolean} filter - 是否过滤
     * @param {object} extend - 扩展参数
     */
    categoryContent: function(tid, pg, filter, extend) {
        return '';
    },
    
    /**
     * 详情内容
     * @param {array} ids - ID列表
     */
    detailContent: function(ids) {
        return '';
    },
    
    /**
     * 搜索内容
     * @param {string} key - 关键词
     * @param {boolean} quick - 是否快速搜索
     * @param {string} pg - 页码
     */
    searchContent: function(key, quick, pg) {
        return '';
    },
    
    /**
     * 播放内容
     * @param {string} flag - 播放标识
     * @param {string} id - 视频ID
     * @param {array} vipFlags - VIP标识列表
     */
    playerContent: function(flag, id, vipFlags) {
        return '';
    },
    
    /**
     * 直播内容
     * @param {string} url - 直播URL
     */
    liveContent: function(url) {
        return '';
    },
    
    /**
     * 代理请求
     * @param {object} params - 请求参数
     */
    proxy: function(params) {
        return null;
    },
    
    /**
     * 动作
     * @param {string} action - 动作名称
     */
    action: function(action) {
        return '';
    },
    
    /**
     * 销毁
     */
    destroy: function() {
    }
};

/**
 * 创建爬虫实例
 */
function createSpider(proto) {
    var spider = new Spider();
    for (var key in proto) {
        spider[key] = proto[key];
    }
    return spider;
}

/**
 * 结果构建器
 */
var Result = {
    /**
     * 构建分类结果
     */
    classes: function(classes, list) {
        return JSON.stringify({
            'class': classes || [],
            'list': list || []
        });
    },
    
    /**
     * 构建列表结果
     */
    list: function(list, page, pageCount, limit, total) {
        return JSON.stringify({
            'list': list || [],
            'page': page || 1,
            'pagecount': pageCount || 1,
            'limit': limit || 20,
            'total': total || 0
        });
    },
    
    /**
     * 构建详情结果
     */
    detail: function(vod) {
        return JSON.stringify({
            'list': [vod]
        });
    },
    
    /**
     * 构建播放结果
     */
    player: function(url, parse, header) {
        var result = {
            'parse': parse ? 1 : 0,
            'url': url
        };
        if (header) {
            result['header'] = header;
        }
        return JSON.stringify(result);
    },
    
    /**
     * 构建错误结果
     */
    error: function(msg) {
        return JSON.stringify({
            'error': msg
        });
    }
};

/**
 * VOD 构建器
 */
var Vod = function() {
    this.vod_id = '';
    this.vod_name = '';
    this.vod_pic = '';
    this.type_name = '';
    this.vod_year = '';
    this.vod_area = '';
    this.vod_remarks = '';
    this.vod_actor = '';
    this.vod_director = '';
    this.vod_content = '';
    this.vod_play_from = '';
    this.vod_play_url = '';
};

/**
 * 分类构建器
 */
var Category = function(id, name) {
    this.type_id = id;
    this.type_name = name;
};
