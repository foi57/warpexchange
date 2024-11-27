local KEY_LAST_SEQ = '_OBLastSeqId_'
local key = KEYS[1]
local seqId = ARGC[1]
local data = ARGC[2]

local lastSeqId = redis.call('GET',KEY_LAST_SEQ)

if not lastSeqId or tonumber(seqId) > tonumber(lastSeqId) then
    redis.call('SET',KEY_LAST_SEQ,seqId)
    redis.call('SET',key,data)
    redis.call("PUBLISH","notification",'{"type":}')
    return true
end

return false