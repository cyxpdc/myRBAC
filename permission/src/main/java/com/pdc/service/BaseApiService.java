package com.pdc.service;

import com.pdc.beans.BaseApiConstants;
import com.pdc.beans.ResponseBase;
import org.springframework.stereotype.Component;

/**
 * @author PDC
 */
@Component
public class BaseApiService {

    public static ResponseBase setResultError(Integer code, String msg) {
        return setResult(code, msg, null);
    }

    // 返回错误，可以传msg
    public static ResponseBase setResultError(String msg) {
        return setResult(BaseApiConstants.HTTP_RES_CODE_500, msg, null);
    }

    // 返回成功，可以传data值
    public static ResponseBase setResultSuccessData(Object data) {
        return setResult(BaseApiConstants.HTTP_RES_CODE_200, BaseApiConstants.HTTP_RES_CODE_200_VALUE, data);
    }

    public static ResponseBase setResultSuccessData(Integer code, Object data) {
        return setResult(code, BaseApiConstants.HTTP_RES_CODE_200_VALUE, data);
    }

    // 返回成功，沒有data值
    public static ResponseBase setResultSuccess() {
        return setResult(BaseApiConstants.HTTP_RES_CODE_200, BaseApiConstants.HTTP_RES_CODE_200_VALUE, null);
    }

    // 返回成功，沒有data值
    public static ResponseBase setResultSuccess(String msg) {
        return setResult(BaseApiConstants.HTTP_RES_CODE_200, msg, null);
    }

    // 通用封装
    public static ResponseBase setResult(Integer code, String msg, Object data) {
        return new ResponseBase(code, msg, data);
    }
}