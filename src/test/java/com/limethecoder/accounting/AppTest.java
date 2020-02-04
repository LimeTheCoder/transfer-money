package com.limethecoder.accounting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.limethecoder.accounting.model.Account;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class AppTest {
    private static final String DEFAULT_HOST = "http://192.168.99.1:5460";
    private static final String ACCOUNT_URI = "/account";
    private static final String MONEY_TRANSFER_URI = "/account/transfer";

    private static AppEntry appEntry;

    @BeforeClass
    public static void startApplication() throws Exception {
        appEntry = new AppEntry();
        appEntry.start();
    }

    @Test
    public void shouldCreateAccount() throws IOException {
        Account account = createAccount(500);
        assertThat(account.getBalance(), is(BigDecimal.valueOf(500)));
    }

    @Test
    public void shouldFindAccount() throws IOException {
        int amount = 50;
        Account account = createAccount(amount);

        Account restored = findAccount(account.getId());

        assertThat(restored, notNullValue());
        assertThat(restored.getId(), is(account.getId()));
        assertThat(restored.getBalance().intValue(), is(amount));
    }

    @Test
    public void shouldNotFindAccountIfNotExists() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(DEFAULT_HOST + ACCOUNT_URI + "/" + 999);
        request.setHeader("Accept", "application/json");

        CloseableHttpResponse response = client.execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(404));

        client.close();
    }

    @Test
    public void shouldTransferMoney() throws IOException {
        int fromAccountAmount = 50;
        Account fromAccount = createAccount(fromAccountAmount);

        int toAccountAmount = 100;
        Account toAccount = createAccount(toAccountAmount);

        int transferAmount = 45;
        transferMoney(fromAccount.getId(), toAccount.getId(), transferAmount, 200);

        Account restoredFromAccount = findAccount(fromAccount.getId());
        Account restoredToAccount = findAccount(toAccount.getId());

        assertThat(restoredFromAccount.getBalance().intValue(), is(fromAccountAmount - transferAmount));
        assertThat(restoredToAccount.getBalance().intValue(), is(toAccountAmount + transferAmount));
    }

    @Test
    public void shouldNotTransferMoneyForSameFromToId() throws IOException {
        transferMoney(1L, 1L, 50, 400);
    }

    @Test
    public void shouldNotTransferMoneyWithInvalidAmount() throws IOException {
        transferMoney(1L, 2L, -50, 400);
    }

    private Account createAccount(int balance) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(DEFAULT_HOST + ACCOUNT_URI);

        String json = String.format("{\"balance\":%s}", balance);
        StringEntity entity = new StringEntity(json);
        request.setEntity(entity);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");

        CloseableHttpResponse response = client.execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(201));

        client.close();

        return retrieveResourceFromResponse(response, Account.class);
    }

    private Account findAccount(long accountId) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(DEFAULT_HOST + ACCOUNT_URI + "/" + accountId);
        request.setHeader("Accept", "application/json");

        CloseableHttpResponse response = client.execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));

        client.close();

        return retrieveResourceFromResponse(response, Account.class);
    }

    private void transferMoney(long fromAccount, long toAccount, int amount, int expectedStatusCode)
            throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(DEFAULT_HOST + MONEY_TRANSFER_URI);

        String json = String.format("{\"amount\":%s, \"fromAccount\":%s, \"toAccount\":%s}",
                amount, fromAccount, toAccount);
        StringEntity entity = new StringEntity(json);
        request.setEntity(entity);
        request.setHeader("Content-type", "application/json");

        CloseableHttpResponse response = client.execute(request);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(expectedStatusCode));

        client.close();
    }

    public static <T> T retrieveResourceFromResponse(HttpResponse response, Class<T> clazz)
            throws IOException {
        String jsonFromResponse = EntityUtils.toString(response.getEntity());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonFromResponse, clazz);
    }
}
