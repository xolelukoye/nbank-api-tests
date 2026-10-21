package requests.skelethon.interfaces;

import io.restassured.response.ValidatableResponse;
import models.BaseModel;

public interface CrudEndpointInterface {
    ValidatableResponse post(BaseModel model);

    ValidatableResponse get(long id);
    ValidatableResponse get();

    ValidatableResponse update(long id, BaseModel model);
    ValidatableResponse update(BaseModel model);

    ValidatableResponse delete();
}
