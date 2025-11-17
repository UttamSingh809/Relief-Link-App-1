# ReliefLink - Crisis Resource Matching Platform

## Overview

ReliefLink is a web-based crisis relief platform that connects donors with people in need during emergencies and disasters. The application facilitates the donation and request of essential supplies (food, medicine, clothing, shelter, water, medical supplies) by matching donors with requesters based on category, location, and urgency levels. The platform includes role-based access for donors, requesters, volunteers, and administrators, with features for tracking donations, requests, and successful matches.

## Features

- **User Registration & Authentication**: Role-based access (Donor, Requester, Volunteer, Admin)
- **Donation Management**: Create and track donation offerings across 7 categories
- **Request Management**: Submit and manage resource requests with urgency levels
- **Smart Matching System**: Algorithm pairs donations with requests based on category, location, and quantity
- **Admin Dashboard**: User oversight, system backup, and complete system reset
- **Emergency Guidelines**: Static emergency contact information and disaster preparedness guides
- **Responsive Design**: Mobile-friendly interface with compact layout
- **Real-time Activity Feed**: Live updates on donations, requests, and matches

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.1.5** (Web MVC, Thymeleaf, DevTools, Actuator)
- **Maven** for dependency management
- **In-memory storage** (ConcurrentHashMap for thread-safe data persistence)

### Frontend
- **Vanilla JavaScript** (No frameworks for lightweight deployment)
- **HTML5** with server-rendered templates
- **CSS3** with responsive design and modern UI components
- **Fetch API** for client-server communication

### Key Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Thymeleaf
- Jackson Databind for JSON processing
- Spring Boot DevTools (development only)

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Frontend Architecture

**Technology Stack**: Vanilla JavaScript with server-rendered HTML templates

The frontend follows a multi-page application (MPA) pattern where each major feature has its own HTML page and corresponding JavaScript file. This architecture was chosen for simplicity and to avoid the complexity of frontend frameworks for a crisis relief application that needs to be lightweight and fast.

**Key Design Decisions**:
- **Template-based rendering**: Uses server-side HTML templates for initial page load, reducing client-side complexity
- **Modular JavaScript**: Each page has a dedicated JS file (login.js, register.js, dashboard.js, etc.) that handles only that page's interactions
- **Fetch API for backend communication**: All client-server communication uses native Fetch API with JSON payloads
- **Form-based interactions**: Heavy reliance on HTML forms for data submission, with JavaScript handling async submission and validation

**Pros**: Simple to understand and maintain, fast initial load times, works well without JavaScript frameworks
**Cons**: Page refreshes required for navigation, some code duplication across JS files

### Backend Architecture

**Technology Stack**: Java Spring Boot 3.1.5 with Maven

The backend follows a traditional MVC pattern with RESTful API endpoints. The application uses a layered architecture separating concerns between presentation (controllers), business logic (services), and data access (repositories).

**Key Design Decisions**:
- **RESTful API design**: Endpoints follow REST conventions (/api/donations, /api/requests, /api/matches)
- **Role-based access control**: AuthInterceptor enforces authentication and ADMIN-only access to admin endpoints
- **Session-based authentication**: HttpSession with secure session validation (getSession(false) to prevent auto-creation)
- **Matching algorithm**: Dedicated endpoint (/api/matches/find) for running donation-request matching logic based on category, exact location match, and quantity availability
- **In-memory storage**: DataStore component using ConcurrentHashMap for thread-safe in-memory data persistence with pre-loaded admin user

**Core Features**:
1. **User Management**: Registration, login, logout with role assignment (DONOR, REQUESTER, VOLUNTEER, ADMIN)
2. **Donation Management**: Create and track donation offerings with categorization (7 categories: FOOD, MEDICINE, CLOTHING, SHELTER, WATER, MEDICAL_SUPPLIES, OTHER)
3. **Request Management**: Submit and manage resource requests with same categorization
4. **Matching System**: Algorithm pairs donations with requests based on category match, location match, and sufficient quantity
5. **Admin Functions**: User oversight, system backup (JSON export), and complete system reset

**Security Implementation**:
- AuthInterceptor blocks all /api/** endpoints without valid session
- Admin endpoints (/api/admin/**) require ADMIN role or return 403 Forbidden
- Page controllers use request.getSession(false) to properly detect unauthenticated users
- No session auto-creation vulnerabilities

**Pros**: Straightforward architecture, single deployment unit, easier to maintain during emergencies, secure authentication
**Cons**: Scaling limitations, all features coupled in single application, in-memory data loss on restart

### Data Storage

**Database**: In-memory storage using ConcurrentHashMap (for MVP/prototype)

**Data Model**:
- **Users**: Stores user profiles with id, fullName, username, email, password, location, role (enum), createdAt
- **Donations**: Tracks donated items with id, donorId, donorName, category (enum), itemName, quantity, location, urgency (enum), description, matched (boolean), createdAt
- **Requests**: Tracks requested items with id, requesterId, requesterName, category (enum), itemName, quantity, location, urgency (enum), description, matched (boolean), createdAt  
- **Matches**: Records successful pairings with id, donationId, requestId, donorName, requesterName, category, itemName, quantity, location, urgency, matchedAt

**Design Decision**: In-memory storage was chosen for the MVP to enable rapid prototyping and easy deployment without database setup. Pre-loaded with an admin user (username: admin, password: admin123) for immediate system access. Data persists during runtime but is lost on restart. Atomic counters ensure thread-safe ID generation.

**Production Recommendation**: Migrate to PostgreSQL or similar RDBMS for persistent storage and data recovery capabilities.

### Authentication & Authorization

**Authentication Mechanism**: Form-based login with server-side HttpSession management

The application uses traditional username/password authentication with sessions. Login credentials are submitted via POST to /login endpoint, which validates credentials and stores userId, userRole, and username in HttpSession.

**Authorization Implementation**:
- **AuthInterceptor**: Spring MVC interceptor registered on /api/** paths
  - Validates session existence using request.getSession(false)
  - Returns 401 Unauthorized if no session or userId attribute
  - Returns 403 Forbidden for non-ADMIN users accessing /api/admin/** endpoints
- **Page Controllers**: All protected pages check getSession(false) and redirect to /login if null or missing userId
- **Admin Restriction**: /admin page requires ADMIN role, redirects others to /dashboard

**Authorization Levels**:
- **Public**: Login (/login) and registration (/register) pages only
- **Authenticated Users**: Dashboard, donations, requests, matches, guidelines, emergency contacts
- **Admin Only**: Admin dashboard (/admin) with user list, system reset, and data backup

**Security Features**:
- Session validation without auto-creation (prevents session fixation)
- Role-based API access control
- Protected page redirects for unauthenticated access
- Explicit ADMIN role enforcement on sensitive operations

**Pros**: Secure implementation, well-understood security model, prevents common session vulnerabilities
**Cons**: Requires server-side session storage, less suitable for distributed deployments, sessions lost on server restart

### Matching Algorithm

**Core Logic**: The MatchingService implements a greedy matching algorithm:

```java
for each unmatched donation:
  for each unmatched request:
    if isCompatible(donation, request):
      create match
      mark both as matched
      break inner loop
```

**Compatibility Criteria** (all must be true):
1. **Category matching**: donation.category == request.category
2. **Location matching**: donation.location.equalsIgnoreCase(request.location) (exact match, case-insensitive)
3. **Quantity sufficiency**: donation.quantity >= request.quantity

**Match Creation**:
- Quantity set to minimum of donation and request quantities
- Urgency level taken from request
- Both donation and request marked as matched (boolean flag)
- Match record persists with all relevant details

**Design Decision**: Manual trigger (via "Find New Matches" button on /matches page) rather than automatic real-time matching. First-come-first-serve pairing within compatibility constraints.

**Pros**: Controlled matching process, simple deterministic algorithm, allows review before finalization
**Cons**: Greedy approach not optimal, requires manual intervention, no partial matching (all-or-nothing)

## External Dependencies

### Frontend Libraries
- **None**: The application uses vanilla JavaScript without external frontend frameworks or libraries. This minimizes dependencies and reduces potential points of failure during crisis deployments.

### Backend Framework
- **Spring Boot 3.1.5**: Full-featured web application framework
- **Spring Web MVC**: For RESTful APIs and page controllers
- **Thymeleaf** (configured but not actively used): Templates served as static HTML with client-side rendering
- **Embedded Tomcat**: Runs on port 5000
- **Spring DevTools**: Hot reload during development
- **Maven**: Dependency management and build tool

### Static Assets
- **CSS**: Custom stylesheet (`/static/css/style.css`) with purple gradient design, responsive grid layouts, and modern UI components
- **JavaScript**: Modular JS files for each page (login.js, register.js, dashboard.js, donate.js, request.js, matches.js, admin.js) using Fetch API for backend communication

### Third-Party Services
- **None**: Completely self-contained application with no external API dependencies
- Emergency contacts are hardcoded static information
- No mapping services, notification systems, or payment processing

### Default Credentials
- **Admin Account** (pre-loaded):
  - Username: `admin`
  - Password: `admin123`
  - Role: ADMIN
  - Location: Central Command

**Design Decision**: The absence of external service dependencies makes the application more resilient during crisis situations when internet connectivity or third-party services may be unreliable. In-memory storage allows deployment without database infrastructure setup.

## Installation & Setup

### Prerequisites

Before running ReliefLink, ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.6+** (for building the project)
- **Git** (for cloning the repository)

### Local Development Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/UttamSingh809/Relief-Link-App-1.git
   cd Relief-Link-App-1
   ```

2. **Navigate to the project directory:**
   ```bash
   cd StockTracker
   ```

3. **Build the application:**
   ```bash
   mvn clean install
   ```

4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application:**
   - Open your browser and go to: `http://localhost:5000`
   - Default admin login: `admin` / `admin123`

### Running with Ngrok (Public Access)

To share your local application publicly using Ngrok:

1. **Install Ngrok:**
   - Download from: https://ngrok.com/download
   - Or install via package manager:
     ```bash
     # Windows (using Chocolatey)
     choco install ngrok

     # macOS
     brew install ngrok

     # Linux
     snap install ngrok
     ```

2. **Authenticate Ngrok (optional but recommended):**
   ```bash
   ngrok config add-authtoken YOUR_AUTH_TOKEN
   ```
   Get your auth token from: https://dashboard.ngrok.com/get-started/your-authtoken

3. **Start the Spring Boot application:**
   ```bash
   cd StockTracker
   mvn spring-boot:run
   ```

4. **In a new terminal, start Ngrok:**
   ```bash
   ngrok http 5000
   ```

5. **Copy the Ngrok URL:**
   - Ngrok will provide a URL like: `https://abcd1234.ngrok.io`
   - Share this URL to give others access to your local application

### Docker Setup (Alternative)

If you prefer using Docker:

1. **Create a Dockerfile in the project root:**
   ```dockerfile
   FROM openjdk:17-jdk-slim
   WORKDIR /app
   COPY pom.xml .
   COPY src ./src
   RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests
   EXPOSE 5000
   CMD ["java", "-jar", "target/relief-link-1.0.0.jar"]
   ```

2. **Build and run:**
   ```bash
   docker build -t relieflink .
   docker run -p 5000:5000 relieflink
   ```

## Usage Guide

### First Time Setup
1. Access the application at `http://localhost:5000`
2. Login with admin credentials: `admin` / `admin123`
3. Register new users or explore the system

### User Roles
- **Admin**: Full system access, user management, system reset
- **Donor**: Can create and manage donations
- **Requester**: Can create and manage requests
- **Volunteer**: Limited access for coordination

### Key Workflows
1. **Making a Donation:**
   - Login → Dashboard → Donate → Fill form → Submit
2. **Requesting Resources:**
   - Login → Dashboard → Request → Fill form → Submit
3. **Finding Matches:**
   - Login → Matches → Click "Find New Matches"
4. **Admin Functions:**
   - Login (admin) → Admin → View users, backup data, reset system

## API Endpoints

### Authentication
- `POST /login` - User login
- `POST /register` - User registration
- `POST /logout` - User logout

### Donations
- `GET /api/donations` - List all donations
- `POST /api/donations` - Create donation
- `GET /api/donations/{id}` - Get donation details
- `PUT /api/donations/{id}` - Update donation
- `DELETE /api/donations/{id}` - Delete donation

### Requests
- `GET /api/requests` - List all requests
- `POST /api/requests` - Create request
- `GET /api/requests/{id}` - Get request details
- `PUT /api/requests/{id}` - Update request
- `DELETE /api/requests/{id}` - Delete request

### Matches
- `GET /api/matches` - List all matches
- `POST /api/matches/find` - Find new matches
- `GET /api/matches/{id}` - Get match details

### Admin (Admin only)
- `GET /api/admin/users` - List all users
- `POST /api/admin/backup` - Backup system data
- `POST /api/admin/reset` - Reset system

## Configuration

### Application Properties
Key settings in `src/main/resources/application.properties`:
- `server.port=5000` - Application port
- `spring.application.name=ReliefLink` - Application name
- `spring.thymeleaf.cache=false` - Disable template caching in development

### Default Admin Account
- Username: `admin`
- Password: `admin123`
- Role: ADMIN

## Troubleshooting

### Common Issues

1. **Port 5000 already in use:**
   ```bash
   # Find process using port 5000
   netstat -ano | findstr :5000
   # Kill the process or change port in application.properties
   ```

2. **Java version issues:**
   ```bash
   java -version  # Should show Java 17+
   ```

3. **Maven build fails:**
   ```bash
   mvn clean
   mvn install
   ```

4. **Ngrok connection issues:**
   - Ensure no firewall blocking connections
   - Try different regions: `ngrok http 5000 --region=us`

### Logs
Application logs are available in the console when running `mvn spring-boot:run`. For more detailed logging, check the Spring Boot actuator endpoints.

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and test thoroughly
4. Commit your changes: `git commit -am 'Add feature'`
5. Push to the branch: `git push origin feature-name`
6. Submit a pull request

## License

This project is open source and available under the MIT License.

## Support

For support or questions:
- Create an issue on GitHub
- Contact the development team
- Check the troubleshooting section above
