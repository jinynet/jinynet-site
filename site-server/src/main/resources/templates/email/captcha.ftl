<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>验证码</title>
</head>
<body style="margin: 0; padding: 0; background-color: #ffffff; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; color: #000000;">
    <div style="max-width: 500px; margin: 0 auto; padding: 40px 20px;">
        <!-- Logo -->
        <div style="margin-bottom: 32px;">
            <span style="font-size: 24px; font-weight: 700; color: #e74c3c;">${logoText}</span>
        </div>

        <!-- 问候语 -->
        <div style="font-size: 18px; font-weight: 500; margin-bottom: 16px; line-height: 1.5;">
            你好,
        </div>

        <!-- 说明文字 -->
        <div style="font-size: 16px; line-height: 1.6; margin-bottom: 32px;">
            你可使用下方验证码进行登录.
        </div>

        <!-- 验证码框 -->
        <div style="background-color: #f5f5f5; border-radius: 8px; padding: 24px 40px; text-align: center; margin-bottom: 32px;">
            <span style="font-size: 36px; font-weight: 700; color: #000000; letter-spacing: 8px;">${code}</span>
        </div>

        <!-- 提示文字 -->
        <div style="font-size: 14px; color: #666666; line-height: 1.6; margin-bottom: 48px;">
            若你并未申请该验证码。可直接忽略本邮件.
        </div>

        <!-- 底部说明 -->
        <div style="font-size: 13px; color: #999999; line-height: 1.6; border-top: 1px solid #eee; padding-top: 24px;">
            本邮件发送原因：你曾在我方应用内或 ${siteUrl} 网站申请验证.
        </div>
    </div>
</body>
</html>
