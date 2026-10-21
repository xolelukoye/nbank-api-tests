package requests.steps;

import io.restassured.specification.RequestSpecification;
import models.CustomerResponse;
import models.ProfileResponse;
import models.UpdateProfileRequest;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.ResponseSpecs;

public class UserSteps {

    // Обновление имени профиля (возвращает ответ, если нужно проверить)
    public static ProfileResponse updateName(RequestSpecification userSpec, String newName) {
        return new ValidatedCrudRequester<ProfileResponse>(
                userSpec,
                Endpoint.UPDATE_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .update(UpdateProfileRequest.builder()
                        .name(newName)
                        .build());
    }

    // Получение текущего профиля
    public static CustomerResponse getProfile(RequestSpecification userSpec) {
        return new ValidatedCrudRequester<CustomerResponse>(
                userSpec,
                Endpoint.GET_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get();
    }
}