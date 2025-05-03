package org.visage.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class ObjectMapperUtil {

    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(NON_NULL);

    public static <T> T readValue(String content, Class<T> valueType) {

        try {
            return JACKSON_MAPPER.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String writeValueAsString(Object o) {

        try {
            return JACKSON_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String content, TypeReference<T> valueTypeRef) {

        try {
            return JACKSON_MAPPER.readValue(content, valueTypeRef);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectNode createObjectNode() {

        return JACKSON_MAPPER.createObjectNode();
    }

}
