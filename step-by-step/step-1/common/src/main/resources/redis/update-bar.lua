---
--- Created by lenovo.
--- DateTime: 2024/11/28 下午11:25
---

local function merge(existBar,newBar)
    existBar[3] = math.max(existBar[3],newBar[3])
    existBar[4] = math.min(existBar[4],newBar[4])
    existBar[5] = newBar[5]
    existBar[6] = existBar[6] + newBar[6]
end

local function tryMergeLast(barType,seqId,zsetBars,timestamp,newBar)
    local topic = 'notification'
    local popedScore,popedBar
    local poped = redis.call('ZPOPMAX',zsetBars)
    if #poped==0 then
        redis.call('ZADD',zsetBars,timestamp,cjson.encode(newBar))
        redis.call('PUBLISH', topic, '{"type":"bar","resolution":"' .. barType .. '","sequenceId":' .. seqId .. ',"data":' .. cjson.encode(newBar) .. '}')
    else
        popedBar = cjson.decode(poped[1])
        popedScore = tonumber(poped[2])
        if popeadScore == timestamp then
            merge(popedBar,newBar)
            redis.call('ZADD',zsetBars,popedScore,cjson.encode(popedBar))
            redis.call('PUBLISH',topic,'{"type":"bar",""resolution":"' .. barType .. '","sequenceId":' .. seqId .. ',"data":' .. cjson.encode(popedBar) .. '}')
        end
            if popedScore < timestamp then
                redis.call('ZADD',zsetBars,popedScore,cjson.encode(popedBar))
                redis.call('PUBLISH',topic,'{"type":"bar",""resolution":"' .. barType .. '","sequenceId":' .. seqId .. ',"data":' .. cjson.encode(newBar) .. '}')
                return popedBar
            end
    end
    return nil
end

local seqId = ARGV[1]
local KEY_BAR_SEQ = '_BarSeq'

local zsetBars,topics,barTypeStartTimes
local openPrice,highPrice,lowPrice,closePrice,quantity
local persistBars = {}

local seq = redis.call('GET',KEY_BAR_SEQ)
if not seq or tonumber(seqId) > tonumber(seq) then
    zsetBars = {KEYS[1],KEYS[2],KEYS[3],KEYS[4]}
    barTypeStartTimes = { tonumber(ARGV[2]), tonumber(ARGV[3]), tonumber(ARGV[4]), tonumber(ARGV[5]) }
    openPrice = tonumber(ARGV[6])
    highPrice = tonumber(ARGV[7])
    lowPrice = tonumber(ARGV[8])
    closePrice = tonumber(ARGV[9])
    quantity = tonumber(ARGV[10])

    local i,bar
    local names = { 'SEC','MIN','HOUR','DAY' }

    for i=1,4 do
        bar = tryMergeLast(names[i],seqId,zsetBars[i],barTypeStartTimes[i],{ barTypeStartTimes[i],openPrice,highPrice,lowPrice,closePrice,quantity})
        if bar then
            persistBars[names[i]] = bar
        end
    end
    redis.call('SET',KEY_BAR_SEQ,seqId)
    return cjson.encode(persistBars)
end

redis.log(redis.LOG_WARNING,'sequence ignored: exist seq =>' .. seq .. ' >= '.. seqId .. ' <= seq')

return '{}'

