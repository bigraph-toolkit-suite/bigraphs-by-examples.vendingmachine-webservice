package org.example.web;

/**
 * A response data object returned by some REST endpoints.
 * <p>
 * It can store a message, and also an image of the current state as a bigraph encoded as BASE64
 *
 * @author Dominik Grzelak
 */
public class ResponseData {

    public static ResponseData create(String message) {
        return new ResponseData(message, "");
    }

    public static ResponseData create(String message, String imageBase64) {
        return new ResponseData(message, imageBase64);
    }

    private String message;
    private String bigraphImage;

    public ResponseData() {
    }

    public ResponseData(String message, String bigraphImage) {
        this.message = message;
        this.bigraphImage = bigraphImage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBigraphImage() {
        return bigraphImage;
    }

    public void setBigraphImage(String bigraphImage) {
        this.bigraphImage = bigraphImage;
    }
}
