# APIRateLimiterDemo

## Problem Statement:
The program that accepts multiple mathematical expressions in bulk and evaluates each of them using any public Web API available. The program should display the result of each expression on the console. Let’s assume that the API only supports 50 requests per second per client whereas our application is expected to evaluate at least 500 expressions per second. Also, the user may initiate more concurrent requests than our application can handle.

Math Expression Rate Limiter

This Java project is an Expression Evaluator that allows users to input mathematical expressions for evaluation. It employs multi-threading, a token bucket algorithm, and network communication to process expressions efficiently.

Key Features:
- Rate limits API requests to ensure compliance with external API restrictions.
- Processes mathematical expressions with efficiency and accuracy.
- Supports queuing and token bucket algorithm for rate control.

Key Components and Technologies:
- Java
- Multi-threading
- Token Bucket Algorithm
- Network Communication (HTTP)
- LinkedBlockingQueue
- Logging

Overview:
The Expression Evaluator project is designed to efficiently evaluate mathematical expressions while respecting API rate limits. It uses a combination of multi-threading and token bucket algorithm for optimal performance.

Features:
- Math Expression Input: Users can input mathematical expressions via the command line.

- Concurrent Processing: Expressions are processed concurrently using multiple threads.

- Rate Limiting: A token bucket algorithm ensures that API requests do not exceed a defined rate.

- Logging: Comprehensive logging captures processing details and potential errors.

Code Structure:
 The project follows the standard Java package structure and consists of the following key components:

- ExpressionEvaluatorApp: The main application class that orchestrates the entire process.
- Expression: A class representing an expression to be evaluated.
- acquireToken(): Method implementing the token bucket algorithm for rate limiting.
- callApi(): Method for making HTTP requests to an external math evaluation API.
- Logging: Extensive logging is used to record processing details and errors.

## Use of Concurrency
Concurrency in the provided code is primarily managed using Java's ExecutorService and a blocking queue.

Here's how concurrency is maintained in the code:

ExecutorService: The code creates an ExecutorService using Executors.newFixedThreadPool. This thread pool manages a fixed number of worker threads (up to the specified limit), allowing concurrent execution of tasks. In this case, the number of worker threads is determined by Math.min(MAX_API_REQUESTS_PER_SECOND, DESIRED_EXPRESSIONS_PER_SECOND). This means it will use up to the smaller of the two values as the number of worker threads.

Blocking Queue: The LinkedBlockingQueue named expressionQueue is used to store Expression objects that represent mathematical expressions. This queue allows multiple producer threads (the main thread and the processing thread) to add expressions and a single consumer thread (the processing thread) to take expressions for processing. It effectively decouples the production and consumption of expressions, allowing for concurrency.

Thread Management: The main thread is responsible for reading user input and adding expressions to the expressionQueue. The processing of expressions is handled by the worker threads in the ExecutorService. Each worker thread takes expressions from the queue and processes them concurrently.

Synchronization: The acquireToken method uses synchronized blocks to ensure thread safety when checking and updating token counts, which are used to enforce the rate limit.

Overall, the combination of an ExecutorService, a blocking queue, and synchronized methods helps maintain concurrency in the application. The main thread and worker threads can work in parallel to process expressions while respecting rate limits and ensuring thread safety where necessary.


Overall Flow:

When the program starts, it reads expressions from the user and adds them to a queue. A separate processing thread asynchronously retrieves expressions from the queue and sends them to an external API for evaluation while respecting rate limits. Results are logged, and the program gracefully exits when all expressions are processed.

Dependencies:
- Java (JDK 8+)
