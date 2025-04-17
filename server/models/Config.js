const mongoose = require('mongoose');

const configSchema = new mongoose.Schema({
  platformPriority: [{
    platform: {
      type: String,
      required: true,
      enum: ['BigoAds', 'InMobi', 'TopOn', 'Mintegral', 'Vungle', 'Fyber', 'Chartboost', 'ironSource', 'UnityAds', 'Mahimeta']
    },
    weight: {
      type: Number,
      required: true,
      min: 0,
      max: 1
    }
  }],
  bidTimeout: {
    type: Number,
    default: 100, // 毫秒
    min: 50,
    max: 500
  },
  cacheDuration: {
    type: Number,
    default: 300, // 秒
    min: 60,
    max: 3600
  },
  adFormats: [{
    type: {
      type: String,
      required: true,
      enum: ['banner', 'native', 'interstitial', 'video']
    },
    dimensions: {
      width: Number,
      height: Number
    },
    platforms: [{
      type: String,
      enum: ['BigoAds', 'InMobi', 'TopOn', 'Mintegral', 'Vungle', 'Fyber', 'Chartboost', 'ironSource', 'UnityAds', 'Mahimeta']
    }]
  }],
  frequencyCapping: {
    enabled: {
      type: Boolean,
      default: true
    },
    maxImpressions: {
      type: Number,
      default: 3
    },
    timeWindow: {
      type: Number,
      default: 3600 // 秒
    }
  },
  targeting: {
    geo: [String],
    device: [String],
    os: [String]
  },
  isActive: {
    type: Boolean,
    default: true
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('Config', configSchema); 