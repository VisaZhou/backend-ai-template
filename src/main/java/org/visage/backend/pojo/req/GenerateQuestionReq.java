package org.visage.backend.pojo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Schema(name = "GenerateQuestionReq", description = "生成习题")
public class GenerateQuestionReq {

    @Schema(description = "题目类型(1:单选，2:多选，3:判断，4:解答)")
    private byte type;

    @Schema(description = "主题内容")
    private String topic;

    @Schema(description = "学科(1:语文，2:数学，3:英语，4:物理，5:化学，6:生物，7:历史，8:地理，9:政治)")
    private byte subject;

    @Schema(description = "年级(1:一年级，2:二年级，3:三年级，4:四年级，5:五年级，6:六年级，7:初一，8:初二，9:初三，10:高一，11:高二，12:高三)")
    private byte grade;

}
