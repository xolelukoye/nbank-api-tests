package requests.steps;

import io.restassured.specification.RequestSpecification;
import models.TransferRequest;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import specs.ResponseSpecs;

public class TransferSteps {

    // успешный перевод
    public static void transfer(RequestSpecification userSpec, long senderId, long receiverId, double amount) {
        new CrudRequester(
                userSpec,
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(TransferRequest.builder()
                        .senderAccountId(senderId)
                        .receiverAccountId(receiverId)
                        .amount(amount)
                        .build());
    }
}
}
