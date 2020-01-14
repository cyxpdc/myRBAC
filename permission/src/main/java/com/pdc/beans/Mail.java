package com.pdc.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mail {
    /**
     * 主题
     */
    private String subject;
    /**
     * 信息
     */
    private String message;
    /**
     * 收件人
     */
    private Set<String> receivers;
}
