package com.itranswarp.exchange.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;

public class JsonUtil
{
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapperForInternal();

    private static ObjectMapper createObjectMapperForInternal(){
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        return mapper;
    }

    public static String writeJson(Object obj){
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e){
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readJson(String str,Class<T> clazz){
        try {
            return OBJECT_MAPPER.readValue(str,clazz);
        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }

    public static <T> T readJson(String str, TypeReference<T> ref){
        try {
            return OBJECT_MAPPER.readValue(str,ref);
        }catch (JsonProcessingException e){
            logger.warn("cannot read json: " + str, e);
            throw new UncheckedIOException(e);
        }
    }
}
