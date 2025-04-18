const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const { Kafka } = require('kafkajs');
const Redis = require('ioredis');

const app = express();
const port = process.env.PORT || 3000;

// 中间件
app.use(cors());
app.use(express.json());

// 数据库连接
mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/adplatform', {
  useNewUrlParser: true,
  useUnifiedTopology: true
});

// Redis连接
const redis = new Redis(process.env.REDIS_URL || 'redis://localhost:6379');

// Kafka配置
const kafka = new Kafka({
  clientId: 'ad-platform',
  brokers: [process.env.KAFKA_BROKER || 'localhost:9092']
});

const producer = kafka.producer();
const consumer = kafka.consumer({ groupId: 'ad-platform-group' });

// 路由
app.post('/api/bid', async (req, res) => {
  try {
    const { placementId, userData } = req.body;
    
    // 发送竞价请求到Kafka
    await producer.send({
      topic: 'bid-requests',
      messages: [
        { value: JSON.stringify({ placementId, userData, timestamp: Date.now() }) }
      ]
    });
    
    // 从Redis获取缓存结果
    const cachedBid = await redis.get(`bid:${placementId}`);
    if (cachedBid) {
      return res.json(JSON.parse(cachedBid));
    }
    
    // 如果没有缓存，返回处理中状态
    res.json({ status: 'processing' });
  } catch (error) {
    console.error('竞价请求错误:', error);
    res.status(500).json({ error: '服务器错误' });
  }
});

app.post('/api/metrics', async (req, res) => {
  try {
    const { placementId, platform, price, timestamp } = req.body;
    
    // 记录到MongoDB
    await mongoose.model('Metrics').create({
      placementId,
      platform,
      price,
      timestamp
    });
    
    // 更新Redis中的统计数据
    await redis.zincrby('platform:ecpm', price, platform);
    
    res.status(200).json({ status: 'success' });
  } catch (error) {
    console.error('数据上报错误:', error);
    res.status(500).json({ error: '服务器错误' });
  }
});

app.get('/api/config', async (req, res) => {
  try {
    const config = await redis.get('ad:config');
    if (config) {
      return res.json(JSON.parse(config));
    }
    
    // 从MongoDB获取默认配置
    const defaultConfig = await mongoose.model('Config').findOne();
    res.json(defaultConfig);
  } catch (error) {
    console.error('配置获取错误:', error);
    res.status(500).json({ error: '服务器错误' });
  }
});

// 启动服务器
app.listen(port, async () => {
  console.log(`服务器运行在端口 ${port}`);
  
  // 连接Kafka
  await producer.connect();
  await consumer.connect();
  await consumer.subscribe({ topic: 'bid-requests' });
  
  // 处理竞价请求
  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      const { placementId, userData } = JSON.parse(message.value);
      
      try {
        // 执行竞价逻辑
        const bids = await Promise.all([
          bidBigoAds(placementId, userData),
          bidInMobi(placementId, userData),
          // ... 其他平台
        ]);
        
        const winningBid = bids.reduce((max, bid) => 
          bid && bid.price > (max?.price || 0) ? bid : max, null
        );
        
        // 缓存结果
        if (winningBid) {
          await redis.setex(
            `bid:${placementId}`,
            300, // 5分钟过期
            JSON.stringify(winningBid)
          );
        }
      } catch (error) {
        console.error('竞价处理错误:', error);
      }
    }
  });
}); 