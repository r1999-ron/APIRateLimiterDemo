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
	private static final int MAX_API_REQUESTS_PER_SECOND = 50; // Adjust as needed
	private static final int DESIRED_EXPRESSIONS_PER_SECOND = 5000; // Adjust as needed
	private static final int QUEUE_CAPACITY = 1000;

	private LinkedBlockingQueue<Expression> expressionQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
	private ExecutorService executorService = Executors
			.newFixedThreadPool(Math.min(MAX_API_REQUESTS_PER_SECOND, DESIRED_EXPRESSIONS_PER_SECOND));
	private Logger logger = Logger.getLogger(ExpressionEvaluatorApp.class.getName());
	private AtomicLong lastRequestTime = new AtomicLong(System.currentTimeMillis());
	private AtomicLong tokens = new AtomicLong(MAX_API_REQUESTS_PER_SECOND); // Initialize with the maximum tokens
	private AtomicLong expressionCounter = new AtomicLong(0);

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
		Thread processingThread = new Thread(this::processExpressions);
		processingThread.start();

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

		try {
			processingThread.join();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Processing thread was interrupted.", e);
		}
	}

	private void processExpressions() {
		while (true) {
			try {
				Expression expr = expressionQueue.take();
				if (expr.getExpression().equals("end")) {
					break;
				}
				long currentExpressionNumber = expressionCounter.incrementAndGet(); // Increment the counter
				if (acquireToken()) {
					String result = callApi(expr.getExpression());
					expr.setResult(result);
					logger.log(Level.INFO,
							"Expression " + currentExpressionNumber + ": " + expr.getExpression() + " => " + result);
				} else {
					logger.log(Level.WARNING, "Rate limit exceeded (HTTP 429) for Expression " + currentExpressionNumber
							+ ": " + expr.getExpression());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	private synchronized boolean acquireToken() {
		long currentTime = System.currentTimeMillis();
		long elapsedTime = currentTime - lastRequestTime.getAndSet(currentTime);

		// Calculate how many tokens to add based on elapsed time.
		long tokensToAdd = (elapsedTime * MAX_API_REQUESTS_PER_SECOND) / 1000;

		// Ensure tokens don't exceed the maximum.
		tokensToAdd = Math.min(tokensToAdd, MAX_API_REQUESTS_PER_SECOND);

		// Refill tokens if needed.
		if (tokens.get() < MAX_API_REQUESTS_PER_SECOND) {
			tokens.addAndGet(tokensToAdd);
		}

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
			logger.log(Level.INFO, "Response Code: " + responseCode);
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
