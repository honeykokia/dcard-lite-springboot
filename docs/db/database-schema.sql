CREATE TABLE `users` (
                 `user_id`       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                 `email`         VARCHAR(100)    NOT NULL,
                 `password_hash` VARCHAR(200)    NOT NULL,
                 `display_name`  VARCHAR(20)     NOT NULL,
                 `role`          VARCHAR(10)     NOT NULL DEFAULT 'USER',
                 `created_at`    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                 PRIMARY KEY (`user_id`),
                 UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `boards` (
                  `board_id` BIGINT NOT NULL AUTO_INCREMENT,
                  `name` VARCHAR(50) NOT NULL,
                  `description` VARCHAR(200) NOT NULL,
                  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                  CONSTRAINT `pk_boards` PRIMARY KEY (`board_id`),
                  CONSTRAINT `uq_boards_name` UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX `idx_boards_name` ON `boards` (`name`);