const Config = require('../models/Config');

// 获取配置
exports.getConfig = async (req, res) => {
    try {
        let config = await Config.findOne();
        if (!config) {
            // 如果没有配置，创建一个默认配置
            config = new Config({
                bidTimeout: 5000,
                cacheExpiry: 300,
                platforms: [
                    { name: 'topon', enabled: true },
                    { name: 'bigo', enabled: true },
                    { name: 'mintegral', enabled: true },
                    { name: 'ironsource', enabled: true },
                    { name: 'inmobi', enabled: true },
                    { name: 'admob', enabled: true },
                    { name: 'facebook', enabled: true },
                    { name: 'vungle', enabled: true },
                    { name: 'chartboost', enabled: true },
                    { name: 'unity', enabled: true },
                    { name: 'fyber', enabled: true },
                    { name: 'mahimeta', enabled: true }
                ]
            });
            await config.save();
        }
        res.json(config);
    } catch (error) {
        console.error('获取配置失败:', error);
        res.status(500).json({ error: '获取配置失败' });
    }
};

// 保存配置
exports.saveConfig = async (req, res) => {
    try {
        const { bidTimeout, cacheExpiry, platforms } = req.body;

        // 验证配置数据
        if (!bidTimeout || !cacheExpiry || !platforms) {
            return res.status(400).json({ error: '缺少必要的配置参数' });
        }

        if (bidTimeout < 1000 || bidTimeout > 30000) {
            return res.status(400).json({ error: '竞价超时时间必须在1000-30000ms之间' });
        }

        if (cacheExpiry < 60 || cacheExpiry > 3600) {
            return res.status(400).json({ error: '缓存过期时间必须在60-3600秒之间' });
        }

        // 更新或创建配置
        let config = await Config.findOne();
        if (!config) {
            config = new Config();
        }

        config.bidTimeout = bidTimeout;
        config.cacheExpiry = cacheExpiry;
        config.platforms = platforms;

        await config.save();

        // 通知所有服务配置已更新
        req.app.emit('configUpdated', config);

        res.json({ message: '配置保存成功', config });
    } catch (error) {
        console.error('保存配置失败:', error);
        res.status(500).json({ error: '保存配置失败' });
    }
}; 