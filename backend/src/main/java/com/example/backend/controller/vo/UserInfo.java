package com.example.backend.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/30
 */
@Builder
@Getter
@EqualsAndHashCode
@ToString
@Schema(description = "user info")
@AllArgsConstructor
public class UserInfo {

    private String name;
    private Integer age;
}
