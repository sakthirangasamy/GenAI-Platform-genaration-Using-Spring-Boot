package com.ai.demo.service;
import java.io.FileReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.demo.entity.Question;
import com.ai.demo.repo.QuestionRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Service
public class CsvQuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public void readCsvAndSaveToDb(String filePath) throws Exception {
        FileReader reader = new FileReader(filePath);

        CsvToBean<Question> csvToBean = new CsvToBeanBuilder<Question>(reader)
                .withType(Question.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        List<Question> questions = csvToBean.parse();
        questionRepository.saveAll(questions);
    }
}
