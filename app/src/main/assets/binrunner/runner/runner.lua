#!/usr/bin/env lua
--[[
Lua Runner 入口脚本
加载并执行用户的爬虫脚本
]]

local json = require("dkjson") or require("cjson") or {
    encode = function(t)
        if type(t) ~= "table" then return tostring(t) end
        local result = {}
        for k, v in pairs(t) do
            local key = type(k) == "string" and '"'..k..'"' or tostring(k)
            local val
            if type(v) == "string" then
                val = '"'..v:gsub('"', '\\"')..'"'
            elseif type(v) == "table" then
                val = json.encode(v)
            else
                val = tostring(v)
            end
            table.insert(result, key..":"..val)
        end
        return "{"..table.concat(result, ",").."}"
    end,
    decode = function(s)
        if not s or s == "" then return {} end
        -- 简单的 JSON 解析
        local func, err = load("return "..s:gsub('("[^"]*"):', '[%1]='):gsub('%[', '{'):gsub('%]', '}'))
        if func then
            return func()
        end
        return {}
    end
}

-- 获取参数
local args = arg
if #args < 2 then
    io.stderr:write("用法: lua runner.lua <爬虫脚本路径> <方法名> [参数...]\n")
    os.exit(1)
end

local spiderPath = args[1]
local method = args[2]
local methodArgs = {}
for i = 3, #args do
    table.insert(methodArgs, args[i])
end

-- 加载爬虫脚本
local spider = nil
local function loadSpider()
    local file = io.open(spiderPath, "r")
    if not file then
        error("爬虫脚本不存在: " .. spiderPath)
    end
    local content = file:read("*all")
    file:close()
    
    -- 添加脚本目录到 package.path
    local spiderDir = spiderPath:match("(.*/)")
    if spiderDir then
        package.path = spiderDir .. "?.lua;" .. package.path
    end
    
    -- 执行脚本
    local func, err = load(content, spiderPath)
    if not func then
        error("编译爬虫脚本失败: " .. (err or "未知错误"))
    end
    
    local ok, result = pcall(func)
    if not ok then
        error("执行爬虫脚本失败: " .. (result or "未知错误"))
    end
    
    -- 获取 spider 对象
    if _G.spider then
        spider = _G.spider
    elseif type(result) == "table" then
        spider = result
    else
        error("爬虫脚本未返回 spider 对象")
    end
end

-- 调用方法
local function callMethod()
    if method == "init" then
        return spider:init(methodArgs[1] or "")
        
    elseif method == "homeContent" then
        local filter = methodArgs[1] and methodArgs[1]:lower() == "true"
        return spider:homeContent(filter)
        
    elseif method == "homeVideoContent" then
        return spider:homeVideoContent()
        
    elseif method == "categoryContent" then
        local tid = methodArgs[1] or ""
        local pg = methodArgs[2] or "1"
        local filter = methodArgs[3] and methodArgs[3]:lower() == "true"
        local extend = methodArgs[4] and json.decode(methodArgs[4]) or {}
        return spider:categoryContent(tid, pg, filter, extend)
        
    elseif method == "detailContent" then
        local ids = methodArgs[1] and json.decode(methodArgs[1]) or {}
        return spider:detailContent(ids)
        
    elseif method == "searchContent" then
        local key = methodArgs[1] or ""
        local quick = methodArgs[2] and methodArgs[2]:lower() == "true"
        local pg = methodArgs[3] or "1"
        return spider:searchContent(key, quick, pg)
        
    elseif method == "playerContent" then
        local flag = methodArgs[1] or ""
        local id = methodArgs[2] or ""
        local vipFlags = methodArgs[3] and json.decode(methodArgs[3]) or {}
        return spider:playerContent(flag, id, vipFlags)
        
    elseif method == "liveContent" then
        local url = methodArgs[1] or ""
        return spider:liveContent(url)
        
    elseif method == "proxy" then
        local params = methodArgs[1] and json.decode(methodArgs[1]) or {}
        return spider:proxy(params)
        
    elseif method == "action" then
        local action = methodArgs[1] or ""
        return spider:action(action)
        
    elseif method == "destroy" then
        if spider.destroy then
            spider:destroy()
        end
        return ""
        
    else
        error("未知方法: " .. method)
    end
end

-- 主函数
local function main()
    local ok, err = pcall(loadSpider)
    if not ok then
        io.stderr:write("加载爬虫脚本失败: " .. tostring(err) .. "\n")
        os.exit(1)
    end
    
    local ok, result = pcall(callMethod)
    if not ok then
        io.stderr:write("执行方法失败: " .. method .. " - " .. tostring(result) .. "\n")
        io.write(json.encode({error = tostring(result)}))
    else
        if type(result) == "string" then
            io.write(result)
        elseif result ~= nil then
            io.write(json.encode(result))
        end
    end
end

main()
