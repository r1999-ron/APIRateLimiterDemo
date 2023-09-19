package com.ronak.assignment.task2;

import java.util.concurrent.CompletableFuture;

class Expression {
	private String expression;
	private CompletableFuture<String> result;

	public Expression(String expression) {
		this.expression = expression;
		this.result = new CompletableFuture<>();
	}

	public String getExpression() {
		return expression;
	}

	public CompletableFuture<String> getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result.complete(result);
	}
}
