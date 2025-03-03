CREATE TABLE chat_rooms (
                            id BIGSERIAL PRIMARY KEY,
                            room_id VARCHAR(255) UNIQUE NOT NULL,
                            user1_id VARCHAR(255) NOT NULL,
                            user2_id VARCHAR(255) NOT NULL,
                            created_at TIMESTAMP NOT NULL
);

CREATE TABLE chat_messages (
                               id BIGSERIAL PRIMARY KEY,
                               chat_room_id INT REFERENCES chat_rooms(id) ON DELETE CASCADE,
                               sender_id VARCHAR(255) NOT NULL,
                               receiver_id VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,
                               timestamp TIMESTAMP NOT NULL
);
