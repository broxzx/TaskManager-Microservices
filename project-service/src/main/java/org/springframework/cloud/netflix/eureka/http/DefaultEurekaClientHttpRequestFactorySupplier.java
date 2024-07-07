//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.cloud.netflix.eureka.http;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.netflix.eureka.RestTemplateTimeoutProperties;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultEurekaClientHttpRequestFactorySupplier implements EurekaClientHttpRequestFactorySupplier, DisposableBean {
    private final AtomicReference<CloseableHttpClient> ref = new AtomicReference();
    private final RestTemplateTimeoutProperties restTemplateTimeoutProperties;
    private final List<HttpRequestInterceptor> requestInterceptors;

    /** @deprecated */
    @Deprecated
    public DefaultEurekaClientHttpRequestFactorySupplier() {
        this.restTemplateTimeoutProperties = new RestTemplateTimeoutProperties();
        this.requestInterceptors = List.of();
    }

    public DefaultEurekaClientHttpRequestFactorySupplier(RestTemplateTimeoutProperties restTemplateTimeoutProperties) {
        this.restTemplateTimeoutProperties = restTemplateTimeoutProperties;
        this.requestInterceptors = List.of();
    }

    public DefaultEurekaClientHttpRequestFactorySupplier(RestTemplateTimeoutProperties restTemplateTimeoutProperties,
                                                         List<HttpRequestInterceptor> requestInterceptors) {
        this.restTemplateTimeoutProperties = restTemplateTimeoutProperties;
        this.requestInterceptors = Collections.unmodifiableList(requestInterceptors);
    }

    public ClientHttpRequestFactory get(SSLContext sslContext, @Nullable HostnameVerifier hostnameVerifier) {
        TimeValue timeValue;
        if (this.restTemplateTimeoutProperties != null) {
            timeValue = TimeValue.ofMilliseconds(this.restTemplateTimeoutProperties.getConnectRequestTimeout());
        } else {
            timeValue = TimeValue.of(30L, TimeUnit.SECONDS);
        }

        HttpClientBuilder httpClientBuilder = HttpClients.custom().evictExpiredConnections().evictIdleConnections(timeValue);
        if (sslContext != null || hostnameVerifier != null || this.restTemplateTimeoutProperties != null) {
            httpClientBuilder.setConnectionManager(this.buildConnectionManager(sslContext, hostnameVerifier, this.restTemplateTimeoutProperties));
        }

        if (this.restTemplateTimeoutProperties != null) {
            httpClientBuilder.setDefaultRequestConfig(this.buildRequestConfig());
        }

        for (HttpRequestInterceptor interceptor : this.requestInterceptors) {
            httpClientBuilder.addRequestInterceptorLast(interceptor);
        }

        if (this.ref.get() == null) {
            this.ref.compareAndSet((CloseableHttpClient) null, httpClientBuilder.build());
        }

        CloseableHttpClient httpClient = (CloseableHttpClient)this.ref.get();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }

    private HttpClientConnectionManager buildConnectionManager(SSLContext sslContext, HostnameVerifier hostnameVerifier, RestTemplateTimeoutProperties restTemplateTimeoutProperties) {
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
        SSLConnectionSocketFactoryBuilder sslConnectionSocketFactoryBuilder = SSLConnectionSocketFactoryBuilder.create();
        if (sslContext != null) {
            sslConnectionSocketFactoryBuilder.setSslContext(sslContext);
        }

        if (hostnameVerifier != null) {
            sslConnectionSocketFactoryBuilder.setHostnameVerifier(hostnameVerifier);
        }

        connectionManagerBuilder.setSSLSocketFactory(sslConnectionSocketFactoryBuilder.build());
        if (restTemplateTimeoutProperties != null) {
            connectionManagerBuilder.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(Timeout.of((long)restTemplateTimeoutProperties.getSocketTimeout(), TimeUnit.MILLISECONDS)).build());
        }

        return connectionManagerBuilder.build();
    }

    private RequestConfig buildRequestConfig() {
        return RequestConfig.custom().setConnectTimeout(Timeout.of((long)this.restTemplateTimeoutProperties.getConnectTimeout(), TimeUnit.MILLISECONDS)).setConnectionRequestTimeout(Timeout.of((long)this.restTemplateTimeoutProperties.getConnectRequestTimeout(), TimeUnit.MILLISECONDS)).build();
    }

    public void destroy() throws Exception {
        CloseableHttpClient httpClient = (CloseableHttpClient)this.ref.get();
        if (httpClient != null) {
            httpClient.close();
        }

    }
}
