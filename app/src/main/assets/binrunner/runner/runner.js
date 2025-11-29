/**
 * QuickJS Runner 入口脚本
 * 加载并执行用户的爬虫脚本
 */

import * as std from 'std';
import * as os from 'os';

// 获取脚本参数
const args = scriptArgs.slice(1);
if (args.length < 2) {
    std.err.puts('用法: qjs runner.js <爬虫脚本路径> <方法名> [参数...]\n');
    std.exit(1);
}

const spiderPath = args[0];
const method = args[1];
const methodArgs = args.slice(2);

// 加载依赖库
const libPath = std.getenv('JS_LIB_PATH') || '.';

function loadLib(name) {
    try {
        const path = libPath + '/' + name;
        const content = std.loadFile(path);
        if (content) {
            std.evalScript(content, { backtrace_barrier: true });
        }
    } catch (e) {
        std.err.puts('加载库失败: ' + name + ' - ' + e.message + '\n');
    }
}

// 加载核心库
loadLib('bridge.js');
loadLib('global.js');
loadLib('http.js');
loadLib('crypto-js.js');
loadLib('cheerio.min.js');
loadLib('spider.js');

// 加载爬虫脚本
let spider = null;
try {
    const spiderCode = std.loadFile(spiderPath);
    if (!spiderCode) {
        throw new Error('无法读取爬虫脚本: ' + spiderPath);
    }
    
    // 执行爬虫脚本，确保脚本导出 spider 对象
    // 先执行脚本，然后检查全局 spider 变量
    std.evalScript(spiderCode, { 
        backtrace_barrier: true 
    });
    
    // 检查脚本是否定义了 spider 对象
    if (typeof spider !== 'undefined') {
        // spider 已在脚本中定义为全局变量
    } else if (typeof __spider__ !== 'undefined') {
        spider = __spider__;
    } else {
        throw new Error('爬虫脚本未导出 spider 对象');
    }
    
    if (!spider) {
        throw new Error('爬虫脚本未导出 spider 对象');
    }
} catch (e) {
    std.err.puts('加载爬虫脚本失败: ' + e.message + '\n');
    std.exit(1);
}

// 调用爬虫方法
async function callMethod() {
    try {
        let result;
        
        switch (method) {
            case 'init':
                result = await spider.init(methodArgs[0] || '');
                break;
            case 'homeContent':
                result = await spider.homeContent(methodArgs[0] === 'true');
                break;
            case 'homeVideoContent':
                result = await spider.homeVideoContent();
                break;
            case 'categoryContent':
                result = await spider.categoryContent(
                    methodArgs[0],
                    methodArgs[1],
                    methodArgs[2] === 'true',
                    methodArgs[3] ? JSON.parse(methodArgs[3]) : {}
                );
                break;
            case 'detailContent':
                result = await spider.detailContent(
                    methodArgs[0] ? JSON.parse(methodArgs[0]) : []
                );
                break;
            case 'searchContent':
                result = await spider.searchContent(
                    methodArgs[0],
                    methodArgs[1] === 'true',
                    methodArgs[2] || '1'
                );
                break;
            case 'playerContent':
                result = await spider.playerContent(
                    methodArgs[0],
                    methodArgs[1],
                    methodArgs[2] ? JSON.parse(methodArgs[2]) : []
                );
                break;
            case 'liveContent':
                result = await spider.liveContent(methodArgs[0] || '');
                break;
            case 'proxy':
                result = await spider.proxy(
                    methodArgs[0] ? JSON.parse(methodArgs[0]) : {}
                );
                break;
            case 'action':
                result = await spider.action(methodArgs[0] || '');
                break;
            case 'destroy':
                if (spider.destroy) {
                    await spider.destroy();
                }
                result = '';
                break;
            default:
                throw new Error('未知方法: ' + method);
        }
        
        // 输出结果
        if (typeof result === 'string') {
            std.out.puts(result);
        } else if (result !== undefined && result !== null) {
            std.out.puts(JSON.stringify(result));
        }
        
    } catch (e) {
        std.err.puts('执行方法失败: ' + method + ' - ' + e.message + '\n');
        std.out.puts(JSON.stringify({ error: e.message }));
    }
}

callMethod();
