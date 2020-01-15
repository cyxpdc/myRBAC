package com.pdc.dao;

import com.pdc.model.AppEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author PDC
 */
public interface AppMapper {

    @Select("SELECT ID,APP_NAME AS appName,app_id,app_secret as appSecret,is_flag as isFlag,access_token as accessToken FROM m_app "
            + "WHERE app_id=#{appId} AND app_secret=#{appSecret}  ")
    AppEntity findApp(AppEntity appEntity);

    @Select("SELECT ID,APP_NAME AS appName,app_id,app_secret as appSecret,is_flag as isFlag,access_token as accessToken FROM m_app "
            + "WHERE app_id=#{appId}")
    AppEntity findAppId(@Param("appId") String appId);

    @Update("UPDATE m_app SET access_token =#{accessToken} WHERE app_id=#{appId} ")
    int updateAccessToken(@Param("accessToken") String accessToken, @Param("appId") String appId);
}

