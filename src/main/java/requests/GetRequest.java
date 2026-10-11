package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

public abstract class GetRequest<T extends BaseModel> extends BaseRequest {

    public GetRequest(RequestSpecification requestSpecification,
                      ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    public abstract ValidatableResponse get();
}
