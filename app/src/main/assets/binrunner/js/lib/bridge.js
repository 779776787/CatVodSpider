/**
 * Java 桥接层
 * 通过 HTTP 调用 BinBridge 服务提供的 Java 方法
 */

var BRIDGE_PORT = parseInt(std.getenv('BINRUNNER_BRIDGE_PORT') || '0');
var BRIDGE_URL = 'http://127.0.0.1:' + BRIDGE_PORT;

/**
 * 调用桥接服务
 * @param {string} action - 操作名称
 * @param {object} data - 请求数据
 * @returns {object} 响应数据
 */
function bridgeCall(action, data) {
    if (BRIDGE_PORT <= 0) {
        throw new Error('桥接服务未启动');
    }
    
    try {
        var url = BRIDGE_URL + '/' + action;
        var body = JSON.stringify(data || {});
        
        // 使用 std.loadFile 发送 HTTP 请求
        // 注意：这是一个简化实现，实际需要使用 HTTP 库
        var cmd = ['curl', '-s', '-X', 'POST', '-H', 'Content-Type: application/json', '-d', body, url];
        var pipe = std.popen(cmd.join(' '), 'r');
        var response = pipe.readAsString();
        pipe.close();
        
        return JSON.parse(response || '{}');
    } catch (e) {
        console.error('桥接调用失败:', action, e.message);
        return { error: e.message };
    }
}

/**
 * 本地缓存
 */
var local = {
    get: function(key, defaultValue) {
        var result = bridgeCall('local.get', { key: key, default: defaultValue || '' });
        return result.value || defaultValue || '';
    },
    
    set: function(key, value) {
        bridgeCall('local.set', { key: key, value: value });
    },
    
    delete: function(key) {
        bridgeCall('local.delete', { key: key });
    }
};

/**
 * MD5 加密
 */
function md5X(data) {
    var result = bridgeCall('md5', { data: data });
    return result.result || '';
}

/**
 * AES 加密
 */
function aesX(mode, encrypt, data, key, iv) {
    var action = encrypt ? 'aes.encrypt' : 'aes.decrypt';
    var result = bridgeCall(action, { data: data, key: key, iv: iv || '' });
    return result.result || '';
}

/**
 * RSA 加解密
 */
function rsaX(mode, encrypt, data, key) {
    var action = encrypt ? 'rsa.encrypt' : 'rsa.decrypt';
    var result = bridgeCall(action, { data: data, key: key });
    return result.result || '';
}

/**
 * 简体转繁体
 */
function s2t(text) {
    var result = bridgeCall('s2t', { text: text });
    return result.result || text;
}

/**
 * 繁体转简体
 */
function t2s(text) {
    var result = bridgeCall('t2s', { text: text });
    return result.result || text;
}

/**
 * 获取代理端口
 */
function getPort() {
    var result = bridgeCall('getPort', {});
    return result.port || 0;
}

/**
 * 获取代理地址
 */
function getProxy(local) {
    var result = bridgeCall('getProxy', {});
    return result.url || '';
}

/**
 * URL 拼接
 */
function joinUrl(base, path) {
    var result = bridgeCall('joinUrl', { base: base, path: path });
    return result.url || base;
}
