/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Service.Request;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import ch.zhaw.hsb.aurora.enricher.Configuration.Configuration;
import ch.zhaw.hsb.aurora.enricher.Configuration.PropertyCredentials;

/**
 * This class is the service for authentication with the DSpace repository.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class AuthenticationService {

    String repositoryAPIUrl;
    HttpClient client;
    String bearer;

    public AuthenticationService() {

        this.repositoryAPIUrl = Configuration.getInstance().getOrganisationField("repositoryAPI");

    }

    public void login() {

        this.client = HTTPService.getInstance();
        this.bearer = generateBearer();

    }

    private String generateBearer() {

        PropertyCredentials propertyCredentials = new PropertyCredentials();

        String username = propertyCredentials.getUsername();
        String password = propertyCredentials.getPassword();

        HttpResponse<String> response = HTTPService.sendRequest("", "text/plain",
                this.repositoryAPIUrl + "/authn/login?user=" + username + "&password=" +
                        URLEncoder.encode(password, StandardCharsets.UTF_8),
                "POST", null);

        if (response != null) {

            return response.headers().firstValue("Authorization").get();

        }

        return null;
    }

    public String getBearer() {
        return this.bearer;
    }

    public HttpClient getClient() {
        return this.client;
    }

    public String getRepositoryAPIUrl() {
        return this.repositoryAPIUrl;
    }

}
