package com.windcore.convert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

public class YamlHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    private final ObjectMapper yamlMapper = new YAMLMapper();

    public YamlHttpMessageConverter() {
        // 设置这个转换器支持的媒体类型是 application/yaml
        super(MediaType.APPLICATION_YAML, MediaType.APPLICATION_YAML);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        // 这里简单返回 true，表示支持所有类型的转换（实际项目里可以根据需要判断）
        return true;
    }

    /**
     * 读取请求体里的 YAML 数据，转换成 Java 对象
     * @param clazz
     * @param inputMessage
     * @return
     * @throws IOException
     * @throws HttpMessageNotReadableException
     */
    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        // 从请求体里读取 YAML 数据，转成 Java 对象
        return yamlMapper.readValue(inputMessage.getBody(), clazz);
    }

    /**
     * 写回 Java 对象为 YAML 数据
     * @param object
     * @param outputMessage
     * @throws IOException
     * @throws HttpMessageNotWritableException
     */
    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        // 把 Java 对象转成 YAML 数据，写回响应体
        byte[] yamlBytes = yamlMapper.writeValueAsBytes(object);
        outputMessage.getBody().write(yamlBytes);
    }
}
