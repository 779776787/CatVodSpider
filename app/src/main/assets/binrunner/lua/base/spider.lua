--[[
Lua Spider 基类
所有 Lua 爬虫脚本应继承此类
]]

-- JSON 编解码（简单实现，建议使用 cjson 或 dkjson）
local json = {}

function json.encode(obj)
    if type(obj) == "nil" then
        return "null"
    elseif type(obj) == "boolean" then
        return obj and "true" or "false"
    elseif type(obj) == "number" then
        return tostring(obj)
    elseif type(obj) == "string" then
        return '"' .. obj:gsub('"', '\\"'):gsub('\n', '\\n'):gsub('\r', '\\r') .. '"'
    elseif type(obj) == "table" then
        local isArray = #obj > 0 or next(obj) == nil
        local parts = {}
        if isArray then
            for _, v in ipairs(obj) do
                table.insert(parts, json.encode(v))
            end
            return "[" .. table.concat(parts, ",") .. "]"
        else
            for k, v in pairs(obj) do
                table.insert(parts, '"' .. tostring(k) .. '":' .. json.encode(v))
            end
            return "{" .. table.concat(parts, ",") .. "}"
        end
    end
    return "null"
end

-- 爬虫基类
local Spider = {}
Spider.__index = Spider

function Spider:new()
    local obj = {
        siteKey = "",
        siteType = 0
    }
    setmetatable(obj, Spider)
    return obj
end

-- 初始化
function Spider:init(extend)
    return ""
end

-- 首页内容
function Spider:homeContent(filter)
    return ""
end

-- 首页视频内容
function Spider:homeVideoContent()
    return ""
end

-- 分类内容
function Spider:categoryContent(tid, pg, filter, extend)
    return ""
end

-- 详情内容
function Spider:detailContent(ids)
    return ""
end

-- 搜索内容
function Spider:searchContent(key, quick, pg)
    return ""
end

-- 播放内容
function Spider:playerContent(flag, id, vipFlags)
    return ""
end

-- 直播内容
function Spider:liveContent(url)
    return ""
end

-- 代理请求
function Spider:proxy(params)
    return nil
end

-- 动作
function Spider:action(action)
    return ""
end

-- 销毁
function Spider:destroy()
end

-- 结果构建器
local Result = {}

-- 构建分类结果
function Result.classes(classes, vods)
    return json.encode({
        class = classes or {},
        list = vods or {}
    })
end

-- 构建列表结果
function Result.list(vods, page, pagecount, limit, total)
    return json.encode({
        list = vods or {},
        page = page or 1,
        pagecount = pagecount or 1,
        limit = limit or 20,
        total = total or 0
    })
end

-- 构建详情结果
function Result.detail(vod)
    return json.encode({
        list = {vod}
    })
end

-- 构建播放结果
function Result.player(url, parse, header)
    local result = {
        parse = parse or 0,
        url = url
    }
    if header then
        result.header = header
    end
    return json.encode(result)
end

-- 构建错误结果
function Result.error(msg)
    return json.encode({error = msg})
end

-- HTTP 请求工具类
local Http = {}

-- GET 请求
function Http.get(url, headers)
    -- 使用 curl 命令
    local cmd = string.format('curl -s "%s"', url)
    local handle = io.popen(cmd)
    local result = handle:read("*a")
    handle:close()
    return result or ""
end

-- POST 请求
function Http.post(url, data, headers)
    local cmd
    if type(data) == "table" then
        local parts = {}
        for k, v in pairs(data) do
            table.insert(parts, k .. "=" .. v)
        end
        data = table.concat(parts, "&")
    end
    cmd = string.format('curl -s -X POST -d "%s" "%s"', data or "", url)
    local handle = io.popen(cmd)
    local result = handle:read("*a")
    handle:close()
    return result or ""
end

-- 加解密工具类
local Crypto = {}

-- MD5 加密（需要外部库支持）
function Crypto.md5(data)
    -- 使用 md5sum 命令
    local handle = io.popen('echo -n "' .. data .. '" | md5sum')
    local result = handle:read("*a")
    handle:close()
    return result:match("^%x+") or ""
end

-- Base64 编码
function Crypto.base64Encode(data)
    local handle = io.popen('echo -n "' .. data .. '" | base64')
    local result = handle:read("*a")
    handle:close()
    return result:gsub("%s+", "")
end

-- Base64 解码
function Crypto.base64Decode(data)
    local handle = io.popen('echo "' .. data .. '" | base64 -d')
    local result = handle:read("*a")
    handle:close()
    return result
end

-- 导出
return {
    Spider = Spider,
    Result = Result,
    Http = Http,
    Crypto = Crypto,
    json = json
}
