package com.example.uc3;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.server.webpush.WebPush;

/**
 * Provides a {@link WebPush} bean configured with the VAPID key pair from
 * {@code application.properties}. If the configured keys are missing or are the
 * placeholder values, a fresh ephemeral pair is generated for the demo and the
 * public key is logged so the next run can reuse it (and previously issued
 * subscriptions can survive restarts).
 */
@Configuration
public class WebPushConfig {

    private static final Logger LOG = LoggerFactory
            .getLogger(WebPushConfig.class);

    static final String GENERATE_MARKER = "__GENERATE__";

    @Bean
    public WebPush webPush(@Value("${app.webpush.public-key:}") String pub,
            @Value("${app.webpush.private-key:}") String priv,
            @Value("${app.webpush.subject:mailto:demo@vaadin.com}") String subject) {

        if (isPlaceholder(pub) || isPlaceholder(priv)) {
            LOG.warn("VAPID keys not configured — generating an ephemeral "
                    + "pair for this run. Persist them in application.properties "
                    + "(app.webpush.public-key / app.webpush.private-key) so "
                    + "subscriptions survive restarts.");
            String[] generated = generateVapidKeys();
            pub = generated[0];
            priv = generated[1];
            // The public key is non-sensitive and is logged so the operator
            // can copy it into application.properties. The private key is
            // only emitted at DEBUG so production logs never carry it.
            LOG.info("Generated VAPID public key:  {}", pub);
            LOG.debug("Generated VAPID private key: {}", priv);
        }
        return new WebPush(pub, priv, subject);
    }

    private static boolean isPlaceholder(String value) {
        return value.isBlank() || GENERATE_MARKER.equals(value.trim());
    }

    private static String[] generateVapidKeys() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
            gen.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair kp = gen.generateKeyPair();

            String pub = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    extractUncompressedPublicKey((ECPublicKey) kp.getPublic()));
            // VAPID requires the private scalar as exactly 32 bytes; trim/pad
            // to drop a sign byte or restore leading zeros from BigInteger.
            byte[] privBytes = trimTo(
                    ((ECPrivateKey) kp.getPrivate()).getS().toByteArray(), 32);
            String priv = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(privBytes);
            return new String[] { pub, priv };
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate VAPID keys", e);
        }
    }

    private static byte[] extractUncompressedPublicKey(ECPublicKey key) {
        byte[] x = trimTo(key.getW().getAffineX().toByteArray(), 32);
        byte[] y = trimTo(key.getW().getAffineY().toByteArray(), 32);
        byte[] out = new byte[65];
        out[0] = 0x04;
        System.arraycopy(x, 0, out, 1, 32);
        System.arraycopy(y, 0, out, 33, 32);
        return out;
    }

    private static byte[] trimTo(byte[] in, int len) {
        if (in.length == len) {
            return in;
        }
        byte[] out = new byte[len];
        if (in.length > len) {
            System.arraycopy(in, in.length - len, out, 0, len);
        } else {
            System.arraycopy(in, 0, out, len - in.length, in.length);
        }
        return out;
    }
}
