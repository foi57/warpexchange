package com.itranswarp.exchange.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itranswarp.exchange.ApiError;
import com.itranswarp.exchange.ApiErrorResponse;
import com.itranswarp.exchange.ApiException;
import com.itranswarp.exchange.bean.OrderBookBean;
import com.itranswarp.exchange.ctx.UserContext;
import com.itranswarp.exchange.message.ApiResultMessage;
import com.itranswarp.exchange.model.trade.OrderEntity;
import com.itranswarp.exchange.redis.RedisCache;
import com.itranswarp.exchange.redis.RedisService;
import com.itranswarp.exchange.service.HistoryService;
import com.itranswarp.exchange.service.TradingEngineApiProxyService;
import com.itranswarp.exchange.support.AbstractApiController;
import com.itranswarp.exchange.util.JsonUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.View;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@RestController
@RequestMapping("/api")
public class TradingApiController extends AbstractApiController {

    @Autowired
    ObjectMapper objectMapper;
    private String timeoutJson = null;

    @Autowired
    private RedisService redisService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    TradingEngineApiProxyService tradingEngineApiProxyService;

    Map<String,DeferredResult<ResponseEntity<String>>> deferredResultMap = new java.util.concurrent.ConcurrentHashMap<>();
    @Autowired
    private View error;

    private String getTimeoutJson() throws IOException{
        if (timeoutJson == null){
            timeoutJson = this.objectMapper.writeValueAsString(new ApiErrorResponse(ApiError.OPERATION_TIMEOUT,null,""));
        }
        return timeoutJson;
    }

    @PostConstruct
    public void init() {
        this.redisService.subscribe(RedisCache.Topic.TRADING_API_RESULT,this::onApiResultMessage);
    }

    @GetMapping("timestamp")
    public Map<String,Long> timestamp() {
        return Map.of("timestamp",Long.valueOf(System.currentTimeMillis()));
    }

    @ResponseBody
    @GetMapping(value = "/assets",produces = "application/json")
    public String getAssets() throws IOException{
        return tradingEngineApiProxyService.get("/internal/" + UserContext.getRequireUserId() + "/assets");
    }

    @ResponseBody
    @GetMapping(value = "/orders/{orderId}", produces = "application/json")
    public String getOpenOrder(@PathVariable("orderId") Long orderId) throws IOException{
        final Long userId = UserContext.getRequireUserId();
        return tradingEngineApiProxyService.get("/internal/" + userId + "/orders" + orderId);
    }

    @ResponseBody
    @GetMapping(value = "/orders",produces = "application/json")
    public String getOpenOrders() throws IOException{
        return tradingEngineApiProxyService.get("/internal/" + UserContext.getRequireUserId() + "/orders");
    }

    @ResponseBody
    @GetMapping(value = "/orderBook",produces = "application/json")
    public String getOrderBook(){
        String data = redisService.get(RedisCache.Key.ORDER_BOOK);
        return data == null ? OrderBookBean.EMPTY : data;
    }

    @ResponseBody
    @GetMapping(value = "ticks",produces = "application/json")
    public String getRecentTicks(){
        List<String> data = redisService.lrange(RedisCache.Key.RECENT_TICKS,0,-1);
        if (data == null || data.isEmpty()){
            return "[]";
        }
        StringJoiner sj = new StringJoiner(",","[","]");
        for (String t : data){
            sj.add(t);
        }
        return sj.toString();
    }

    @ResponseBody
    @GetMapping(value = "/bars/day", produces = "application/json")
    public String getDayBars() {
        long end = System.currentTimeMillis();
        long start = end - 366 * 86400_000;
        return getBars(RedisCache.Key.HOUR_BARS, start, end);
    }

    @ResponseBody
    @GetMapping(value = "/bars/hour",produces = "application/json")
    public String getHourBars(){
        long end = System.currentTimeMillis();
        long start = end - 720 * 3600_000;
        return getBars(RedisCache.Key.HOUR_BARS,start,end);
    }

    @ResponseBody
    @GetMapping(value = "/bars/min",produces = "application/json")
    public String getMinBars(){
        long end = System.currentTimeMillis();
        long start = end - 1440 * 60_000;
        return getBars(RedisCache.Key.MIN_BARS,start,end);
    }

    @ResponseBody
    @GetMapping(value = "/bars/sec", produces = "application/json")
    public String getSecBars() {
        long end = System.currentTimeMillis();
        long start = end - 3600 * 1_000;
        return getBars(RedisCache.Key.SEC_BARS, start, end);
    }


    private String getBars(String key,long start,long end){
        List<String> data = redisService.zrangebyscore(key,start,end);
        if (data == null || data.isEmpty()){
            return "[]";
        }
        StringJoiner sj = new StringJoiner(",","[","]");
        for (String t : data){
            sj.add(t);
        }
        return sj.toString();
    }

    @GetMapping("/history/orders")
    public List<OrderEntity> getHistoryOrders(@RequestParam(value = "maxResults",defaultValue = "100") int maxResults){
        if (maxResults < 1 || maxResults > 1000){
            throw new ApiException(ApiError.PARAMETER_INVALID, "maxResults", "Invalid parameter.");
        }
        return historyService.getHistoryOrders(UserContext.getRequireUserId(),maxResults);
    }

    public void onApiResultMessage(String msg){
        logger.info("on subscribed message: {}", msg);
        try {
            ApiResultMessage message = objectMapper.readValue(msg,ApiResultMessage.class);
            if (message.refId !=null){
                DeferredResult<ResponseEntity<String>> deferred = this.deferredResultMap.remove(message.refId);
                if (deferred != null){
                    if (message.error !=null) {
                        String error = objectMapper.writeValueAsString(message.error);
                        ResponseEntity<String> resp = new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                        deferred.setResult(resp);
                    }else {
                        ResponseEntity<String> resp = new ResponseEntity<>(JsonUtil.writeJson(message.result),HttpStatus.OK);
                        deferred.setResult(resp);
                    }
                }
            }
        }catch (Exception e){
            logger.error("Invalid ApiResultMessage: " + msg, e);
        }
    }

}
