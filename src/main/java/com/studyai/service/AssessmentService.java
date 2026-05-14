package com.studyai.service;

import com.studyai.dto.AnswerDTO;
import com.studyai.dto.AssessmentDTO;
import com.studyai.entity.*;
import com.studyai.repository.*;
import com.studyai.service.agent.AssessmentAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final AssessmentResultRepository resultRepository;
    private final AssessmentAgent assessmentAgent;

    public List<QuizQuestion> generateQuestions(String subject, String knowledgePoint, String difficulty, int count) {
        List<QuizQuestion> questions = assessmentAgent.generateQuestions(subject, knowledgePoint, difficulty, count);
        for (QuizQuestion q : questions) {
            questionRepository.save(q);
        }
        return questions;
    }

    @Transactional
    public AssessmentResult submitAssessment(AssessmentDTO dto) {
        List<QuizQuestion> questions = new ArrayList<>();
        for (Long qid : dto.getQuestionIds()) {
            QuizQuestion q = questionRepository.findById(qid).orElse(null);
            if (q != null) questions.add(q);
        }

        List<QuizAttempt> attempts = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.valueOf(questions.size() * 20);

        for (int i = 0; i < questions.size() && i < dto.getAnswers().size(); i++) {
            QuizQuestion q = questions.get(i);
            AnswerDTO ans = dto.getAnswers().get(i);

            boolean correct = q.getCorrectAnswer() != null &&
                    q.getCorrectAnswer().trim().equalsIgnoreCase(ans.getAnswer() != null ? ans.getAnswer().trim() : "");

            QuizAttempt attempt = new QuizAttempt();
            attempt.setStudentId(dto.getStudentId());
            attempt.setQuestionId(q.getId());
            attempt.setStudentAnswer(ans.getAnswer());
            attempt.setIsCorrect(correct ? 1 : 0);
            attempt.setScore(correct ? BigDecimal.valueOf(20) : BigDecimal.ZERO);
            attempt.setTimeSpentSeconds(ans.getTimeSpentSeconds());
            attemptRepository.save(attempt);
            attempts.add(attempt);

            if (correct) totalScore = totalScore.add(BigDecimal.valueOf(20));
        }

        AssessmentResult result = assessmentAgent.analyzeAssessment(dto, questions, attempts);
        result.setTotalScore(totalScore);
        result.setMaxScore(maxScore);
        if (maxScore.compareTo(BigDecimal.ZERO) > 0) {
            result.setScorePercent(totalScore.multiply(BigDecimal.valueOf(100)).divide(maxScore, 0, RoundingMode.HALF_UP).intValue());
        }
        resultRepository.save(result);
        return result;
    }

    public List<AssessmentResult> listByStudent(Long studentId) {
        return resultRepository.findByStudentId(studentId);
    }
}
