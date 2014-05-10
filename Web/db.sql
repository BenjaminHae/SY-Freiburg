--
-- Tabellenstruktur für Tabelle `scotland_yard`
--

CREATE TABLE IF NOT EXISTS `scotland_yard` (
  `group_id` int(11) NOT NULL,
  `position` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `direction` varchar(255) DEFAULT NULL,
  `transportation` varchar(255) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ID` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (ID)
);