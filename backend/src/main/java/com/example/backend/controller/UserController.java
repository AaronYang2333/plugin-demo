package com.example.backend.controller;

import com.example.backend.StandardResponse;
import com.example.backend.controller.vo.UserInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/who-am-i")
    public StandardResponse<UserInfo> whoAmI() {
        return StandardResponse.<UserInfo>builder()
                .data(UserInfo.builder()
                        .name(RandomStringUtils.randomAlphabetic(8))
                        .age(Double.valueOf(Math.random() * 100).intValue())
                        .build())
                .build();
    }
}
