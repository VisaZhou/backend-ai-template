package org.visage.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeTool {

    @Tool(description = "获取系统的当前时间")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(description = "提供时间和邮件发送地址，在该时间发送邮件给该地址。内容为：签到提醒")
    void sendEmail(@ToolParam(description = "ISO-8601 格式的时间") String time, String email) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("已设置在 " + alarmTime + " 向 " + email + " 发送签到提醒邮件");
    }
}
