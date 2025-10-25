CREATE TABLE ACCOUNT (
    account_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    is_email_verified BOOLEAN DEFAULT false,
    pfp VARCHAR(512),
    role VARCHAR(50) NOT NULL,
    estado VARCHAR(100),
    faculdade VARCHAR(255),
    semestre INT,
    curso VARCHAR(255),
    doc_type VARCHAR(20) NOT NULL,
    doc_number VARCHAR(20) NOT NULL UNIQUE,
    bio TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IDEA (
    idea_id SERIAL PRIMARY KEY,
    account_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    price INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_account
        FOREIGN KEY(account_id)
        REFERENCES ACCOUNT(account_id)
        ON DELETE CASCADE
);

CREATE TABLE ACCOUNT_LINKS (
    link_id SERIAL PRIMARY KEY,
    account_id INT NOT NULL,
    url VARCHAR(512) NOT NULL,

    CONSTRAINT fk_account
        FOREIGN KEY(account_id)
        REFERENCES ACCOUNT(account_id)
        ON DELETE CASCADE
);

CREATE TABLE FOLLOW (
    followed_id INT NOT NULL,
    follower_id INT NOT NULL,

    PRIMARY KEY (followed_id, follower_id),

    CONSTRAINT fk_followed
        FOREIGN KEY(followed_id)
        REFERENCES ACCOUNT(account_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_follower
        FOREIGN KEY(follower_id)
        REFERENCES ACCOUNT(account_id)
        ON DELETE CASCADE
);

CREATE TABLE KEYWORDS (
    keyword_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE KEYWORD_ACCOUNT (
    keyword_id INT NOT NULL,
    account_id INT NOT NULL,

    PRIMARY KEY (keyword_id, account_id),

    CONSTRAINT fk_keyword
        FOREIGN KEY(keyword_id)
        REFERENCES KEYWORDS(keyword_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_account
        FOREIGN KEY(account_id)
        REFERENCES ACCOUNT(account_id)
        ON DELETE CASCADE
);

CREATE TABLE KEYWORD_IDEA (
    keyword_id INT NOT NULL,
    idea_id INT NOT NULL,

    PRIMARY KEY (keyword_id, idea_id),

    CONSTRAINT fk_keyword
        FOREIGN KEY(keyword_id)
        REFERENCES KEYWORDS(keyword_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_idea
        FOREIGN KEY(idea_id)
        REFERENCES IDEA(idea_id)
        ON DELETE CASCADE
);

CREATE TABLE IDEA_FILES (
    file_id SERIAL PRIMARY KEY,
    idea_id INT NOT NULL,
    file_name VARCHAR(255),
    file_type VARCHAR(50),
    file_url VARCHAR(512) NOT NULL,

    CONSTRAINT fk_idea
        FOREIGN KEY(idea_id)
        REFERENCES IDEA(idea_id)
        ON DELETE CASCADE
);

CREATE TABLE LIKES (
    account_id INT NOT NULL,
    idea_id INT NOT NULL,

    PRIMARY KEY (account_id, idea_id),

    CONSTRAINT fk_account
        FOREIGN KEY(account_id)
        REFERENCES ACCOUNT(account_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_idea
        FOREIGN KEY(idea_id)
        REFERENCES IDEA(idea_id)
        ON DELETE CASCADE
);

CREATE TABLE FAVORITE (
    account_id INT NOT NULL,
    idea_id INT NOT NULL,

    PRIMARY KEY (account_id, idea_id),

    CONSTRAINT fk_account
        FOREIGN KEY(account_id)
        REFERENCES ACCOUNT(account_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_idea
        FOREIGN KEY(idea_id)
        REFERENCES IDEA(idea_id)
        ON DELETE CASCADE
);
