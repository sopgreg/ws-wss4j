/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wss4j.dom.components.crypto;

import org.apache.wss4j.dom.WSSecurityEngine;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.common.KeystoreCallbackHandler;
import org.apache.wss4j.dom.common.SOAPUtil;
import org.apache.wss4j.dom.common.SecurityTestUtil;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.util.XMLUtils;
import org.apache.wss4j.dom.message.WSSecEncrypt;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.apache.wss4j.dom.message.WSSecSignature;
import org.apache.xml.security.utils.Base64;
import org.w3c.dom.Document;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * This is a test for WSS-86 - "CryptoBase.splitAndTrim does not take into account the format of a 
 * DN constructed by different providers":
 * http://issues.apache.org/jira/browse/WSS-86
 * 
 * Keystore: keys\wss86.keystore
 * Password: security
 * Generated by:
 * 
 * keytool -genkey -validity 3650 -alias wss86 -keyalg RSA -keystore wss86.keystore 
 * -dname "1.2.840.113549.1.9.1=#16125765726e6572406578616d706c652e636f6d,CN=Werner,
 * OU=WSS4J,O=Apache,L=Munich,ST=Bayern,C=DE"
 */
public class CryptoProviderTest extends org.junit.Assert {
    private static final org.slf4j.Logger LOG = 
        org.slf4j.LoggerFactory.getLogger(CryptoProviderTest.class);
    private WSSecurityEngine secEngine = new WSSecurityEngine();
    private CallbackHandler callbackHandler = new KeystoreCallbackHandler();
    private Crypto crypto;

    public CryptoProviderTest() throws Exception {
        secEngine.getWssConfig(); //make sure BC gets registered
        crypto = CryptoFactory.getInstance("wss86.properties");
    }

    @org.junit.AfterClass
    public static void cleanup() throws Exception {
        SecurityTestUtil.cleanup();
    }
    
    /**
     * Test signing a SOAP message using a cert with an OID
     */
    @org.junit.Test
    public void testSignatureOID() throws Exception {
        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecSignature sign = new WSSecSignature();
        sign.setUserInfo("wss86", "security");
        sign.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = sign.build(doc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            String outputString = 
                XMLUtils.PrettyDocumentToString(signedDoc);
            LOG.debug(outputString);
        }
        verify(signedDoc);
    }
    
    /**
     * Like before but substitute in an "EMAILADDRESS" instead of the OID
     * @throws Exception
     */
    @org.junit.Test
    public void testSignatureEmailAddress() throws Exception {
        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecSignature sign = new WSSecSignature();
        sign.setUserInfo("wss86", "security");
        sign.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = sign.build(doc, crypto, secHeader);
        
        String outputString = XMLUtils.PrettyDocumentToString(signedDoc);
        outputString = 
            outputString.replace("1.2.840.113549.1.9.1=#16125765726e6572406578616d706c652e636f6d",
                             "EMAILADDRESS=Werner@example.com");
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        InputStream is = new ByteArrayInputStream(outputString.getBytes());
        Document parsedDoc = dbf.newDocumentBuilder().parse(is);
        verify(parsedDoc);
    }
    
    /**
     * Like before but substitute in an "E" instead of the OID
     * @throws Exception
     */
    @org.junit.Test
    public void testSignatureOtherEmailAddress() throws Exception {
        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecSignature sign = new WSSecSignature();
        sign.setUserInfo("wss86", "security");
        sign.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = sign.build(doc, crypto, secHeader);
        
        String outputString = XMLUtils.PrettyDocumentToString(signedDoc);
        outputString = 
            outputString.replace("1.2.840.113549.1.9.1=#16125765726e6572406578616d706c652e636f6d",
                             "E=Werner@example.com");
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        InputStream is = new ByteArrayInputStream(outputString.getBytes());
        Document parsedDoc = dbf.newDocumentBuilder().parse(is);
        verify(parsedDoc);
    }
    
    /**
     * Test loading a certificate using BouncyCastle, and using it to encrypt a message, but
     * decrypt the message using the Java Keystore provider
     */
    @org.junit.Test
    public void testInterop() throws Exception {
        // 
        // This cert corresponds to the cert in wss86.keystore
        // Extracted with:
        // keytool -export -rfc -keystore wss86.keystore -alias wss86 -file wss86.cer
        //
        byte[] certBytes = 
            Base64.decode(
                "MIICfDCCAeUCBEnHoGMwDQYJKoZIhvcNAQEEBQAwgYQxCzAJBgNVBAYTAkRFMQ8wDQYDVQQIEwZC"
                + "YXllcm4xDzANBgNVBAcTBk11bmljaDEPMA0GA1UEChMGQXBhY2hlMQ4wDAYDVQQLEwVXU1M0SjEP"
                + "MA0GA1UEAxMGV2VybmVyMSEwHwYJKoZIhvcNAQkBFhJXZXJuZXJAZXhhbXBsZS5jb20wHhcNMDkw"
                + "MzIzMTQ0NDUxWhcNMTkwMzIxMTQ0NDUxWjCBhDELMAkGA1UEBhMCREUxDzANBgNVBAgTBkJheWVy"
                + "bjEPMA0GA1UEBxMGTXVuaWNoMQ8wDQYDVQQKEwZBcGFjaGUxDjAMBgNVBAsTBVdTUzRKMQ8wDQYD"
                + "VQQDEwZXZXJuZXIxITAfBgkqhkiG9w0BCQEWEldlcm5lckBleGFtcGxlLmNvbTCBnzANBgkqhkiG"
                + "9w0BAQEFAAOBjQAwgYkCgYEA3uRplw7q8y/sIR541uCrlbIMzJHXCRU3nQreGNr6dM49/LxHYffQ"
                + "Ex99chQh+wR6fwArFlziDRNnqslOy8zKMfGbaBaR41ZZrxvkSsIwzOhD6yAPgKVQL2vTmJAbdZ35"
                + "GwcOW8oe7l+NV9qmv7yrr5OhqDhFh36WhgjVLiwmP/cCAwEAATANBgkqhkiG9w0BAQQFAAOBgQBP"
                + "PnR2BYn7DKn/SkU8XTgf9g2NoYcMyvQOB+Uo25/QzDdMk6HKmHl0+7mh7RAtXcBz2YqC3WbQW5U3"
                + "KmOH6fVxB8hw6xalBjs2YpnBx4gaHAws35KlAfkGVVe5wqnrI7ER7RBYO/7Gr7uCUq11QrGyEG8/"
                + "yIXktaFLxgD2R4hpfA=="
            );
        CertificateFactory factory = 
            CertificateFactory.getInstance("X.509", "BC");
        X509Certificate cert = 
            (X509Certificate)factory.generateCertificate(
                new java.io.ByteArrayInputStream(certBytes)
            );

        WSSecEncrypt encrypt = new WSSecEncrypt();
        encrypt.setUseThisCert(cert);
        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document encryptedDoc = encrypt.build(doc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            String outputString = 
                XMLUtils.PrettyDocumentToString(encryptedDoc);
            LOG.debug(outputString);
        }
        verify(encryptedDoc);
        
    }
    
    
    /**
     * Test loading a certificate using BouncyCastle, and using it to encrypt a message, but
     * decrypt the message using the Java Keystore provider. In this case though the cert doesn't
     * correspond with the cert in wss86.keystore.
     */
    @org.junit.Test
    public void testBadInterop() throws Exception {
        byte[] certBytes = 
            Base64.decode(
                "MIIDNDCCAp2gAwIBAgIBEDANBgkqhkiG9w0BAQQFADBmMQswCQYDVQQGEwJERTEPMA0GA1UECBMG"
                + "QmF5ZXJuMQ8wDQYDVQQHEwZNdW5pY2gxDTALBgNVBAoTBEhvbWUxFTATBgNVBAsTDEFwYWNoZSBX"
                + "U1M0SjEPMA0GA1UEAxMGV2VybmVyMB4XDTA4MDQwNDE5MzIxOFoXDTEwMDQwNDE5MzIxOFowYTEL"
                + "MAkGA1UEBhMCREUxDzANBgNVBAgTBkJheWVybjEPMA0GA1UEBxMGTXVuaWNoMQ8wDQYDVQQKEwZB"
                + "cGFjaGUxDjAMBgNVBAsTBVdTUzRKMQ8wDQYDVQQDEwZXZXJuZXIwgZ8wDQYJKoZIhvcNAQEBBQAD"
                + "gY0AMIGJAoGBAINlL3/k0H/zvknpBtLo8jzXwx/IJU/CGSv6MsqJZ2fyZ6kpLlXCuSBUZ/tfkdxp"
                + "uzhYq/Sc7A8csIk9gDf9RUbrhK0qKw0VP6DoCIJjS5IeN+NeJkx8YjmzLPmZqLYbNPXr/hy8CRrR"
                + "6CqLTTSkBwoEJ+cDkfZrdH2/bND0FEIZAgMBAAGjgfYwgfMwCQYDVR0TBAIwADAsBglghkgBhvhC"
                + "AQ0EHxYdT3BlblNTTCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYEFFSZXv0I5bG7XPEw"
                + "jylwG3lmZGdiMIGYBgNVHSMEgZAwgY2AFL/FsHHolGIMacU1TZW/88Bd2EL6oWqkaDBmMQswCQYD"
                + "VQQGEwJERTEPMA0GA1UECBMGQmF5ZXJuMQ8wDQYDVQQHEwZNdW5pY2gxDTALBgNVBAoTBEhvbWUx"
                + "FTATBgNVBAsTDEFwYWNoZSBXU1M0SjEPMA0GA1UEAxMGV2VybmVyggkAuBIOAWJ19mwwDQYJKoZI"
                + "hvcNAQEEBQADgYEAUiUh/wORVcQYXxIh13h3w2Btg6Kj2g6V6YO0Utc/gEYWwT310C2OuroKAwwo"
                + "HapMIIWiJRclIAiA8Hnb0Sv/puuHYD4G4NWFdiVjRord90eZJe40NMGruRmlqIRIGGKCv+wv3E6U"
                + "x1cWW862f5H9Eyrcocke2P+3GNAGy83vghA="
            );
        CertificateFactory factory = 
            CertificateFactory.getInstance("X.509", "BC");
        X509Certificate cert = 
            (X509Certificate)factory.generateCertificate(
                new java.io.ByteArrayInputStream(certBytes)
            );

        WSSecEncrypt encrypt = new WSSecEncrypt();
        encrypt.setUseThisCert(cert);
        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document encryptedDoc = encrypt.build(doc, crypto, secHeader);
        
        if (LOG.isDebugEnabled()) {
            String outputString = 
                XMLUtils.PrettyDocumentToString(encryptedDoc);
            LOG.debug(outputString);
        }
        try {
            verify(encryptedDoc);
            fail("Failure expected on encryption with a key that does not exist in the keystore");
        } catch (Exception ex) {
            // expected
        }
        
    }
    
    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param doc 
     * @throws Exception Thrown when there is a problem in verification
     */
    private void verify(Document doc) throws Exception {
        secEngine.processSecurityHeader(doc, null, callbackHandler, crypto);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Verfied and decrypted message:");
            String outputString = 
                XMLUtils.PrettyDocumentToString(doc);
            LOG.debug(outputString);
        }
    }

}
