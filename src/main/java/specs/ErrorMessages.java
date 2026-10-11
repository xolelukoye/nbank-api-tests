package specs;

public enum ErrorMessages {
    UNAUTHORIZED_ACCESS("Unauthorized access to account"),
    INVALID_TRANSFER("Invalid transfer: insufficient funds or invalid accounts"),
    TRANSFER_BELOW_MIN("Transfer amount must be at least 0.01"),
    TRANSFER_ABOVE_MAX("Transfer amount cannot exceed 10000"),
    DEPOSIT_BELOW_MIN("Deposit amount must be at least 0.01"),
    DEPOSIT_ABOVE_MAX("Deposit amount cannot exceed 5000"),
    PROFILE_UPDATED("Profile updated successfully");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
