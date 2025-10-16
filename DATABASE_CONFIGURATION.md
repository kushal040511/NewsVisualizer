# NewsVisualizer Database Configuration

## 🗄️ **Database Type: H2 Database**

The NewsVisualizer project uses **H2 Database** - a lightweight, embedded Java database engine.

## 📋 **Database Details:**

### **Database Engine:**
- **Name:** H2 Database Engine
- **Version:** 2.2.224
- **Type:** Embedded/File-based database
- **Language:** Java
- **License:** Open Source (MPL 2.0 or EPL 1.0)

### **Connection Configuration:**
```java
JDBC URL: jdbc:h2:./data/newsvisualizer;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
Username: sa (System Administrator)
Password: (empty/blank)
```

### **Database File Location:**
```
/Users/mkmohan/Desktop/NewsVisualizer/data/newsvisualizer.mv.db
File Size: ~45 KB (as of current state)
```

## 🔧 **Technical Stack:**

### **Connection Pooling:**
- **Library:** HikariCP 5.0.1
- **Max Pool Size:** 10 connections
- **Connection Timeout:** 30 seconds
- **Idle Timeout:** 10 minutes
- **Max Lifetime:** 30 minutes

### **Dependencies (from pom.xml):**
```xml
<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>

<!-- Connection Pooling -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version>
</dependency>
```

## 📊 **Database Schema:**

### **Table: users**
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
)
```

### **Table: search_history**
```sql
CREATE TABLE search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    search_type VARCHAR(20) NOT NULL,
    search_query VARCHAR(500),
    country VARCHAR(10),
    category VARCHAR(50),
    articles_found INTEGER DEFAULT 0,
    analysis_results TEXT,
    searched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
)
```

## 🔐 **Security Features:**

### **Password Hashing:**
- **Library:** BCrypt (jBCrypt 0.4)
- **Method:** Blowfish-based password hashing
- **Security:** Industry-standard password protection

### **Database Security:**
- **Access:** Local file-based access only
- **Authentication:** Simple username/password
- **Encryption:** File-system level (depending on OS)

## ✨ **Key Features:**

### **Why H2 Database?**
1. **✅ Lightweight:** Minimal memory footprint (~2.5MB jar)
2. **✅ Embedded:** No separate database server required
3. **✅ Fast:** In-memory and file-based modes
4. **✅ SQL Compatible:** Standard SQL support
5. **✅ Zero Configuration:** Works out of the box
6. **✅ Cross-Platform:** Pure Java implementation
7. **✅ Development Friendly:** Built-in web console available

### **H2 Advantages for This Project:**
- **Easy Deployment:** Single JAR file includes everything
- **No Installation:** No separate database server setup
- **Development Speed:** Quick prototyping and testing
- **Portability:** Database files can be easily moved
- **Performance:** Fast for small to medium datasets
- **Java Integration:** Seamless Java ecosystem integration

## 🚀 **Usage in NewsVisualizer:**

### **What's Stored:**
1. **User Accounts:**
   - Registration data
   - Login credentials (hashed passwords)
   - User profile information
   - Login timestamps

2. **Search History:**
   - User search patterns
   - News query history
   - Analysis results
   - Usage statistics

### **What's NOT Stored:**
- ❌ **News Articles** (fetched in real-time from APIs)
- ❌ **RSS Feed Data** (processed on-demand)
- ❌ **Visualization Data** (generated dynamically)
- ❌ **API Keys** (managed separately)

## 📈 **Database Operations:**

### **Main Operations:**
- **User Management:** Registration, authentication, profile updates
- **Session Management:** Login tracking, user sessions
- **Search History:** Store and retrieve user search patterns
- **Analytics:** Track usage patterns and preferences

### **Performance Characteristics:**
- **Small Dataset:** Optimized for personal/small team use
- **Fast Queries:** In-memory caching and indexing
- **Concurrent Access:** Connection pooling for multi-user support
- **Data Integrity:** ACID transactions and foreign key constraints

## 🔧 **Management & Maintenance:**

### **Database File:**
- **Location:** `./data/newsvisualizer.mv.db`
- **Backup:** Simple file copy
- **Size Management:** Automatic cleanup and optimization
- **Migration:** Standard SQL export/import

### **Monitoring:**
- **Logging:** SLF4J + Logback integration
- **Connection Monitoring:** HikariCP metrics
- **Error Handling:** Comprehensive exception management

## 🌐 **Alternative Database Options:**

For scaling or production use, the project could be easily migrated to:
- **PostgreSQL:** For production deployments
- **MySQL:** For web-based deployments  
- **SQLite:** For even lighter embedded use
- **Oracle/SQL Server:** For enterprise environments

The current H2 setup provides an excellent foundation for development and small-scale production use! 🚀