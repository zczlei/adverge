import { logger } from './logger';

class RetryManager {
    constructor(config) {
        this.config = config;
        this.maxRetries = config.retry?.maxRetries || 3;
        this.retryDelay = config.retry?.retryDelay || 1000;
        this.retryBackoff = config.retry?.retryBackoff || 2;
    }

    async execute(requestFn, options = {}) {
        const maxRetries = options.maxRetries || this.maxRetries;
        const retryDelay = options.retryDelay || this.retryDelay;
        const retryBackoff = options.retryBackoff || this.retryBackoff;

        let lastError;
        for (let attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return await requestFn();
            } catch (error) {
                lastError = error;
                
                // 如果是非重试错误，直接抛出
                if (!this.shouldRetry(error)) {
                    throw error;
                }

                // 计算下次重试的延迟时间
                const delay = retryDelay * Math.pow(retryBackoff, attempt);
                
                logger.warn(`请求失败，${delay}ms后重试 (${attempt + 1}/${maxRetries}): ${error.message}`);
                
                // 等待后重试
                await new Promise(resolve => setTimeout(resolve, delay));
            }
        }

        throw lastError;
    }

    shouldRetry(error) {
        // 网络错误、超时、服务器错误等可以重试
        return (
            error.code === 'ECONNABORTED' || // 超时
            error.code === 'ECONNRESET' ||   // 连接重置
            error.code === 'ETIMEDOUT' ||    // 连接超时
            (error.response && error.response.status >= 500) || // 服务器错误
            error.message.includes('Network Error') // 网络错误
        );
    }
}

export default RetryManager; 