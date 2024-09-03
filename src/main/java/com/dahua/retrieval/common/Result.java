package com.dahua.retrieval.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result<T> {
    private int code;
    private String message;
    private T data;

    // 构造函数、getter 和 setter 省略

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 可以添加一个通用的成功响应构造函数
    public static <T> Result<T> success(T data) {
        return new Result<T>(200, "Success", data);
    }

    // 可以添加一个通用的错误响应构造函数
    public static <T> Result<T> error(int code, String message) {
        return new Result<T>(code, message, null);
    }
}