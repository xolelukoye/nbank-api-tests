package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),
    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),
    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            AccountResponse.class
    ),
    GET_ALL_CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            AccountResponse.class
    ),
    DEPOSIT(
            "/accounts/deposit",
            DepositRequest.class,
            AccountResponse.class
    ),
    TRANSFER(
            "/accounts/transfer",
            TransferRequest.class,
            BaseModel.class
    ),
    UPDATE_PROFILE(
            "/customer/profile",
            UpdateProfileRequest.class,
            BaseModel.class
    ),
    GET_PROFILE(
            "/customer/profile",
            BaseModel.class,
            CustomerResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
