/**
 * Node.js Runner 入口脚本
 * 加载并执行用户的爬虫脚本
 */

const fs = require('fs');
const path = require('path');
const vm = require('vm');

// 获取脚本参数
const args = process.argv.slice(2);
if (args.length < 2) {
    console.error('用法: node runner_node.js <爬虫脚本路径> <方法名> [参数...]');
    process.exit(1);
}

const spiderPath = args[0];
const method = args[1];
const methodArgs = args.slice(2);

// 获取库路径
const libPath = process.env.NODE_PATH || path.join(__dirname, '../js/lib');

// 加载库文件
function loadLib(name) {
    try {
        const libFile = path.join(libPath, name);
        if (fs.existsSync(libFile)) {
            const content = fs.readFileSync(libFile, 'utf8');
            vm.runInThisContext(content, { filename: name });
        }
    } catch (e) {
        console.error('加载库失败:', name, '-', e.message);
    }
}

// 创建全局对象
global.console = console;

// 加载核心库
loadLib('http.js');
loadLib('crypto-js.js');

// 加载爬虫脚本
let spider = null;
try {
    if (!fs.existsSync(spiderPath)) {
        throw new Error('爬虫脚本不存在: ' + spiderPath);
    }
    
    const spiderCode = fs.readFileSync(spiderPath, 'utf8');
    
    // 创建模块上下文
    const context = {
        ...global,
        module: { exports: {} },
        exports: {},
        require: require,
        __filename: spiderPath,
        __dirname: path.dirname(spiderPath)
    };
    
    vm.runInNewContext(spiderCode, context, { filename: spiderPath });
    
    spider = context.spider || context.module.exports || context.exports;
    
    if (!spider) {
        throw new Error('爬虫脚本未导出 spider 对象');
    }
} catch (e) {
    console.error('加载爬虫脚本失败:', e.message);
    process.exit(1);
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
            process.stdout.write(result);
        } else if (result !== undefined && result !== null) {
            process.stdout.write(JSON.stringify(result));
        }
        
    } catch (e) {
        console.error('执行方法失败:', method, '-', e.message);
        process.stdout.write(JSON.stringify({ error: e.message }));
    }
}

callMethod().then(() => {
    process.exit(0);
}).catch(e => {
    console.error(e);
    process.exit(1);
});
