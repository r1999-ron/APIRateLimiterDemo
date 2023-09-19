# APIRateLimiterDemo
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

Code Structure
The project follows the standard Java package structure and consists of the following key components:

- ExpressionEvaluatorApp: The main application class that orchestrates the entire process.
- Expression: A class representing an expression to be evaluated.
- acquireToken(): Method implementing the token bucket algorithm for rate limiting.
- callApi(): Method for making HTTP requests to an external math evaluation API.
- Logging: Extensive logging is used to record processing details and errors.

Dependencies:
- Java (JDK 8+)
