package com.backend.util;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dm20151123.AsyncClient;
import com.aliyun.sdk.service.dm20151123.models.SingleSendMailRequest;
import com.aliyun.sdk.service.dm20151123.models.SingleSendMailResponse;
import darabonba.core.client.ClientOverrideConfiguration;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

import com.backend.common.constant.EmailConstant;

/**
 * 邮件验证码工具类
 */
@Component
public class EmailUtil {
    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    @Value("${app.aliyun.mail.accessKeyId}")
    private String accessKeyId;

    @Value("${app.aliyun.mail.accessKeySecret}")
    private String accessKeySecret;

    @Value("${app.aliyun.mail.accountName}")
    private String accountName;

    @Value("${app.aliyun.mail.fromAlias}")
    private String fromAlias;

    private AsyncClient client;

    @PostConstruct
    public void init() {
        StaticCredentialProvider provider = StaticCredentialProvider.create(
                Credential.builder()
                        .accessKeyId(accessKeyId)
                        .accessKeySecret(accessKeySecret)
                        .build()
        );

        client = AsyncClient.builder()
                .region("cn-hangzhou")
                .credentialsProvider(provider)
                .overrideConfiguration(ClientOverrideConfiguration.create()
                        .setEndpointOverride("dm.aliyuncs.com"))
                .build();
    }

    public String sendVerificationCode(String email, String type) {
        try {
            String code = generateVerificationCode();
            sendVerificationCodeEmail(email, code, type);
            return code;
        } catch (Exception e) {
            logger.error("发送验证码邮件失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送验证码邮件失败: " + e.getMessage(), e);
        }
    }

    public boolean sendOrderNotification(String email, String orderId, String flightNumber,
                                    String seatNo, String departureTime, int status) {
        try {
            sendOrderNotificationEmail(email, orderId, flightNumber, seatNo, departureTime, status);
            return true;
        } catch (Exception e) {
            logger.error("发送订单通知邮件失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送订单通知邮件失败: " + e.getMessage(), e);
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < EmailConstant.CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void sendVerificationCodeEmail(String toEmail, String code, String type) throws Exception {
        SingleSendMailRequest request = SingleSendMailRequest.builder()
                .accountName(accountName)
                .fromAlias(fromAlias)
                .addressType(1)
                .toAddress(toEmail)
                .subject(getVerificationCodeEmailSubject(type))
                .htmlBody(getVerificationCodeEmailContent(code, type))
                .replyToAddress(true)
                .build();

        SingleSendMailResponse response = client.singleSendMail(request).get();
        logger.info("发送验证码邮件成功, RequestId: {}", response.getBody().getRequestId());
    }

    private String getVerificationCodeEmailSubject(String type) {
        return switch (type) {
            case "register" -> EmailConstant.APP_NAME + " - 注册验证码";
            case "reset" -> EmailConstant.APP_NAME + " - 重置密码验证码";
            default -> EmailConstant.APP_NAME + " - 验证码";
        };
    }

    private String getVerificationCodeEmailContent(String code, String type) {
        String purpose = switch (type) {
            case "register" -> "注册";
            case "reset" -> "重置密码";
            default -> "获取验证码";
        };

        return "<div style='background-color:#f7f7f7;padding:40px 0;text-align:center;'>"
                + "<div style='display:inline-block;background-color:#ffffff;padding:30px;border-radius:8px;box-shadow:0 2px 12px rgba(0,0,0,0.1);max-width:500px;width:90%;text-align:left;'>"
                + "<h2 style='color:#333333;text-align:center;'>" + EmailConstant.APP_NAME + "</h2>"
                + "<p style='color:#555;font-size:15px;margin-top:20px;'>您正在进行 <strong style='color:#409EFF;'>" + purpose + "</strong> 操作，验证码如下：</p>"
                + "<div style='background-color:#f2f6fc;padding:15px;margin:20px 0;text-align:center;border-radius:6px;'>"
                + "<span style='color:#409eff;font-size:26px;font-weight:bold;letter-spacing:6px;'>" + code + "</span>"
                + "</div>"
                + "<p style='color:#999;font-size:13px;'>验证码有效期为 <strong>" + EmailConstant.EXPIRE_MINUTES + "</strong> 分钟，请勿泄露给他人。</p>"
                + "<p style='color:#bbb;font-size:12px;margin-top:30px;text-align:center;'>如非本人操作，请忽略此邮件。</p>"
                + "</div>"
                + "</div>";
    }

    private void sendOrderNotificationEmail(String toEmail, String orderId, String flightNumber,
                                            String seatNo, String departureTime, int status) throws Exception {
        SingleSendMailRequest request = SingleSendMailRequest.builder()
                .accountName(accountName)
                .fromAlias(fromAlias)
                .addressType(1)
                .toAddress(toEmail)
                .subject(getOrderNotificationEmailSubject(status))
                .htmlBody(getOrderNotificationEmailContent(orderId, flightNumber, seatNo, departureTime, status))
                .replyToAddress(true)
                .build();

        SingleSendMailResponse response = client.singleSendMail(request).get();
        logger.info("发送订单通知邮件成功, RequestId: {}", response.getBody().getRequestId());
    }

    private String getOrderNotificationEmailSubject(int status) {
        // 根据状态码确定订单状态文字
        String statusText;
        switch (status) {
            case 1 -> statusText = "购票成功";
            case 2 -> statusText = "已取消";
            default -> statusText = "状态未知";
        }
        return EmailConstant.APP_NAME + " - 航班订单" + statusText;
    }

    private String getOrderNotificationEmailContent(String orderId, String flightNumber,
                                                    String seatNo, String departureTime, int status) {
        // 根据状态码确定文字和颜色
        String statusText;
        String statusColor;
        boolean isSucceed;

        switch (status) {
            case 1 -> {
                statusText = "购票成功";
                statusColor = "#67C23A"; // 绿色
                isSucceed = true;
            }
            case 2 -> {
                statusText = "已取消";
                statusColor = "#F56C6C"; // 红色
                isSucceed = false;
            }
            default -> {
                statusText = "未知状态";
                statusColor = "#E6A23C"; // 橙色警告
                isSucceed = false;
            }
        }

        return "<div style=\"margin:0;padding:0;background-color:#f7f7f7;width:100%;text-align:center;\">"
                + "<div style=\"display:inline-block;background-color:#ffffff;padding:0;border-radius:8px;"
                + "box-shadow:0 2px 12px rgba(0,0,0,0.1);max-width:500px;width:90%;"
                + "font-family:Arial,sans-serif;color:#333;text-align:left;overflow:hidden;\">"

                + "<div style=\"background-color:#409EFF;height:8px;\"></div>"

                + "<div style=\"padding:30px;line-height:1.6;\">"

                + "<h2 style=\"text-align:center;margin-bottom:20px;color:#409EFF;\">SmartAir 航班订单通知</h2>"

                + "<p style=\"font-size:15px;\">尊敬的用户，您的订单状态已更新：</p>"

                + "<div style=\"background-color:#f9f9f9;padding:15px 20px;border-radius:6px;margin:20px 0;"
                + "border-left:5px solid " + statusColor + ";\">"

                + "<p style=\"margin:8px 0;\"><strong>订单编号：</strong><span>" + orderId + "</span></p>"
                + "<p style=\"margin:8px 0;\"><strong>航班号：</strong><span>" + flightNumber + "</span></p>"
                + "<p style=\"margin:8px 0;\"><strong>座位号：</strong><span>" + seatNo + "</span></p>"
                + "<p style=\"margin:8px 0;\"><strong>起飞时间：</strong><span>" + departureTime + "</span></p>"
                + "<p style=\"margin:8px 0;\"><strong>订单状态：</strong>"
                + "<span style=\"color:" + statusColor + ";font-weight:bold;\">" + statusText + "</span></p>"
                + "</div>"

                + "<p style=\"font-size:13px;color:#666;\">"
                + (isSucceed
                ? "感谢您使用 SmartAir，祝您旅途愉快。"
                : "如您有其他出行安排，我们随时为您服务。<br>感谢您对 SmartAir 的关注与支持。")
                + "</p>"

                + "<hr style=\"margin:30px 0 20px;border:none;border-top:1px solid #eee;\">"

                + "<p style=\"font-size:12px;color:#aaa;text-align:center;\">"
                + "本邮件由系统自动发送，请勿直接回复。如有疑问，请联系航空公司或客服。"
                + "</p>"

                + "</div></div></div>";
    }
}
