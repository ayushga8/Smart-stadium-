# 🏟️ Smart Stadium — AI-Powered Tournament Management Platform

A full-stack, real-time tournament and stadium management platform with AI-powered analytics, role-based access control, and a futuristic cyberpunk-themed UI.

![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green?style=flat-square&logo=springboot)
![React](https://img.shields.io/badge/React-19-blue?style=flat-square&logo=react)
![Vite](https://img.shields.io/badge/Vite-8-purple?style=flat-square&logo=vite)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue?style=flat-square&logo=postgresql)
![Tests](https://img.shields.io/badge/Tests-328-brightgreen?style=flat-square)

---

## ✨ Features

### 🔐 Authentication & Security
- **Dual Login System** — Email OTP + Google OAuth2
- **JWT-based** stateless authentication (access + refresh tokens)
- **Role-Based Access Control** — User, Volunteer, Admin
- **Rate limiting** on OTP requests to prevent abuse

### 📊 Stadium Management
- **Live Match Dashboard** — Real-time scores and schedules
- **Crowd Monitoring** — Live occupancy tracking with visual indicators
- **Stadium Map** — Interactive zone-based layout
- **Analytics** — AI-powered insights and statistics

### 🤖 AI Integration
- **Google Gemini AI** — Smart stadium assistant chatbot
- **Contextual responses** based on stadium data

### 🛡️ Admin Panel
- **User Management** — View all users, assign roles
- **Role Assignment** — Promote users to Volunteer/Admin
- **Platform Stats** — Total users, role distribution
- **Admin-only access** — Hidden from regular users

### ♿ Additional Modules
- **Accessibility Center** — Inclusive stadium features
- **Sustainability Dashboard** — Eco-friendly operations tracking
- **Transport Hub** — Parking & transit coordination
- **Security Operations** — Safety monitoring
- **Volunteer Hub** — Volunteer task coordination

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 25, Spring Boot 3.4, Spring Security |
| **Frontend** | React 19, Vite 8, CSS (Cyberpunk Theme) |
| **Database** | Neon PostgreSQL (Cloud) |
| **Auth** | JWT (HS256), Google OAuth2, Email OTP |
| **AI** | Google Gemini API |
| **Email** | Gmail SMTP with App Passwords |
| **Testing** | JUnit 5, Mockito, Spring MockMvc |

---

## 📁 Project Structure

```
Smart-stadium/
├── backend/
│   ├── src/main/java/com/smartstadium/
│   │   ├── config/          # Security, CORS configuration
│   │   ├── controller/      # REST API endpoints
│   │   ├── dto/             # Data transfer objects
│   │   ├── entity/          # JPA entities (User, ChatMessage)
│   │   ├── exception/       # Custom exceptions & global handler
│   │   ├── repository/      # JPA repositories
│   │   ├── security/        # JWT filter, OAuth2 handlers
│   │   └── service/         # Business logic
│   ├── src/test/            # 328 automated test cases
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/      # React UI components
│   │   │   ├── Dashboard.jsx
│   │   │   ├── AdminPanel.jsx
│   │   │   ├── LoginCard.jsx
│   │   │   ├── Sidebar.jsx
│   │   │   └── ... (15+ components)
│   │   ├── App.jsx          # Client-side routing
│   │   └── index.css        # Cyberpunk theme styles
│   ├── vite.config.js
│   └── package.json
└── .gitignore
```

---

## 🚀 Getting Started

### Prerequisites
- Java 25+
- Node.js 18+
- A Gmail account with [App Password](https://myaccount.google.com/apppasswords)
- (Optional) Google Cloud OAuth2 credentials
- (Optional) Neon PostgreSQL database

### 1. Clone the repository
```bash
git clone https://github.com/ayushga8/Smart-stadium-.git
cd Smart-stadium-
```

### 2. Backend Setup
```bash
cd backend

# Copy env template and fill in your values
cp .env.example .env
# Edit .env with your credentials

# Run the backend
# Windows:
set MAIL_USERNAME=your-email@gmail.com
set MAIL_PASSWORD=your-app-password
set JWT_SECRET=YourSecretKeyThatIsAtLeast256BitsLong!
set DATABASE_URL=jdbc:postgresql://your-host/your-db
set DATABASE_DRIVER=org.postgresql.Driver
set DATABASE_USERNAME=your-db-user
set DATABASE_PASSWORD=your-db-password
.\mvnw.cmd spring-boot:run -DskipTests
```

### 3. Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

### 4. Open the app
Navigate to **http://localhost:5173** in your browser.

---

## 🔑 Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `MAIL_USERNAME` | Gmail address for OTP emails | ✅ |
| `MAIL_PASSWORD` | Gmail App Password | ✅ |
| `JWT_SECRET` | Secret key (min 256 bits) | ✅ |
| `DATABASE_URL` | PostgreSQL JDBC URL | ✅ |
| `DATABASE_DRIVER` | `org.postgresql.Driver` | ✅ |
| `DATABASE_USERNAME` | Database username | ✅ |
| `DATABASE_PASSWORD` | Database password | ✅ |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID | Optional |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 Secret | Optional |
| `GEMINI_API_KEY` | Google Gemini API key | Optional |
| `CORS_ORIGINS` | Allowed frontend URLs | For deployment |
| `FRONTEND_URL` | Frontend base URL | For deployment |
| `PORT` | Server port (default: 8080) | For deployment |

---

## 🧪 Testing

The project includes **328 automated test cases** across 12 test files:

```bash
cd backend
.\mvnw.cmd test
```

| Test File | Tests | Coverage Area |
|-----------|-------|--------------|
| JwtServiceTest | 45 | JWT token generation & validation |
| RateLimiterTest | 36 | Rate limiting logic |
| AuthIntegrationTest | 36 | End-to-end auth flows |
| AuthServiceTest | 34 | Business logic |
| AdminControllerTest | 32 | Admin API endpoints |
| RoleAssignmentTest | 28 | Role auto-assignment |
| EntityTest | 25 | Entity models & enums |
| JwtRoleClaimsTest | 22 | JWT role claims |
| OtpServiceTest | 21 | OTP generation & verification |
| AuthControllerTest | 19 | Auth REST endpoints |
| UserControllerTest | 17 | User profile endpoints |
| SecurityTest | 13 | Security filter chain |

---

## 🔒 Security Features

- ✅ Passwords never stored — OTP-based passwordless auth
- ✅ JWT with expiry (15 min access, 7 day refresh)
- ✅ CORS protection with configurable origins
- ✅ Rate limiting on sensitive endpoints
- ✅ Role-based route protection (frontend + backend)
- ✅ HttpOnly cookies for refresh tokens
- ✅ No secrets in source code — all via environment variables

---

## 📦 Deployment

The app is deployment-ready with environment variable configuration:

1. **Backend** → Deploy to Render / Railway / Fly.io
2. **Frontend** → Deploy to Vercel / Netlify (`npm run build` → deploy `dist/`)
3. Set all environment variables on your hosting platform
4. Update Google Console redirect URI to production URL

---

## 🎨 UI Theme

The frontend features a **cyberpunk/mission-control** aesthetic with:
- Dark mode with cyan/teal accents
- Glassmorphism effects
- Animated gradients and glow effects
- Responsive sidebar navigation
- Real-time data visualizations

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 👤 Author

**Ayush** — [GitHub](https://github.com/ayushga8)

---

⭐ If you found this project helpful, please give it a star!
