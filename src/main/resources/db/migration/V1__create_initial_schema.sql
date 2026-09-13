CREATE TABLE category (
                          category_id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL
);

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       university_email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(150) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       email_verified TINYINT(1) NOT NULL DEFAULT 0,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE listings (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          seller_id BIGINT NOT NULL,
                          category_id INT NOT NULL,
                          title VARCHAR(200) NOT NULL,
                          description TEXT,
                          price DECIMAL(12,2) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          version INT NOT NULL DEFAULT 0,
                          CONSTRAINT fk_listings_seller FOREIGN KEY (seller_id) REFERENCES users(id),
                          CONSTRAINT fk_listings_category FOREIGN KEY (category_id) REFERENCES category(category_id)
);

CREATE TABLE messages (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          conversation_id BIGINT NOT NULL,
                          sender_id BIGINT NOT NULL,
                          message_text TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);