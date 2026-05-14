package com.studyai.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String nickname;
    private String email;
    private String avatar;
    private Integer age;
    private String phone;
    private Integer gender;
    private String major;
    private String school;
    private String grade;
}
