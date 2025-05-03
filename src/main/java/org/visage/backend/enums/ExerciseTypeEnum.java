package org.visage.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;import org.visage.backend.exception.ServiceException;

@AllArgsConstructor
@Getter
public enum ExerciseTypeEnum {

    RADIO((byte) 1, "单选题", "单选题：单选题是指在给定的四个选项中，只有一个选项是正确的。"),

    CHECKBOX((byte) 2, "多选题", "多选题：多选题是指在给定的四个选项中，可能有多个选项是正确的。"),

    JUDGE((byte) 3, "判断题", "判断题：答案只有对和错两种。"),

    ANSWER((byte) 4, "解答题", "解答题：解答题是指需要根据题目给出的条件进行推理和计算，得出答案。"),

    ;

    private final byte value;

    private final String name;

    private final String prompt;

    public static String getNameByValue(byte value) {
        for (ExerciseTypeEnum type : ExerciseTypeEnum.values()) {
            if (type.getValue() == value) {
                return type.getName();
            }
        }
        throw new ServiceException("该题型不存在");
    }

    public static String getPromptByValue(byte value) {
        for (ExerciseTypeEnum type : ExerciseTypeEnum.values()) {
            if (type.getValue() == value) {
                return type.getPrompt();
            }
        }
        throw new ServiceException("该题型不存在");
    }
}
