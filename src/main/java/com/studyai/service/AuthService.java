package com.studyai.service;

import com.studyai.common.JwtUtil;
import com.studyai.dto.LoginDTO;
import com.studyai.dto.RegisterDTO;
import com.studyai.dto.UpdateProfileDTO;
import com.studyai.entity.Student;
import com.studyai.repository.StudentRepository;
import com.studyai.repository.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Map<String, Object> login(LoginDTO loginDTO) {
        Student student = studentRepository.findByUsername(loginDTO.getUsername());
        if (student == null) {
            throw new RuntimeException("用户不存在");
        }
        if (student.getStatus() != null && student.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        boolean passwordMatch;
        if (student.getPassword() == null) {
            passwordMatch = false;
        } else if (student.getPassword().startsWith("$2")) {
            passwordMatch = passwordEncoder.matches(loginDTO.getPassword(), student.getPassword());
        } else {
            passwordMatch = loginDTO.getPassword().equals(student.getPassword());
        }
        if (!passwordMatch) {
            throw new RuntimeException("密码错误");
        }
        String token = JwtUtil.generateToken(student.getId(), student.getUsername());
        boolean hasProfile = studentProfileRepository.findTopByStudentIdOrderByVersionDesc(student.getId()) != null;
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", convertToMap(student));
        result.put("hasProfile", hasProfile);
        return result;
    }

    public Student register(RegisterDTO registerDTO) {
        Student exist = studentRepository.findByUsername(registerDTO.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        Student student = new Student();
        student.setUsername(registerDTO.getUsername());
        student.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        student.setNickname(registerDTO.getNickname());
        student.setEmail(registerDTO.getEmail());
        student.setStatus(1);
        student.setDeleted(0);
        return studentRepository.save(student);
    }

    public Map<String, Object> getUserInfo(Long userId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToMap(student);
    }

    public Map<String, Object> updateProfile(Long userId, UpdateProfileDTO dto) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (dto.getNickname() != null) student.setNickname(dto.getNickname());
        if (dto.getEmail() != null) student.setEmail(dto.getEmail());
        if (dto.getAvatar() != null) student.setAvatar(dto.getAvatar());
        if (dto.getAge() != null) student.setAge(dto.getAge());
        if (dto.getPhone() != null) student.setPhone(dto.getPhone());
        if (dto.getGender() != null) student.setGender(dto.getGender());
        if (dto.getMajor() != null) student.setMajor(dto.getMajor());
        if (dto.getSchool() != null) student.setSchool(dto.getSchool());
        if (dto.getGrade() != null) student.setGrade(dto.getGrade());
        Student updated = studentRepository.save(student);
        return convertToMap(updated);
    }

    private Map<String, Object> convertToMap(Student student) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", student.getId());
        map.put("username", student.getUsername());
        map.put("nickname", student.getNickname());
        map.put("email", student.getEmail());
        map.put("avatar", student.getAvatar());
        map.put("age", student.getAge());
        map.put("phone", student.getPhone());
        map.put("gender", student.getGender());
        map.put("major", student.getMajor());
        map.put("school", student.getSchool());
        map.put("grade", student.getGrade());
        return map;
    }
}
