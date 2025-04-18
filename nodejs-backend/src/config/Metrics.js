const mongoose = require('mongoose');

const metricsSchema = new mongoose.Schema({
  placementId: {
    type: String,
    required: true,
    index: true
  },
  platform: {
    type: String,
    required: true,
    enum: ['BigoAds', 'InMobi', 'TopOn', 'Mintegral', 'Vungle', 'Fyber', 'Chartboost', 'ironSource', 'UnityAds', 'Mahimeta']
  },
  price: {
    type: Number,
    required: true
  },
  timestamp: {
    type: Date,
    required: true,
    index: true
  },
  userData: {
    geo: {
      country: String,
      region: String,
      city: String
    },
    device: {
      type: String,
      enum: ['desktop', 'mobile', 'tablet']
    },
    os: {
      type: String,
      enum: ['android', 'ios', 'windows', 'macos', 'other']
    }
  }
}, {
  timestamps: true
});

// 添加索引
metricsSchema.index({ platform: 1, timestamp: 1 });
metricsSchema.index({ placementId: 1, timestamp: 1 });

module.exports = mongoose.model('Metrics', metricsSchema); 