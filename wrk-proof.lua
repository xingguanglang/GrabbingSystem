math.randomseed(os.time())
threads = {}

function setup(thread)
    table.insert(threads, thread)
end

function init(args)
    ok = 0
    http_non_200 = 0
    c41003 = 0
    c41004 = 0
    c41005 = 0
    c_other = 0
end

request = function()
    local uid = math.random(2000, 999999999)
    local path = "/api/seckill/order/create?userId=" .. uid .. "&promoId=1&skuId=1"
    return wrk.format("POST", path)
end

response = function(status, headers, body)
    if status ~= 200 then
        http_non_200 = http_non_200 + 1
        return
    end
    local code = body:match('"code"%s*:%s*(%d+)')
    if not code then
        ok = ok + 1
    elseif code == "41003" then
        c41003 = c41003 + 1
    elseif code == "41004" then
        c41004 = c41004 + 1
    elseif code == "41005" then
        c41005 = c41005 + 1
    else
        c_other = c_other + 1
    end
end

done = function(summary, latency, requests)
    local a,b,d,e,f,g = 0,0,0,0,0,0
    for _, t in ipairs(threads) do
        a = a + t:get("ok")
        b = b + t:get("http_non_200")
        d = d + t:get("c41003")
        e = e + t:get("c41004")
        f = f + t:get("c41005")
        g = g + t:get("c_other")
    end
    io.write("\n=== BUSINESS COUNTS ===\n")
    io.write("ok - 成功=" .. a .. "\n")
    io.write("http_non_200=" .. b .. "\n")
    io.write("code_41003限流=" .. d .. "\n")
    io.write("code_41004重复=" .. e .. "\n")
    io.write("code_41005售罄=" .. f .. "\n")
    io.write("code_other=" .. g .. "\n")
end
