package eu.droogers.smsmatrix;

/**
 * Created by gerben on 9-10-17.
 */

class NotSendMesage {
    private String phone;
    private String body;

    public NotSendMesage(String phone, String body) {
        this.phone = phone;
        this.body = body;
    }

    public String getPhone() {
        return phone;
    }

    public String getBody() {
        return body;
    }
}
