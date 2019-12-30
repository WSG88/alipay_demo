package com.alipay.sdk.pay.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.pay.demo.util.AliPayAppUtil;
import com.alipay.sdk.pay.demo.util.AliPayResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class PayDemoActivity extends AppCompatActivity {

  public static String APPID = "";
  public static String RSA2_PRIVATE = "";
  public static final int SDK_PAY_OK = 1;
  public static final int SDK_PAY_FAIL = 2;

  Handler mHandler =
      new Handler() {
        public void handleMessage(Message msg) {
          switch (msg.what) {
            case SDK_PAY_OK:
              {
                showAlert(PayDemoActivity.this, getString(R.string.pay_success) + msg.obj);
                break;
              }
            case SDK_PAY_FAIL:
              {
                showAlert(PayDemoActivity.this, getString(R.string.pay_failed) + msg.obj);
                break;
              }
            default:
              break;
          }
        };
      };

  Runnable payRunnable =
      new Runnable() {

        @Override
        public void run() {
          String orderInfo =
              AliPayAppUtil.getOrderPayInfo(
                  APPID,
                  RSA2_PRIVATE,
                  AliPayAppUtil.getOutTradeNo(),
                  "0.09",
                  AliPayAppUtil.getOutTradeNo(),
                  AliPayAppUtil.getOutTradeNo(),
                  "15m",
                  "https://lcatcallback3.hhotel.com/cts/trade/order/unified/alipay/callback");
          Log.i("msp", orderInfo);

          Map<String, String> result = new PayTask(PayDemoActivity.this).payV2(orderInfo, true);
          Log.i("msp", result.toString());

          AliPayResult aliPayResult = new AliPayResult(result);
          if ("9000".equals(aliPayResult.getResultStatus())) {
            Message msg = new Message();
            msg.what = SDK_PAY_OK;
            msg.obj = aliPayResult.toString();
            mHandler.sendMessage(msg);
          } else {
            Message msg = new Message();
            msg.what = SDK_PAY_FAIL;
            msg.obj = aliPayResult.toString();
            mHandler.sendMessage(msg);
          }
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pay_main);

    EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
    APPID = "2016092700604419";
    RSA2_PRIVATE =
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDO81VRPqGmULqboijJYLpnrHo2Yml0NWNmIQkkaYaVFV2wmtg77XHI5IZQjF7WM9FRDnuPgkWt98iARwiK0gCi5jo1+LzVz/+ZDmcJMA8d4VGBleDtwECHgTgZfbBksZQvOxqa8mviLa6JeBCe6u3krNBUQeT4HZyHuDoZBPqnL2Tuv3gBZTiYHPxjHfll7BPa0TypVL1HSvRy5JSbFWcng5FkdB0sm9+G8zZscNZNjPCG9m5bOD2GYvEQpj5LrJwqioy7IpXRa7Q//w/65MxZ6dkcuPUY1KJ7UBpcAYGKtEFOSQVe0gD6BP8kZYM/djlCKyBKRe13YUvobJFZvgrrAgMBAAECggEAQUyohA02JmtcplUBd6FMz/+tRFHDIobiCUytPM8LRylJubryNd6TQ6dP9kSEfCQ3jNL8C+k4IU+I9Wj0ehEPYFcy0OrecrzB0sKnok+GxY/eDvyCz6IU+my8jdaJ/QvHTSviFjgd8G0noHrBC9PnInug9KUiIWP+ixiHRHO8v1l0h5ybIvoeqDDcqT6dnGAfxpwQLbdzvPupLjAuxBt0dmYHDJXJ8nyIQbHwfRmcgtCYjcH2KY4Rr2dTkzT5rlm15Wtkw1TtGLDGCHIhNrBfoM3lwPmijylo28tP65GQiUTIKz3YxrFsvTuFAvpHWZf1hTouEk9EsU++gojcodWc0QKBgQD0B9ZmDIw7k43dG9MQqF8wG4/8R3QdPrfgD0Iu5k/x6YHo3emFCN7ZpIAqzmVU43XTGeB9YiGVyCM/BPPE3tTvpWDLOxmvl1Ayto74rgLGMjm83rV2RztMAxWADjoNX4vTM24UGfHRyueqqf71jUUHnhTacTBxdIIh+vwFwi1wYwKBgQDZGeaY6a2Nb4aU/u4GpmD6GIO7r9ye8E4sEs2BBkn5x/wRy4XrqX7Ew7VJdBnrmi8jr5VHrJEQFfvRXBi4P4ckDplbTx0vMp6Mc/tXmk+LHGQhDz0BQP3yicGoElA5N+4KvrzVtINxS8INJwg0JJCA/hqD8ERURw3DYgbxvaRN2QKBgQDxz7ZfBv4KpwtDV6gNcJpqRFWoSnG/7P8tslEFwkjj0VxekU8t+X1CM2ShhkYZGRAPMCwoco4PKvPqJZEcuyZLlhfVKuon+guJRSdlOqEbHnfiNNmhq7IRf1jVvMM/rzcSAHuKxa6/EY4AqlUvo14Mz1Ogpdq4AhhVbaxAVIrMOwKBgHf1K8mKNO9VlFN3mSXrgOIv8XofbhQUS91mdk64nt95dPqQLmtMuFMYoG75y+i2xR0+w4la/LyaqQ3BiZck4xM1Td96E3Ik0h5hZ5u8E/+VrG3n8Zljw+3j2DF6DQHQuaMOSWD74TUU2yBRVOMa4Q2lWhlVBSxcJmqzG4svcj3hAoGAUQ4mk5R2YgY1ZM/rcfrDOrm/B/EAQsMl/8BywXGu8ZMB0B58HyLjZaWMgzf1bx7Wmqc8sVLVL2oRt3ZJ4uYSb3WA9AYN8y1VSyndXkqEbCxp5MLbLH3u+jmVyWAO9tNez+mnWUYMyYIUAAWEAxoAT/AVsbsmnJ6gGlEJsoyp3Yo=";
  }

  public void h5Pay(View v) {
    Intent intent = new Intent();
    String url = "https://lcatcallback3.hhotel.com/cts/trade/order/unified/test/alipay/web";
    intent.setData(Uri.parse(url));
    intent.setAction(Intent.ACTION_VIEW);
    startActivity(intent);
  }

  public void appLocal(View v) {
    Thread payThread = new Thread(payRunnable);
    payThread.start();
  }

  private static void showAlert(Context ctx, String info) {
    new AlertDialog.Builder(ctx)
        .setMessage(info)
        .setPositiveButton(R.string.confirm, null)
        .setOnDismissListener(null)
        .show();
  }

  public void appNet(View v) {
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                try {
                  URL url =
                      new URL(
                          "https://lcatcallback3.hhotel.com/cts/trade/order/unified/test/alipay/app");
                  HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

                  httpConn.setDoOutput(true);
                  httpConn.setDoInput(true);
                  httpConn.setUseCaches(false);
                  httpConn.setRequestMethod("POST");
                  httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                  httpConn.setRequestProperty("Connection", "Keep-Alive"); // 维持长连接
                  httpConn.setRequestProperty("Charset", "UTF-8");
                  httpConn.connect();
                  int resultCode = httpConn.getResponseCode();
                  if (HttpURLConnection.HTTP_OK == resultCode) {
                    StringBuffer sb = new StringBuffer();
                    String readLine = new String();
                    BufferedReader responseReader =
                        new BufferedReader(
                            new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                    while ((readLine = responseReader.readLine()) != null) {
                      sb.append(readLine).append("\n");
                    }
                    responseReader.close();
                    Log.i("msp", sb.toString());

                    String orderInfo = sb.toString().replace("\"", "");
                    Map<String, String> result =
                        new PayTask(PayDemoActivity.this).payV2(orderInfo, true);
                    Log.i("msp", result.toString());

                    AliPayResult aliPayResult = new AliPayResult(result);
                    if ("9000".equals(aliPayResult.getResultStatus())) {
                      Message msg = new Message();
                      msg.what = SDK_PAY_OK;
                      msg.obj = aliPayResult.toString();
                      mHandler.sendMessage(msg);
                    } else {
                      Message msg = new Message();
                      msg.what = SDK_PAY_FAIL;
                      msg.obj = aliPayResult.toString();
                      mHandler.sendMessage(msg);
                    }
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            })
        .start();
  }
}
