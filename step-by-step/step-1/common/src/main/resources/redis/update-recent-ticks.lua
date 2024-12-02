---
--- Created by lenovo.
--- DateTime: 2024/11/28 下午11:10
---

local KEY_LAST_SEQ = '_TickSeq_'
local LIST_RECENT_TICKS = KEY[1]

local seqId = ARGV[1]
local jsonData = ARGV[2]
local strData = ARGV[3]

local lastSeqId = redis.call('GET',KEY_LAST_SEQ)
local ticks, len;

if not lastSeqId or tonumber(seqId) > tonumber(lastSeqId) then
    redis.call('PUBLISH','notification','{"type":"tick","sequencedId":'..seqId..',"data":'..jsonData..'}')

    redis.call('SET',KEY_LAST_SEQ,seqId)
    ticks = cjson.decode(strData)
    len = redis.call('RPUSH',LIST_RECENT_TICKS,unpack(ticks))
    if len > 100 then
        redis.call('LTRIM',LIST_RECENT_TICKS,len-100,len-1)
    end
end