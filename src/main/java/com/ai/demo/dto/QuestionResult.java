package com.ai.demo.dto;

import com.ai.demo.entity.Question;

public class QuestionResult {
    public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public String getUserAnswer() {
		return userAnswer;
	}

	public void setUserAnswer(String userAnswer) {
		this.userAnswer = userAnswer;
	}

	public boolean isAttempted() {
		return attempted;
	}

	public void setAttempted(boolean attempted) {
		this.attempted = attempted;
	}

	public boolean isCorrect() {
		return correct;
	}

	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	private Question question;
    private String userAnswer;
    private boolean attempted;
    private boolean correct;

    // Constructor
    public QuestionResult(Question question, String userAnswer, boolean attempted, boolean correct) {
        this.question = question;
        this.userAnswer = userAnswer;
        this.attempted = attempted;
        this.correct = correct;
    }

    // Getters and Setters...
}

