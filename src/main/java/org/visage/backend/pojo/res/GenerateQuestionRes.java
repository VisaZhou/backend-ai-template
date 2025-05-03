package org.visage.backend.pojo.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Schema(name = "GenerateQuestionRes", description = "生成习题")
public class GenerateQuestionRes {

    @Schema(description = "题目")
    private String question;

    @Schema(description = "选项")
    private List<String> options;

    @Schema(description = "答案")
    private String answer;

}
