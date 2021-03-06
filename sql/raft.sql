CREATE TABLE IF NOT EXISTS `raft`.`node`(
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `node_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '节点ID',
  `node_state` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '节点角色状态',
  `term` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '投票轮次',
  `log_index` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '日志次序',
  `vote_for` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '投票节点ID',
  `vote_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '得到的票数',
  `create_time` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  `modify_time` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '更改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY idx_node_id (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='节点记录表';


INSERT INTO `raft`.`node`(node_id, node_state, term, log_index, vote_for, voteCount) VALUE (?,?,?,?,?,?);