package requests.skelethon.requesters;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skelethon.Endpoint;
import requests.skelethon.HttpRequest;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest{
    private CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @SuppressWarnings("unchecked")
    public T post(BaseModel model) {
        return (T) crudRequester.post(model).extract().as(endpoint.getResponseModel());
    }

    public T post() {
        return post(null);
    }

    @SuppressWarnings("unchecked")
    public T get() {
        return (T) crudRequester.get().extract().as(endpoint.getResponseModel());
    }

    @SuppressWarnings("unchecked")
    public T get(long id) {
        return (T) crudRequester.get(id).extract().as(endpoint.getResponseModel());
    }

    @SuppressWarnings("unchecked")
    public T update(BaseModel model) {
        return (T) crudRequester.update(model).extract().as(endpoint.getResponseModel());
    }

    @SuppressWarnings("unchecked")
    public T update(long id, BaseModel model) {
        return (T) crudRequester.update(id, model).extract().as(endpoint.getResponseModel());
    }

    public T delete() {
        crudRequester.delete();
        return null;
    }
}
