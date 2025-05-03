package org.visage.backend.enums;

import lombok.AllArgsConstructor;import lombok.Getter;import org.visage.backend.exception.ServiceException;

@AllArgsConstructor
@Getter
public enum GradeEnum {

    PRIMARY_ONE((byte) 1, "一年级", "小学一年级。"),
    PRIMARY_TWO((byte) 2, "二年级", "小学二年级。"),
    PRIMARY_THREE((byte) 3, "三年级", "小学三年级。"),
    PRIMARY_FOUR((byte) 4, "四年级", "小学四年级。"),
    PRIMARY_FIVE((byte) 5, "五年级", "小学五年级。"),
    PRIMARY_SIX((byte) 6, "六年级", "小学六年级。"),
    MIDDLE_ONE((byte) 7, "初一", "初中一年级。"),
    MIDDLE_TWO((byte) 8, "初二", "初中二年级。"),
    MIDDLE_THREE((byte) 9, "初三", "初中三年级。"),
    HIGH_ONE((byte) 10, "高一", "高中一年级。"),
    HIGH_TWO((byte) 11, "高二", "高中二年级。"),
    HIGH_THREE((byte) 12, "高三", "高中三年级。"),
    ;

    private final byte value;

    private final String name;

    private final String prompt;

    public static String getNameByValue(byte value) {
        for (GradeEnum grade : GradeEnum.values()) {
            if (grade.getValue() == value) {
                return grade.getName();
            }
        }
        throw new ServiceException("该年级不存在");
    }

    public static String getPromptByValue(byte value) {
        for (GradeEnum grade : GradeEnum.values()) {
            if (grade.getValue() == value) {
                return grade.getPrompt();
            }
        }
        throw new ServiceException("该年级不存在");
    }
}
