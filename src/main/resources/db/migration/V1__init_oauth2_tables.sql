-- V1__init_oauth2_tables.sql

CREATE TABLE IF NOT EXISTS `jwk_keys` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `algorithm` VARCHAR(20) NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `is_active` BIT(1) NOT NULL,
  `kid` VARCHAR(100) NOT NULL,
  `private_key_pem` LONGTEXT NOT NULL,
  `public_key_pem` LONGTEXT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmw6egcr7r3ew20rjm4vujrq3y` (`kid`),
  KEY `idx_jwk_kid` (`kid`),
  KEY `idx_jwk_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `oauth2_authorization` (
  `id` VARCHAR(100) NOT NULL,
  `registered_client_id` VARCHAR(100) NOT NULL,
  `principal_name` VARCHAR(200) NOT NULL,
  `authorization_grant_type` VARCHAR(100) NOT NULL,
  `authorized_scopes` VARCHAR(1000) DEFAULT NULL,
  `attributes` TEXT,
  `state` VARCHAR(500) DEFAULT NULL,
  `authorization_code_value` TEXT,
  `authorization_code_issued_at` TIMESTAMP NULL DEFAULT NULL,
  `authorization_code_expires_at` TIMESTAMP NULL DEFAULT NULL,
  `authorization_code_metadata` TEXT,
  `access_token_value` TEXT,
  `access_token_issued_at` TIMESTAMP NULL DEFAULT NULL,
  `access_token_expires_at` TIMESTAMP NULL DEFAULT NULL,
  `access_token_metadata` TEXT,
  `access_token_type` VARCHAR(100) DEFAULT NULL,
  `access_token_scopes` VARCHAR(1000) DEFAULT NULL,
  `oidc_id_token_value` TEXT,
  `oidc_id_token_issued_at` TIMESTAMP NULL DEFAULT NULL,
  `oidc_id_token_expires_at` TIMESTAMP NULL DEFAULT NULL,
  `oidc_id_token_metadata` TEXT,
  `refresh_token_value` TEXT,
  `refresh_token_issued_at` TIMESTAMP NULL DEFAULT NULL,
  `refresh_token_expires_at` TIMESTAMP NULL DEFAULT NULL,
  `refresh_token_metadata` TEXT,
  `user_code_value` TEXT,
  `user_code_issued_at` TIMESTAMP NULL DEFAULT NULL,
  `user_code_expires_at` TIMESTAMP NULL DEFAULT NULL,
  `user_code_metadata` TEXT,
  `device_code_value` TEXT,
  `device_code_issued_at` TIMESTAMP NULL DEFAULT NULL,
  `device_code_expires_at` TIMESTAMP NULL DEFAULT NULL,
  `device_code_metadata` TEXT,
  PRIMARY KEY (`id`),
  KEY `idx_auth_registered_client_id` (`registered_client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `oauth2_authorization_consent` (
  `registered_client_id` VARCHAR(100) NOT NULL,
  `principal_name` VARCHAR(200) NOT NULL,
  `authorities` VARCHAR(1000) NOT NULL,
  PRIMARY KEY (`registered_client_id`,`principal_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `oauth2_registered_client` (
  `id` VARCHAR(100) NOT NULL,
  `client_id` VARCHAR(100) NOT NULL,
  `client_id_issued_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `client_secret` VARCHAR(200) DEFAULT NULL,
  `client_secret_expires_at` TIMESTAMP NULL DEFAULT NULL,
  `client_name` VARCHAR(200) NOT NULL,
  `client_authentication_methods` VARCHAR(1000) NOT NULL,
  `authorization_grant_types` VARCHAR(1000) NOT NULL,
  `redirect_uris` VARCHAR(1000) DEFAULT NULL,
  `scopes` VARCHAR(1000) NOT NULL,
  `client_settings` VARCHAR(2000) NOT NULL,
  `token_settings` VARCHAR(2000) NOT NULL,
  `post_logout_redirect_uris` VARCHAR(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;