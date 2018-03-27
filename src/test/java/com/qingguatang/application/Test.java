package com.qingguatang.application;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author joe
 * @date 2018/3/16
 */
public class Test {

  public static void main(String[] args) throws UnsupportedEncodingException {

    String target = URLEncoder.encode("https://www.unreach.io");

    String url = URLEncoder.encode(
        "https://member.unreach.io/api/user/login?bizType=bmatch&loginType=weixin&target=" + target,
        "utf-8");
    System.out.println(url);

  }

}
