package com.alipay.sdk.pay.demo.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AliPayAppUtil {

  public static String getOrderPayInfo(
      String appId,
      String rsaKey,
      String payId,
      String totalAmount,
      String subject,
      String body,
      String timeoutExpress,
      String notify_url) {
    Map<String, String> params =
        AliPayAppUtil.buildOrderParamMap(
            appId, payId, totalAmount, subject, body, timeoutExpress, notify_url);
    String string =
        AliPayAppUtil.buildOrderParam(params) + "&" + AliPayAppUtil.getSign(params, rsaKey, true);
    Log.i("msp","order byAll info by local : " + string);
    return string;
  }

  /** 构造支付订单参数列表 */
  private static Map<String, String> buildOrderParamMap(
      String appId,
      String payId,
      String totalAmount,
      String subject,
      String body,
      String timeoutExpress,
      String notify_url) {
    Map<String, String> keyValues = new HashMap<>();
    keyValues.put("app_id", appId);
    keyValues.put("timestamp", getNowDateTime());
    keyValues.put(
        "biz_content",
        "{\"timeout_express\":\""
            + timeoutExpress
            + "\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\""
            + totalAmount
            + "\",\"subject\":\""
            + subject
            + "\",\"body\":\""
            + body
            + "\",\"out_trade_no\":\""
            + payId
            + "\"}");
    keyValues.put("charset", "utf-8");
    keyValues.put("method", "alipay.trade.app.byAll");
    keyValues.put("sign_type", "RSA2");
    keyValues.put("version", "1.0");
    keyValues.put("notify_url", notify_url);
    return keyValues;
  }

  /** 获取时间格式 */
  private static String getNowDateTime() {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    return df.format(new Date());
  }

  /** 构造支付订单参数信息 */
  private static String buildOrderParam(Map<String, String> map) {
    List<String> keys = new ArrayList<>(map.keySet());

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < keys.size() - 1; i++) {
      String key = keys.get(i);
      String value = map.get(key);
      sb.append(buildKeyValue(key, value, true));
      sb.append("&");
    }

    String tailKey = keys.get(keys.size() - 1);
    String tailValue = map.get(tailKey);
    sb.append(buildKeyValue(tailKey, tailValue, true));

    return sb.toString();
  }

  /** 拼接键值对 */
  private static String buildKeyValue(String key, String value, boolean isEncode) {
    StringBuilder sb = new StringBuilder();
    sb.append(key);
    sb.append("=");
    if (isEncode) {
      try {
        sb.append(URLEncoder.encode(value, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        sb.append(value);
      }
    } else {
      sb.append(value);
    }
    return sb.toString();
  }

  /** 对支付参数信息进行签名 */
  private static String getSign(Map<String, String> map, String rsaKey, boolean rsa2) {
    List<String> keys = new ArrayList<>(map.keySet());
    // key排序
    Collections.sort(keys);

    StringBuilder authInfo = new StringBuilder();
    for (int i = 0; i < keys.size() - 1; i++) {
      String key = keys.get(i);
      String value = map.get(key);
      authInfo.append(buildKeyValue(key, value, false));
      authInfo.append("&");
    }

    String tailKey = keys.get(keys.size() - 1);
    String tailValue = map.get(tailKey);
    authInfo.append(buildKeyValue(tailKey, tailValue, false));

    String oriSign = sign(authInfo.toString(), rsaKey, rsa2);
    String encodedSign = "";

    try {
      encodedSign = URLEncoder.encode(oriSign, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return "sign=" + encodedSign;
  }

  /** 要求外部订单号必须唯一。 */
  public static String getOutTradeNo() {
    SimpleDateFormat df = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
    String key = df.format(new Date());

    Random r = new Random();
    key = key + r.nextInt();
    key = key.substring(0, 15);
    return key;
  }

  // ---------------------------------------------
  private static final String ALGORITHM = "RSA";

  private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

  private static final String SIGN_SHA256RSA_ALGORITHMS = "SHA256WithRSA";

  private static final String DEFAULT_CHARSET = "UTF-8";

  private static String getAlgorithms(boolean rsa2) {
    return rsa2 ? SIGN_SHA256RSA_ALGORITHMS : SIGN_ALGORITHMS;
  }

  private static String sign(String content, String privateKey, boolean rsa2) {
    try {
      PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKey));
      KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
      PrivateKey priKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

      java.security.Signature signature = java.security.Signature.getInstance(getAlgorithms(rsa2));

      signature.initSign(priKey);
      signature.update(content.getBytes(DEFAULT_CHARSET));

      byte[] signed = signature.sign();

      return Base64.encode(signed);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
