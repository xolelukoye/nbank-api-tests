package requests.steps;

import io.restassured.common.mapper.TypeRef;
import io.restassured.specification.RequestSpecification;
import models.AccountResponse;
import models.DepositRequest;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.ResponseSpecs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountSteps {

    // создание счета, извлечение id
    public static long createAccount(RequestSpecification userSpec) {
        AccountResponse account = new ValidatedCrudRequester<AccountResponse>(
                userSpec,
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        return account.getId();
    }

    // чтение балансов всех аккаунтов пользователя
    public static Map<Long, Double> getBalances(RequestSpecification userSpec) {
        List<AccountResponse> accounts = new CrudRequester(
                userSpec,
                Endpoint.GET_ALL_CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(new TypeRef<List<AccountResponse>>() {});

        return accounts.stream()
                .collect(Collectors.toMap(AccountResponse::getId, AccountResponse::getBalance));
    }

    // пополнение счета (депозит)
    public static void deposit(RequestSpecification userSpec, long accountId, double amount) {
        new CrudRequester(userSpec,
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(DepositRequest.builder()
                        .id(accountId)
                        .balance(amount)
                        .build());
    }

    // разбивает пополнение счета на куски по 5000 (из-за лимита)
    public static void fundAccount(RequestSpecification userSpec, long accountId, double totalAmount) {
        double remaining = totalAmount;
        while (remaining > 0) {
            double chunk = Math.min(remaining, 5000.0);
            deposit(userSpec, accountId, chunk);
            remaining -= chunk;
        }
    }
}
