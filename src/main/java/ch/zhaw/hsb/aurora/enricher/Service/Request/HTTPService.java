/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Request;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.Builder;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import ch.zhaw.hsb.aurora.enricher.Configuration.Configuration;

/**
 * This class is the service for HTTP actions.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class HTTPService {

    public static HttpClient client = null;
    private static String xsrfToken;

    private HTTPService() {
    }

    public static HttpClient getInstance() {

        if (client != null) {

            return client;

        }

        // Set up the CookieManager
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL); // Accept all cookies

        // Set the CookieManager as the default CookieHandler
        CookieHandler.setDefault(cookieManager);
        client = HttpClient.newBuilder().cookieHandler(cookieManager).build();
        return client;

    }

    public static String getXsrfToken() throws Exception {
        if (xsrfToken != null) {
            return xsrfToken;
        }

        HttpResponse<String> response = HTTPService.sendRequest(null, null,
                Configuration.getInstance().getOrganisationField("repositoryAPI") + Configuration.getInstance().getCSRFTokenEndpoint(), "GET", null);

        if (response == null) {
            System.out.println("Could not get XSRF Token from Repository. Try again.");
            System.exit(1);
        }

        xsrfToken = response.headers().firstValue("DSPACE-XSRF-TOKEN").get();

        return xsrfToken;
    }

    private static List<HttpCookie> getCookies() {
        HttpClient client = HTTPService.getInstance();
        return ((CookieManager) client.cookieHandler().get()).getCookieStore()
                .getCookies();
    }

    // bei einem POST wird CSRF Token benötigt, auch wenn man nicht eingeloggt ist.
    public static HttpResponse<String> sendRequest(String input, String inputType, String url, String method,
            Map<String, String> headers) {

        try {
            Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url));

            switch (method) {
                case "POST":
                    builder.POST(HttpRequest.BodyPublishers.ofString(input));
                    builder.header("X-XSRF-TOKEN", HTTPService.getXsrfToken());

                    break;
                case "GET":
                    builder.GET();

                    break;
                case "DELETE":
                    builder.DELETE();

                    break;
                default:
                    break;
            }

            if (inputType != null) {
                builder.header("Content-Type", inputType);

            }

            if (headers != null) {

                for (Entry<String, String> header : headers.entrySet()) {

                    builder.header(header.getKey(), header.getValue());
                }

            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = getInstance().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {

                List<HttpCookie> cookies = getCookies();
                for (HttpCookie cookie : cookies) {
                    if ("DSPACE-XSRF-COOKIE".equals(cookie.getName())) {
                        xsrfToken = cookie.getValue();
                        break;
                    }
                }

                return response;
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

    }

    public static HttpResponse<String> sendRequestWithAuthentication(String input, String inputType, String url,
            String method) {

        try {


            AuthenticationService authenticationService = new AuthenticationService();
            authenticationService.login();

            Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url));

            // Set the HTTP method
            switch (method.toUpperCase()) {
                case "GET":
                    requestBuilder.GET();
                    break;
                case "POST":
                    requestBuilder.POST(HttpRequest.BodyPublishers.ofString(input != null ? input : ""));
                    break;
                case "PUT":
                    requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(input != null ? input : ""));
                    break;
                case "PATCH":
                    requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(input != null ? input : ""));
                    break;
                case "DELETE":
                    requestBuilder.DELETE();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }

            if (inputType != null) {
                requestBuilder.header("Content-Type", inputType);

            }

            HttpRequest request;
            request = requestBuilder
                    .header("X-XSRF-TOKEN", HTTPService.getXsrfToken())
                    .header("Authorization", authenticationService.getBearer())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300 && !(response.statusCode() == 204 && !method.toUpperCase().equals("DELETE"))) {

                try {
                    xsrfToken = response.headers().firstValue("DSPACE-XSRF-TOKEN").get();

                } catch (NoSuchElementException e) {
                    // TODO: handle exception
                }

                return response;
            }

        } catch (Exception e) {
            System.out.println("Request to Repository unsuccessfull.");
            System.exit(1);
        }

        return null;

    }

}
