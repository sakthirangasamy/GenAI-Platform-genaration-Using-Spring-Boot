package com.ai.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.demo.entity.Question;
import com.ai.demo.repo.QuestionRepository;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    // Get random questions based on language
    public List<Question> getRandomQuestionsByLanguage(String language) {
        return questionRepository.findRandomQuestionsByLanguage(language);
    }

    // Get all questions based on language
    public List<Question> getQuestionsByLanguage(String language) {
        return questionRepository.findByLanguage(language);
    }
}
