package org.visage.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;import org.visage.backend.exception.ServiceException;

@AllArgsConstructor
@Getter
public enum SubjectEnum {

    Chinese((byte) 1, "语文", "语文"),

    Math((byte) 2, "数学", "数学"),

    English((byte) 3, "英语", "英语"),

    Physics((byte) 4, "物理", "物理"),

    Chemistry((byte) 5, "化学", "化学"),

    Biology((byte) 6, "生物", "生物"),

    History((byte) 7, "历史", "历史"),

    Geography((byte) 8, "地理", "地理"),

    Politics((byte) 9, "政治", "政治"),
    ;

    private final byte value;

    private final String name;

    private final String prompt;

    public static String getNameByValue(byte value) {
        for (SubjectEnum subject : SubjectEnum.values()) {
            if (subject.getValue() == value) {
                return subject.getName();
            }
        }
        throw new ServiceException("该学科不存在");
    }

    public static String getPromptByValue(byte value) {
        for (SubjectEnum subject : SubjectEnum.values()) {
            if (subject.getValue() == value) {
                return subject.getPrompt();
            }
        }
        throw new ServiceException("该学科不存在");
    }

}
