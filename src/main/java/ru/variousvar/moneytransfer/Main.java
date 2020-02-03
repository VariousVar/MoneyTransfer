package ru.variousvar.moneytransfer;

import ru.variousvar.moneytransfer.dao.db.H2DbDao;

public class Main {
    public static void main(String[] args) throws Exception {
        H2DbDao.initialiseDatabase();
    }
}
