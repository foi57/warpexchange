package com.itranswarp.exchange.ctx;

import com.itranswarp.exchange.ApiError;
import com.itranswarp.exchange.ApiException;


public class UserContext implements AutoCloseable{
    static final ThreadLocal<Long> THREAD_LOCAL_CTX = new ThreadLocal<>();

    public static Long getRequireUserId(){
        Long userId = getUserId();
        if (userId == null){
            throw new ApiException(ApiError.AUTH_SIGNIN_REQUIRED,null,"Need sign in.");
        }
        return userId;
    }

    public static Long getUserId(){
        return THREAD_LOCAL_CTX.get();
    }

    public UserContext(Long userId){
        THREAD_LOCAL_CTX.set(userId);
    }

    @Override
    public void close(){
        THREAD_LOCAL_CTX.remove();
    }
}
