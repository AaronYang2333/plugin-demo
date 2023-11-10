package com.example.backend.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
@Getter
@AllArgsConstructor
public enum  RuntimeMode {

    DEV("dev"),

    TEST("test"),

    PROD("prod");

    private final String mode;

    public static RuntimeMode byName(String model){
        if(DEV.name().equalsIgnoreCase(model)){
            return RuntimeMode.DEV;
        } else {
            return RuntimeMode.PROD;
        }
    }
}

