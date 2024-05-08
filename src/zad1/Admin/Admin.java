package zad1.Admin;

import zad1.Service.IClient;

public class Admin implements IClient {
    public static void main(String[] args) {

    }

    @Override
    public String getType() {
        return "ADMIN";
    }
}
