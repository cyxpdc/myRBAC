package com.pdc.util;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TokenUtils {

	@RequestMapping("/getToken")
	public static String getAccessToken(String appId,String appSecret) {
		return UUID.randomUUID().toString().replace("-", "") + appId + appSecret;
	}

}
