#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Python Runner 入口脚本
加载并执行用户的爬虫脚本
"""

import sys
import os
import json
import importlib.util
import traceback

def load_spider(spider_path):
    """加载爬虫脚本"""
    try:
        # 添加爬虫所在目录到 Python 路径
        spider_dir = os.path.dirname(os.path.abspath(spider_path))
        if spider_dir not in sys.path:
            sys.path.insert(0, spider_dir)
        
        # 动态加载模块
        spec = importlib.util.spec_from_file_location("spider_module", spider_path)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        
        # 获取 spider 对象
        if hasattr(module, 'spider'):
            return module.spider
        elif hasattr(module, 'Spider'):
            return module.Spider()
        else:
            raise Exception("爬虫脚本未定义 spider 对象或 Spider 类")
            
    except Exception as e:
        raise Exception(f"加载爬虫脚本失败: {str(e)}")

def parse_args(args, expected_types):
    """解析参数"""
    result = []
    for i, (arg, expected) in enumerate(zip(args, expected_types)):
        if expected == 'bool':
            result.append(arg.lower() == 'true')
        elif expected == 'json':
            try:
                result.append(json.loads(arg) if arg else {})
            except:
                result.append({})
        elif expected == 'list':
            try:
                result.append(json.loads(arg) if arg else [])
            except:
                result.append([])
        else:
            result.append(arg)
    return result

def call_method(spider, method, args):
    """调用爬虫方法"""
    try:
        if method == 'init':
            return spider.init(args[0] if args else '')
            
        elif method == 'homeContent':
            filter_flag = args[0].lower() == 'true' if args else False
            return spider.homeContent(filter_flag)
            
        elif method == 'homeVideoContent':
            return spider.homeVideoContent()
            
        elif method == 'categoryContent':
            tid = args[0] if len(args) > 0 else ''
            pg = args[1] if len(args) > 1 else '1'
            filter_flag = args[2].lower() == 'true' if len(args) > 2 else False
            extend = json.loads(args[3]) if len(args) > 3 and args[3] else {}
            return spider.categoryContent(tid, pg, filter_flag, extend)
            
        elif method == 'detailContent':
            ids = json.loads(args[0]) if args else []
            return spider.detailContent(ids)
            
        elif method == 'searchContent':
            key = args[0] if len(args) > 0 else ''
            quick = args[1].lower() == 'true' if len(args) > 1 else False
            pg = args[2] if len(args) > 2 else '1'
            return spider.searchContent(key, quick, pg)
            
        elif method == 'playerContent':
            flag = args[0] if len(args) > 0 else ''
            id_ = args[1] if len(args) > 1 else ''
            vip_flags = json.loads(args[2]) if len(args) > 2 and args[2] else []
            return spider.playerContent(flag, id_, vip_flags)
            
        elif method == 'liveContent':
            url = args[0] if args else ''
            return spider.liveContent(url)
            
        elif method == 'proxy':
            params = json.loads(args[0]) if args else {}
            return spider.proxy(params)
            
        elif method == 'action':
            action = args[0] if args else ''
            return spider.action(action)
            
        elif method == 'destroy':
            if hasattr(spider, 'destroy'):
                spider.destroy()
            return ''
            
        else:
            raise Exception(f"未知方法: {method}")
            
    except Exception as e:
        traceback.print_exc(file=sys.stderr)
        return json.dumps({"error": str(e)})

def main():
    if len(sys.argv) < 3:
        print("用法: python runner.py <爬虫脚本路径> <方法名> [参数...]", file=sys.stderr)
        sys.exit(1)
    
    spider_path = sys.argv[1]
    method = sys.argv[2]
    method_args = sys.argv[3:]
    
    try:
        # 加载爬虫
        spider = load_spider(spider_path)
        
        # 调用方法
        result = call_method(spider, method, method_args)
        
        # 输出结果
        if isinstance(result, str):
            print(result, end='')
        elif result is not None:
            print(json.dumps(result, ensure_ascii=False), end='')
            
    except Exception as e:
        traceback.print_exc(file=sys.stderr)
        print(json.dumps({"error": str(e)}), end='')
        sys.exit(1)

if __name__ == '__main__':
    main()
