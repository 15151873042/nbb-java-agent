package com.nbb.feign;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 胡鹏
 */
public class FixedUrlFeignClientsRegistrar {

    private static final Map<String, String> SERVICE_NAME_2_URL = new HashMap<String, String>() {
        {
            put("platform-system-server", "http://100.104.226.6:21110");
        }
    };

    public static String getUrl(ConfigurableBeanFactory beanFactory, Map<String, Object> attributes) {
        String serviceName = (String)attributes.get("name");
        String url = SERVICE_NAME_2_URL.getOrDefault(serviceName, "");
        System.out.println("服务名为【"+ serviceName +"】匹配到替换url【" + url + "】");
        return url;
    }

//    public static String getUrl(String url) {
//        if (StringUtils.hasText(url) && (!url.startsWith("#{") || !url.contains("}"))) {
//            if (!url.contains("://"))
//                url = "http://" + url;
//            try {
//                new URL(url);
//            } catch (MalformedURLException e) {
//                throw new IllegalArgumentException(url + " is malformed", e);
//            }
//        }
//        return url;
//    }
}
