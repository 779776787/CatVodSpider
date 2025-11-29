# -*- coding: utf-8 -*-
"""
Python Spider 基类
所有 Python 爬虫脚本应继承此类
"""

import json
import urllib.request
import urllib.parse
import hashlib
import base64
from typing import List, Dict, Any, Optional


class Spider:
    """
    爬虫基类
    """
    
    def __init__(self):
        self.siteKey = ''
        self.siteType = 0
    
    def init(self, extend: str = '') -> str:
        """
        初始化
        :param extend: 扩展参数
        :return: 初始化结果
        """
        return ''
    
    def homeContent(self, filter: bool = False) -> str:
        """
        首页内容
        :param filter: 是否过滤
        :return: JSON 格式的首页内容
        """
        return ''
    
    def homeVideoContent(self) -> str:
        """
        首页视频内容
        :return: JSON 格式的视频列表
        """
        return ''
    
    def categoryContent(self, tid: str, pg: str, filter: bool, extend: Dict[str, str]) -> str:
        """
        分类内容
        :param tid: 分类ID
        :param pg: 页码
        :param filter: 是否过滤
        :param extend: 扩展参数
        :return: JSON 格式的分类内容
        """
        return ''
    
    def detailContent(self, ids: List[str]) -> str:
        """
        详情内容
        :param ids: ID列表
        :return: JSON 格式的详情内容
        """
        return ''
    
    def searchContent(self, key: str, quick: bool, pg: str = '1') -> str:
        """
        搜索内容
        :param key: 搜索关键词
        :param quick: 是否快速搜索
        :param pg: 页码
        :return: JSON 格式的搜索结果
        """
        return ''
    
    def playerContent(self, flag: str, id: str, vipFlags: List[str]) -> str:
        """
        播放内容
        :param flag: 播放标识
        :param id: 视频ID
        :param vipFlags: VIP标识列表
        :return: JSON 格式的播放内容
        """
        return ''
    
    def liveContent(self, url: str) -> str:
        """
        直播内容
        :param url: 直播URL
        :return: JSON 格式的直播内容
        """
        return ''
    
    def proxy(self, params: Dict[str, str]) -> Optional[Any]:
        """
        代理请求
        :param params: 请求参数
        :return: 代理响应
        """
        return None
    
    def action(self, action: str) -> str:
        """
        动作
        :param action: 动作名称
        :return: 动作结果
        """
        return ''
    
    def destroy(self):
        """
        销毁
        """
        pass


class Result:
    """
    结果构建器
    """
    
    @staticmethod
    def classes(classes: List[Dict], vods: List[Dict] = None) -> str:
        """构建分类结果"""
        return json.dumps({
            'class': classes or [],
            'list': vods or []
        }, ensure_ascii=False)
    
    @staticmethod
    def list(vods: List[Dict], page: int = 1, pagecount: int = 1, 
             limit: int = 20, total: int = 0) -> str:
        """构建列表结果"""
        return json.dumps({
            'list': vods or [],
            'page': page,
            'pagecount': pagecount,
            'limit': limit,
            'total': total
        }, ensure_ascii=False)
    
    @staticmethod
    def detail(vod: Dict) -> str:
        """构建详情结果"""
        return json.dumps({
            'list': [vod]
        }, ensure_ascii=False)
    
    @staticmethod
    def player(url: str, parse: int = 0, header: Dict = None) -> str:
        """构建播放结果"""
        result = {
            'parse': parse,
            'url': url
        }
        if header:
            result['header'] = header
        return json.dumps(result, ensure_ascii=False)
    
    @staticmethod
    def error(msg: str) -> str:
        """构建错误结果"""
        return json.dumps({'error': msg}, ensure_ascii=False)


class Http:
    """
    HTTP 请求工具类
    """
    
    DEFAULT_HEADERS = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
    }
    
    @staticmethod
    def get(url: str, headers: Dict[str, str] = None) -> str:
        """GET 请求"""
        try:
            req_headers = Http.DEFAULT_HEADERS.copy()
            if headers:
                req_headers.update(headers)
            
            request = urllib.request.Request(url, headers=req_headers)
            with urllib.request.urlopen(request, timeout=30) as response:
                return response.read().decode('utf-8')
        except Exception as e:
            print(f'HTTP GET 请求失败: {e}')
            return ''
    
    @staticmethod
    def post(url: str, data: Any = None, headers: Dict[str, str] = None) -> str:
        """POST 请求"""
        try:
            req_headers = Http.DEFAULT_HEADERS.copy()
            if headers:
                req_headers.update(headers)
            
            if isinstance(data, dict):
                data = urllib.parse.urlencode(data).encode('utf-8')
            elif isinstance(data, str):
                data = data.encode('utf-8')
            
            request = urllib.request.Request(url, data=data, headers=req_headers)
            with urllib.request.urlopen(request, timeout=30) as response:
                return response.read().decode('utf-8')
        except Exception as e:
            print(f'HTTP POST 请求失败: {e}')
            return ''
    
    @staticmethod
    def post_json(url: str, data: Any, headers: Dict[str, str] = None) -> str:
        """POST JSON 请求"""
        req_headers = headers.copy() if headers else {}
        req_headers['Content-Type'] = 'application/json'
        return Http.post(url, json.dumps(data), req_headers)


class Crypto:
    """
    加解密工具类
    """
    
    @staticmethod
    def md5(data: str) -> str:
        """MD5 加密"""
        return hashlib.md5(data.encode('utf-8')).hexdigest()
    
    @staticmethod
    def base64_encode(data: str) -> str:
        """Base64 编码"""
        return base64.b64encode(data.encode('utf-8')).decode('utf-8')
    
    @staticmethod
    def base64_decode(data: str) -> str:
        """Base64 解码"""
        return base64.b64decode(data).decode('utf-8')
