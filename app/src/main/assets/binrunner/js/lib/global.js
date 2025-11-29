/**
 * 全局方法实现
 * 提供爬虫脚本常用的全局方法
 */

/**
 * 日志输出
 */
function log(msg) {
    std.err.puts('[LOG] ' + msg + '\n');
}

/**
 * 打印调试信息
 */
function print(msg) {
    std.out.puts(msg);
}

/**
 * 延迟执行
 */
function sleep(ms) {
    os.sleep(ms);
}

/**
 * Base64 编码
 */
function base64Encode(str) {
    var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
    var bytes = [];
    for (var i = 0; i < str.length; i++) {
        bytes.push(str.charCodeAt(i));
    }
    
    var result = '';
    for (var i = 0; i < bytes.length; i += 3) {
        var n = (bytes[i] << 16) | ((bytes[i + 1] || 0) << 8) | (bytes[i + 2] || 0);
        result += chars[(n >> 18) & 63];
        result += chars[(n >> 12) & 63];
        result += i + 1 < bytes.length ? chars[(n >> 6) & 63] : '=';
        result += i + 2 < bytes.length ? chars[n & 63] : '=';
    }
    return result;
}

/**
 * Base64 解码
 */
function base64Decode(str) {
    var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
    str = str.replace(/=+$/, '');
    
    var result = '';
    for (var i = 0; i < str.length; i += 4) {
        var n = (chars.indexOf(str[i]) << 18) |
                (chars.indexOf(str[i + 1]) << 12) |
                ((chars.indexOf(str[i + 2]) || 0) << 6) |
                (chars.indexOf(str[i + 3]) || 0);
        result += String.fromCharCode((n >> 16) & 255);
        if (str[i + 2] !== '=') result += String.fromCharCode((n >> 8) & 255);
        if (str[i + 3] !== '=') result += String.fromCharCode(n & 255);
    }
    return result;
}

/**
 * URL 编码
 */
function encodeUrl(str) {
    return encodeURIComponent(str);
}

/**
 * URL 解码
 */
function decodeUrl(str) {
    return decodeURIComponent(str);
}

/**
 * 获取当前时间戳（毫秒）
 */
function timestamp() {
    return Date.now();
}

/**
 * 生成随机字符串
 */
function randomStr(length) {
    var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var result = '';
    for (var i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
}

/**
 * 字符串格式化
 */
function sprintf(format) {
    var args = Array.prototype.slice.call(arguments, 1);
    var index = 0;
    return format.replace(/%[sdfo]/g, function(match) {
        var arg = args[index++];
        switch (match) {
            case '%s': return String(arg);
            case '%d': return parseInt(arg);
            case '%f': return parseFloat(arg);
            case '%o': return JSON.stringify(arg);
            default: return match;
        }
    });
}

/**
 * 检查是否为空
 */
function isEmpty(obj) {
    if (obj === null || obj === undefined) return true;
    if (typeof obj === 'string') return obj.trim() === '';
    if (Array.isArray(obj)) return obj.length === 0;
    if (typeof obj === 'object') return Object.keys(obj).length === 0;
    return false;
}

/**
 * 对象深拷贝
 */
function deepClone(obj) {
    return JSON.parse(JSON.stringify(obj));
}

/**
 * 数组去重
 */
function unique(arr) {
    return [...new Set(arr)];
}

/**
 * 正则匹配所有
 */
function matchAll(str, regex) {
    var result = [];
    var match;
    var re = new RegExp(regex, 'g');
    while ((match = re.exec(str)) !== null) {
        result.push(match);
    }
    return result;
}

/**
 * HTML 实体解码
 */
function htmlDecode(str) {
    var entities = {
        '&amp;': '&',
        '&lt;': '<',
        '&gt;': '>',
        '&quot;': '"',
        '&#39;': "'",
        '&nbsp;': ' '
    };
    return str.replace(/&[^;]+;/g, function(entity) {
        return entities[entity] || entity;
    });
}

/**
 * 移除 HTML 标签
 */
function stripTags(str) {
    return str.replace(/<[^>]*>/g, '');
}
