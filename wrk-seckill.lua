math.randomseed(os.time())

request = function()
    local uid = math.random(1,9999999)
    local path = "/api/seckill/order/create?userId=" .. uid .. "&promoId=1&skuId=1"
    return wrk.format("POST", path)
end