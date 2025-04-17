package bidding;

public class BiddingException extends Exception {
    public enum ErrorType {
        TIMEOUT,           // 超时
        NETWORK_ERROR,     // 网络错误
        INVALID_RESPONSE,  // 无效响应
        NO_BIDS,          // 无竞价
        LOW_BID,          // 竞价过低
        INVALID_TOKEN,    // 无效令牌
        UNKNOWN_ERROR     // 未知错误
    }
    
    private ErrorType errorType;
    private String adUnitId;
    
    public BiddingException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public BiddingException(ErrorType errorType, String message, String adUnitId) {
        super(message);
        this.errorType = errorType;
        this.adUnitId = adUnitId;
    }
    
    public BiddingException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public String getAdUnitId() {
        return adUnitId;
    }
    
    @Override
    public String toString() {
        return "BiddingException{" +
                "errorType=" + errorType +
                ", adUnitId='" + adUnitId + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
} 