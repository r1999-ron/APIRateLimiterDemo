/*
Problem Statement
Write a program that accepts multiple mathematical expressions in bulk and evaluates each of them using any public Web API available. The program should display the result of each expression on the console. Let’s assume that the API only supports 50 requests per second per client whereas your application is expected to evaluate at least 500 expressions per second. Also, the user may initiate more concurrent requests than your application can handle. Suggest an approach to handle this along with the reasoning and implementation of the same.

Rules
No expressions should be evaluated in the code. All evaluations should be using the Web API.
You can assume different expressions/operators that are compatible with the API you choose. 
Example: Some API might use ^ operator for power some might use pow()
Example
Input (every line is an expression, evaluate when “end” is provided as an expression)
2 * 4 * 4
5 / (7 - 5)
sqrt(5^2 - 4^2)
sqrt(-3^2 - 4^2)
end
Output
2 * 4 * 4 => 32
5 / (7 - 5) => 2.5
sqrt(5^2 - 4^2) => 3
sqrt(-3^2 - 4^2) = 5i
*/
package com.ronak.assignment.task2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpressionEvaluatorApp {
	private static final String API_ENDPOINT = "http://api.mathjs.org/v4/";
	private static final int MAX_API_REQUESTS_PER_SECOND = 50;
	private static final int DESIRED_EXPRESSIONS_PER_SECOND = 500;
	private static final int QUEUE_CAPACITY = 1000; // Adjust the capacity as needed

	private LinkedBlockingQueue<Expression> expressionQueue;
	private ExecutorService executorService;
	private Logger logger;
	private AtomicLong lastRequestTime = new AtomicLong(System.currentTimeMillis());
	private AtomicLong tokens = new AtomicLong(0);

	public ExpressionEvaluatorApp() {
		expressionQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
		int maxThreads = Math.min(MAX_API_REQUESTS_PER_SECOND, DESIRED_EXPRESSIONS_PER_SECOND);
		executorService = Executors.newFixedThreadPool(maxThreads);
		logger = Logger.getLogger(ExpressionEvaluatorApp.class.getName());
	}

	public void start() {
		Scanner scanner = new Scanner(System.in);
		logger.log(Level.INFO, "Enter mathematical expressions (type 'end' to finish):");
		String expression;
		do {
			expression = scanner.nextLine().trim();
			if (!expression.equals("end")) {
				try {
					expressionQueue.put(new Expression(expression)); // Add to the queue
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		} while (!expression.equals("end"));

		// Start a thread to process expressions from the queue
		Thread processingThread = new Thread(() -> {
			while (true) {
				try {
					Expression expr = expressionQueue.take();
					if (expr.getExpression().equals("end")) {
						break;
					}
					if (acquireToken()) {
						String result = callApi(expr.getExpression());
						expr.setResult(result);
						logger.log(Level.INFO, expr.getExpression() + " => " + result);
					} else {
						logger.log(Level.WARNING, "Rate limit exceeded for expression: " + expr.getExpression());
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});
		processingThread.start();

		// Add "end" to the queue to signal the end of processing
		try {
			expressionQueue.put(new Expression("end"));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Thread execution was interrupted.", e);
		} finally {
			scanner.close();
		}

		// Wait for the processing thread to complete
		try {
			processingThread.join();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Processing thread was interrupted.", e);
		}
	}

	private boolean acquireToken() {
		long currentTime = System.currentTimeMillis();
		long elapsedTime = currentTime - lastRequestTime.getAndSet(currentTime);

		// Calculate how many tokens to add based on elapsed time.
		long tokensToAdd = elapsedTime * MAX_API_REQUESTS_PER_SECOND / 1000;

		// Ensure tokens don't exceed the maximum.
		tokensToAdd = Math.min(tokensToAdd, MAX_API_REQUESTS_PER_SECOND);

		// Add tokens to the bucket.
		tokens.getAndAdd(tokensToAdd);

		if (tokens.get() >= 1) {
			tokens.decrementAndGet(); // Consume one token.
			return true;
		} else {
			return false;
		}
	}

	private String callApi(String expression) {
		try {
			URL url = new URL(API_ENDPOINT + "?expr=" + expression);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000); // Set a connection timeout (5 seconds)

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					StringBuilder response = new StringBuilder();
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					return response.toString();
				}
			} else {
				logger.log(Level.WARNING, "API Request Failed with Response Code: " + responseCode);
				return "API Request Failed with Response Code: " + responseCode;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "API Request Failed with Exception: " + e.getMessage(), e);
			return "API Request Failed with Exception: " + e.getMessage();
		}
	}

	public static void main(String[] args) {
		ExpressionEvaluatorApp app = new ExpressionEvaluatorApp();
		app.start();

	}
}
