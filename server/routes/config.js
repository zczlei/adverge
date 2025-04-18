const express = require('express');
const router = express.Router();
const configController = require('../controllers/configController');

// 获取配置
router.get('/', configController.getConfig);

// 保存配置
router.post('/', configController.saveConfig);

module.exports = router; 