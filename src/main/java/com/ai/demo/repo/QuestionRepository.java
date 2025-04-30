package com.ai.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ai.demo.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
	  @Query(value = "SELECT * FROM question WHERE language = :lang ORDER BY RAND() LIMIT 10", nativeQuery = true)
	    List<Question> findRandomQuestionsByLanguage(@Param("lang") String language);
	  
	    @Query("SELECT q FROM Question q WHERE q.language = :lang")
	    List<Question> findByLanguage(@Param("lang") String language);
	    
	   
}
