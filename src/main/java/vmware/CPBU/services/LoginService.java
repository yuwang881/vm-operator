package vmware.CPBU.services;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.protocol.HttpConfiguration;
import vmware.CPBU.utils.SslUtil;
import vmware.CPBU.utils.VapiAuthenticationHelper;
import vmware.CPBU.utils.VimAuthenticationHelper;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

public class LoginService {
    private static Map<String,LoginService> loginServices = new HashMap<>();
    private VapiAuthenticationHelper vapiAuthHelper = new VapiAuthenticationHelper();
    private VimAuthenticationHelper vimAuthHelper = new VimAuthenticationHelper();
    private StubConfiguration sessionStubConfig;
    private boolean skipServerVerification=true;
    private String truststorePath="";
    private String truststorePassword="";
    private String server="";
    private String username="";
    private String password="";

    public static LoginService getLoginService(String serverUrl) {
        return loginServices.get(serverUrl);
    }
    public LoginService setServer(String serverUrl) {
        this.server = serverUrl;
        return this;
    }

    public LoginService setUsername(String username) {
        this.username = username;
        return this;
    }

    public LoginService setPassword(String passwd) {
        this.password = passwd;
        return this;
    }

    public LoginService setSkipServerVerification(boolean skipVerification) {
        this.skipServerVerification = skipVerification;
        return this;
    }

    public LoginService setTruststorePassword(String passwd) {
        this.truststorePassword = passwd;
        return this;
    }

    public LoginService setTruststorePath(String path) {
        this.truststorePath = path;
        return this;
    }

    public VapiAuthenticationHelper getVapiAuthHelper() {
        return vapiAuthHelper;
    }


    public StubConfiguration getSessionStubConfig() {
        return sessionStubConfig;
    }

    public VimAuthenticationHelper getVimAuthHelper() {
        return vimAuthHelper;
    }

    public void login() throws Exception {
        HttpConfiguration httpConfig = buildHttpConfiguration();
        this.sessionStubConfig =
                vapiAuthHelper.loginByUsernameAndPassword(server, username, password, httpConfig);
        this.vimAuthHelper.loginByUsernameAndPassword(server, username, password);
        loginServices.put(server,this);
    }


    /**
     * Builds the Http settings to be applied for the connection to the server.
     * @return http configuration
     * @throws Exception
     */
    protected HttpConfiguration buildHttpConfiguration() throws Exception {
        HttpConfiguration httpConfig =
                new HttpConfiguration.Builder()
                        .setSslConfiguration(buildSslConfiguration())
                        .getConfig();

        return httpConfig;
    }

    /**
     * Builds the SSL configuration to be applied for the connection to the
     * server
     *
     * For vApi connections:
     * If "skip-server-verification" is specified, then the server certificate
     * verification is skipped. The method retrieves the certificate
     * from specified server and adds it to an in-memory trustStore which is
     * returned.
     * If "skip-server-verification" is not specified, then it uses the
     * truststorepath and truststorepassword to load the truststore and return
     * it.
     *
     * For VIM connections:
     * If "skip-server-verification" is specified, then it trusts all the
     * VIM API connections made to the specified server.
     * If "skip-server-verification" is not specified, then it sets the System
     * environment property "javax.net.ssl.trustStore" to the path of the file
     * containing the trusted server certificates.
     *
     *<p><b>
     * Note: Below code circumvents SSL trust if "skip-server-verification" is
     * specified. Circumventing SSL trust is unsafe and should not be used
     * in production software. It is ONLY FOR THE PURPOSE OF DEVELOPMENT
     * ENVIRONMENTS.
     *<b></p>
     * @return SSL configuration
     * @throws Exception
     */
    protected HttpConfiguration.SslConfiguration buildSslConfiguration() throws Exception {
        HttpConfiguration.SslConfiguration sslConfig;

        if(this.skipServerVerification) {
            /*
             * Below method enables all VIM API connections to the server
             * without validating the server certificates.
             *
             * Note: Below code is to be used ONLY IN DEVELOPMENT ENVIRONMENTS.
             * Circumventing SSL trust is unsafe and should not be used in
             * production software.
             */
            SslUtil.trustAllHttpsCertificates();

            /*
             * Below code enables all vAPI connections to the server
             * without validating the server certificates..
             *
             * Note: Below code is to be used ONLY IN DEVELOPMENT ENVIRONMENTS.
             * Circumventing SSL trust is unsafe and should not be used in
             * production software.
             */
            sslConfig = new HttpConfiguration.SslConfiguration.Builder()
                    .disableCertificateValidation()
                    .disableHostnameVerification()
                    .getConfig();
        } else {
            /*
             * Set the system property "javax.net.ssl.trustStore" to
             * the truststorePath
             */
            System.setProperty("javax.net.ssl.trustStore", this.truststorePath);
            KeyStore trustStore =
                    SslUtil.loadTrustStore(this.truststorePath,
                            this.truststorePassword);
            HttpConfiguration.KeyStoreConfig keyStoreConfig =
                    new HttpConfiguration.KeyStoreConfig("", this.truststorePassword);
            sslConfig =
                    new HttpConfiguration.SslConfiguration.Builder()
                            .setKeyStore(trustStore)
                            .setKeyStoreConfig(keyStoreConfig)
                            .getConfig();
        }

        return sslConfig;
    }

    /**
     * Logs out of the server
     * @throws Exception
     */
    public void logout() throws Exception {
        this.vapiAuthHelper.logout();
        this.vimAuthHelper.logout();
    }
}
