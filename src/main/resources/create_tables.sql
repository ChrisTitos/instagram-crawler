CREATE TABLE IF NOT EXISTS `inst_locations` (
  `id` bigint(20) NOT NULL,
  `name` varchar(500) CHARACTER SET utf8mb4 NOT NULL,
  `latitude` float(10,6) NOT NULL,
  `longitude` float(10,6) NOT NULL,
  PRIMARY KEY (`id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `inst_users`
--

CREATE TABLE IF NOT EXISTS `inst_users` (
  `id` bigint(20) NOT NULL,
  `username` varchar(45) NOT NULL,
  `fullname` varchar(100) CHARACTER SET utf8mb4 NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `inst_posts`
--

CREATE TABLE IF NOT EXISTS `inst_posts` (
  `id` varchar(30) NOT NULL,
  `userId` bigint(20) NOT NULL,
  `latitude` float(10,6) DEFAULT NULL,
  `longitude` float(10,6) DEFAULT NULL,
  `createdAt` datetime NOT NULL,
  `text` varchar(1000) CHARACTER SET utf8mb4 DEFAULT NULL,
  `type` varchar(10) DEFAULT 'image',
  `locationId` bigint(20) DEFAULT NULL,
  `link` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `inst_post_user_idx` (`userId`),
  KEY `inst_post_location_idx` (`locationId`),
  KEY `id_string` (`id`) USING BTREE,
  CONSTRAINT `inst_post_location` FOREIGN KEY (`locationId`) REFERENCES `inst_locations` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inst_post_user` FOREIGN KEY (`userId`) REFERENCES `inst_users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;