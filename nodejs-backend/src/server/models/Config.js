const mongoose = require('mongoose');

const platformSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        enum: ['topon', 'bigo', 'mintegral', 'ironsource', 'inmobi', 'admob', 'facebook', 'vungle', 'chartboost', 'unity', 'fyber', 'mahimeta']
    },
    enabled: {
        type: Boolean,
        default: true
    },
    appId: String,
    appKey: String,
    placementId: String,
    bidFloor: {
        type: Number,
        default: 0
    }
});

const configSchema = new mongoose.Schema({
    bidTimeout: {
        type: Number,
        default: 5000,
        min: 1000,
        max: 30000
    },
    cacheExpiry: {
        type: Number,
        default: 300,
        min: 60,
        max: 3600
    },
    platforms: [platformSchema],
    createdAt: {
        type: Date,
        default: Date.now
    },
    updatedAt: {
        type: Date,
        default: Date.now
    }
});

// 更新时自动更新 updatedAt
configSchema.pre('save', function(next) {
    this.updatedAt = Date.now();
    next();
});

module.exports = mongoose.model('Config', configSchema); 