package org.jmxtrans.embedded.util.ssl;

import java.security.cert.X509Certificate;

public class CertificateLoggingHelper {

    public void appendCertificateDescription(StringBuilder logBuilder, X509Certificate cert) {
        logBuilder.append("SubjectDN = '").append(cert.getSubjectDN()).append("'");
        logBuilder.append(", IssuerDN = '").append(cert.getIssuerDN()).append("'");
        logBuilder.append(", SerialNumber = '").append(cert.getSerialNumber()).append("'");
    }

}
