package com.example.module_a.controller;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import com.arangodb.Protocol;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author AaronY
 * @version 1.0
 * @since 2023/10/31
 */
@RestController
@RequestMapping("/kg")
public class KnowledgeGraphController {

    @GetMapping("/test")
    public String test() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        // Base64 encoded CA certificate
        String encodedCA = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURHRENDQWdDZ0F3SUJBZ0lRR252b2ZITUNKeFZDQkVaWGxBc3g5ekFOQmdrcWhraUc5dzBCQVFzRkFEQW0KTVJFd0R3WURWUVFLRXdoQmNtRnVaMjlFUWpFUk1BOEdBMVVFQXhNSVFYSmhibWR2UkVJd0hoY05Nak14TURNdwpNRFkwT1RJNVdoY05Namd4TURJNE1EWTBPVEk1V2pBbU1SRXdEd1lEVlFRS0V3aEJjbUZ1WjI5RVFqRVJNQThHCkExVUVBeE1JUVhKaGJtZHZSRUl3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRRG4KTHA3WS9DS1VVRStoM0JBZG03ZDhGY09HY2VEZU5KUWhacDh6VFJvRTFYSERZdE1id3UzY2lWQXhxYTFucGFSTApBbFZLWjlCRWF4aG85MUhPNDZRMEQ0RUdMK1Fab3lGMldSTnJHZ1JzcTJNdkw1MmFSQzZMR21WTzViOXZqRGs2CkR2NEo4emNDOHhUeldEeXNQbmlkQlJ1RWo1NWpNbXRXTlRhOEZEZXpVcFh0TGFuY0NMZHRPa2FJUk41WHlrUGIKTDM2dkJrektPTUovc3VvVjhCYzREZldNb0plL21WakZjVldtMFZjb3d3VVRCS2lVZHhQZTQ1QmxmSEdBVkgrcgpLN0pwcVlsL29jc3lsR05CVitWVjJDSFJ2ZVMrN2RScHltTWR3V01UZXZGaUR3Z3Yvc2lFVjJVeExwcjFhcithCkYwQWN2cUlXVmZjT1c0RW5DVmhwQWdNQkFBR2pRakJBTUE0R0ExVWREd0VCL3dRRUF3SUNwREFQQmdOVkhSTUIKQWY4RUJUQURBUUgvTUIwR0ExVWREZ1FXQkJSZGc3dmZiL1B1dmxPOE1memFROHZHcE1HSUV6QU5CZ2txaGtpRwo5dzBCQVFzRkFBT0NBUUVBV01uRU1WRjFjZFlpTllXVG1LelVTcjlHMjJjVXhVWU0rRXlEMmdHU2FFb0FSaGVYCmdIaEhZejR0NU5UZ1o4UmNtK2NRSUJpc2lWV2tnb1JsMHluamdHdVpQZDJvQlVnM2RQcU9MS0laRW91Q0pSalkKZlNVMDNlZi93QVgzRFNjb1htdGdjZDNEZllyY3I4MXFqcWk0aENoamEzR01FaDBLbDJXcEFrL1lBOG1BNFdxagp2d05KWFZEb1c5VVBUY1NhRGZIaGduYmlLL0VJRlVTZlI0WWdMY01PQi8vcG9zU2g5WlYwb2Y2emFLSm0zUms0CjdNbDFwVFRPak5mYm9kVmNUZEZpeVM3RExLaFpVeGcyanlZRm01N2VxU0hWUzcvQjBUWmpDWE5rTXZOTEVHMm4KZDlyTG81RG1iOEFaSjlDbk9jK1RzdVpQUW1UR2gxWHBLY2JJTVE9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==";
        InputStream is = new java.io.ByteArrayInputStream(Base64.getDecoder().decode(encodedCA));

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null);
        ks.setCertificateEntry("caCert", caCert);

        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        ArangoDB arangoDB = new ArangoDB.Builder()
                .useSsl(true)
                .host("ba17be8fdb18.arangodb.cloud", 18529)
                .user("root")
                .password("fYFd7GuApPrieuVXKYzB")
                .protocol(Protocol.HTTP2_JSON)
                .sslContext(sslContext)
                .build();
        // Note that ArangoGraph Insights Platform runs deployments in a cluster configuration.
        // To achieve the best possible availability, your client application has to handle
        // connection failures by retrying operations if needed.
        ArangoDatabase db = arangoDB.db();
        System.out.println(db.name());
        return "ArangoDB dependency has loaded.";
    }

}
