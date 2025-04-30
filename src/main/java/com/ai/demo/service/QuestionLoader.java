package com.ai.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ai.demo.repo.QuestionRepository;

@Component
public class QuestionLoader implements CommandLineRunner {

	@Autowired
	private CsvQuestionService csvQuestionService;

	@Autowired
	private QuestionRepository questionRepository;

	@Override
	public void run(String... args) throws Exception {
		if (questionRepository.count() == 0) {
			csvQuestionService.readCsvAndSaveToDb(
					"E:\\SpringBootWorkSpace\\CourseGenarationPlatform\\src\\main\\resources\\ques.csv");
			System.out.println("CSV data inserted.");
		} else {
			System.out.println("Questions already exist. Skipping import.");
		}

	}
}
