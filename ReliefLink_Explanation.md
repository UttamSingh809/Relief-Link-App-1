### ReliefLink Application Explained

The ReliefLink application is a web-based platform designed to connect donors with individuals or organizations in need of aid. It functions as a matching system where donations are paired with corresponding requests based on criteria like item category, location, and quantity. The application is built using the Spring Boot framework.

#### Core Functionality

1.  **User Management & Authentication**:
    *   Users can register with roles such as `DONOR`, `REQUESTER`, or `VOLUNTEER`. There is also a pre-defined `ADMIN` role.
    *   Authentication is session-based. After a successful login, the user's ID and role are stored in the `HttpSession`.
    *   The system uses an `AuthInterceptor` to protect API endpoints, ensuring that only authenticated users can access them. Admin-specific endpoints are further restricted to users with the `ADMIN` role.

2.  **Donations and Requests**:
    *   Logged-in users can submit **donations** of items (e.g., food, medicine, clothing).
    *   Users can also submit **requests** for items they need.
    *   Both donations and requests include details like the item category, quantity, location, and an urgency level.

3.  **Matching Service**:
    *   This is the heart of the application. A `MatchingService` contains the core logic to find compatible pairs of donations and requests.
    *   A match is considered compatible if the **item category**, **location**, and **quantity** (donation quantity must be greater than or equal to request quantity) align.
    *   When a match is found, the system records it and marks the corresponding donation and request as "matched" to prevent them from being matched again.

4.  **Data Persistence**:
    *   The application uses an **in-memory data store** (`DataStore.java`).
    *   It uses `ConcurrentHashMap` objects to store all data, including users, donations, requests, and matches.
    *   **Crucially, this means all data is volatile and will be lost when the application is shut down.** An admin user is created by default every time the application starts.

5.  **Admin Features**:
    *   An admin dashboard (`/admin`) provides administrative functionalities.
    *   Admins can view all users in the system.
    *   Admins have the ability to **reset the entire system**, which clears all data from the in-memory store.
    *   Admins can also **backup the current data**, which exports the contents of the data store as a JSON object.

#### Technical Architecture

*   **Framework**: Spring Boot
*   **Language**: Java
*   **View Layer**: Thymeleaf is used for server-side rendering of HTML pages (`dashboard.html`, `login.html`, etc.).
*   **Web Layer**:
    *   `@RestController` (`ApiController`): Exposes a RESTful API for creating and retrieving data (donations, requests, matches) via AJAX from the frontend.
    *   `@Controller` (`AuthController`, `DashboardController`): Handles traditional web page navigation and user authentication.
*   **Configuration**:
    *   The application runs on port `5000`, as defined in `application.properties`.
    *   `WebConfig.java` registers the `AuthInterceptor` to secure the API endpoints.
