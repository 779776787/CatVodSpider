/**
 * HTTP 封装
 * 提供 HTTP 请求功能
 */

var BRIDGE_PORT = parseInt(std.getenv('BINRUNNER_BRIDGE_PORT') || '0');

/**
 * HTTP 请求
 * @param {string} url - 请求 URL
 * @param {object} options - 请求选项
 * @returns {string} 响应内容
 */
function _http(url, options) {
    options = options || {};
    var method = (options.method || 'GET').toUpperCase();
    var headers = options.headers || {};
    var data = options.data || options.body || '';
    var timeout = options.timeout || 30000;
    
    // 使用桥接服务
    if (BRIDGE_PORT > 0) {
        return bridgeHttp(url, method, headers, data);
    }
    
    // 回退到 curl
    return curlRequest(url, method, headers, data, timeout);
}

/**
 * 通过桥接服务发送 HTTP 请求
 */
function bridgeHttp(url, method, headers, data) {
    var requestData = {
        url: url,
        method: method,
        headers: headers
    };
    
    if (method === 'POST' && data) {
        requestData.data = typeof data === 'object' ? JSON.stringify(data) : data;
        if (typeof data === 'object') {
            requestData.json = true;
        }
    }
    
    var result = bridgeCall('http', requestData);
    return result.data || '';
}

/**
 * 使用 curl 发送请求
 */
function curlRequest(url, method, headers, data, timeout) {
    var cmd = ['curl', '-s', '-X', method];
    
    // 超时
    cmd.push('--max-time', Math.floor(timeout / 1000));
    
    // 请求头
    for (var key in headers) {
        cmd.push('-H', key + ': ' + headers[key]);
    }
    
    // 请求体
    if (method === 'POST' && data) {
        if (typeof data === 'object') {
            cmd.push('-H', 'Content-Type: application/json');
            cmd.push('-d', JSON.stringify(data));
        } else {
            cmd.push('-d', data);
        }
    }
    
    cmd.push(url);
    
    try {
        var pipe = std.popen(cmd.join(' '), 'r');
        var response = pipe.readAsString();
        pipe.close();
        return response;
    } catch (e) {
        console.error('HTTP 请求失败:', e.message);
        return '';
    }
}

/**
 * GET 请求
 */
function httpGet(url, headers) {
    return _http(url, { method: 'GET', headers: headers });
}

/**
 * POST 请求
 */
function httpPost(url, data, headers) {
    return _http(url, { method: 'POST', data: data, headers: headers });
}

/**
 * 请求封装对象
 */
var req = {
    get: function(url, headers) {
        return _http(url, { method: 'GET', headers: headers || {} });
    },
    
    post: function(url, data, headers) {
        return _http(url, { method: 'POST', data: data, headers: headers || {} });
    },
    
    postJson: function(url, json, headers) {
        headers = headers || {};
        headers['Content-Type'] = 'application/json';
        return _http(url, { method: 'POST', data: JSON.stringify(json), headers: headers });
    }
};

/**
 * fetch 风格的请求
 */
function fetch(url, options) {
    options = options || {};
    return new Promise(function(resolve, reject) {
        try {
            var response = _http(url, options);
            resolve({
                ok: true,
                status: 200,
                text: function() { return Promise.resolve(response); },
                json: function() { return Promise.resolve(JSON.parse(response)); }
            });
        } catch (e) {
            reject(e);
        }
    });
}
