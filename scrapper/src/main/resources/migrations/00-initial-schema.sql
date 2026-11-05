
CREATE TABLE user_ids (
                       id BIGINT PRIMARY KEY UNIQUE NOT NULL
);

CREATE TABLE links (
                       id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY ,
                       url VARCHAR(255) NOT NULL,
                       update_time TIMESTAMP
);

CREATE TABLE tags (
                      id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                      tag VARCHAR(255) NOT NULL
);

CREATE TABLE filters (
                         id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY ,
                         filter VARCHAR(255) NOT NULL
);


CREATE TABLE links_filters (
                               id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                               link_id BIGINT NOT NULL,
                               user_id BIGINT NOT NULL,
                               filter_id BIGINT NOT NULL,
                               FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE,
                               FOREIGN KEY (user_id) REFERENCES user_ids(id) ON DELETE CASCADE,
                               FOREIGN KEY (filter_id) REFERENCES filters(id) ON DELETE CASCADE
);
CREATE TABLE links_tags (
                            id BIGINT PRIMARY KEY UNIQUE GENERATED ALWAYS AS IDENTITY,
                            link_id BIGINT NOT NULL,
                            user_id BIGINT NOT NULL,
                            tag_id BIGINT NOT NULL,
                            FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE,
                            FOREIGN KEY (user_id) REFERENCES user_ids(id) ON DELETE CASCADE,
                            FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE TABLE links_users (
                            id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY ,
                            user_id BIGINT NOT NULL,
                            link_id BIGINT NOT NULL,
                            FOREIGN KEY (user_id) REFERENCES user_ids(id) ON DELETE CASCADE,
                            FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE
);
