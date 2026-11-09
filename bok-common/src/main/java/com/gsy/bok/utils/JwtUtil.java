package com.gsy.bok.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public final class JwtUtil {

    /** 签发用的 RSA 私钥（PKCS#8，Base64）。 */
    private static final String PRIVATE_KEY_BASE64 =
            "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDegxYbg/YV76Yr+fSt6aliaFUC5sgNO9MMaxPxWuIfvv0CvyCDzhMXczc61+Em5wULxazrvwdISTDmz7HMFLpOemh4q2F2IL+FbNlSsW5XxgSyxyZHk8BGLj9E3qzS3i+fnBf9UhjAY/A4XHyC9Dkf5IBw98hqYx3J3NWVDxXmOZY0UbqjeOyEE9u9r7pdecxPsLTSsaHyuoimNsIvTpzfobgZcaU3pXfISmOejz0XDIJOio5O+aMbIGz6SIwTa4HqNl1CLE7mMRFicbkRs+mSg50yWJHE7+qPeQbAQWfi2QeisS3YBr6UD+xKJOopl6YFJMIepu7yJkbkk+JUXQf9AgMBAAECggEACBwF773I97ayuzKSaM41xqjfxEnN67GM+Qxj/y1dRWPsC7AUisk1Z+rijen1Qjo0NGAhc7wUE13bguX56GKvAZIzgYZZeHksEbxMVVezGk7UuAFLE1u+q7AezS9CZDMF0E7bjvdxgH2D8UHppcop1NA6oCNfsgTnvoYmYJbaqPfJ6V3cDYhg+gN8qCtNhyfRMF9m/Gyws7Xz1W1lWDG4HLZE2dVe+UX8IrxPlrtQUwGWQPhlwQi1ZpnWttZxpkdQA0QjL1dlJ2AtoTEVq1rF5gkWaBQG5UI8z/Gv9rRCMwBhDCRKGj2wX5njlL5EPdgB59BjzN1Gak8dywaNG/HRIQKBgQD30shk1ZXUmjyKwpBVr4SY5Qf7xhOYS1N4umfih0nHtbYKYqrjDAF9Qm7D11HMsLN0pIS9UgmazBj7KuspwsesSvBjEsPz4zM2feLAdiAPwzcvhdzDS/8JKgFU4yuX8GffQvWNkANZMD02fLtSFxiUlT7esa9RXDESrlzmudNIsQKBgQDl2oOOWnYXEUmNXDt0EHkaUO0QvcRywTdqcp3Bh23j2tWXuyWiNxz+reU8T3n9aimLbxxoR8DhUMWaJPwejaEtZPOB4KzYHZcfSaj6KxkaMSfVI3X30DmYOfPRwujOlFRSLAkyyx326Nv2NBuDzajy6O0qFXJOlb11k5XgYV2HDQKBgGdWvblOVuwQZK538zfHuMMIvgt+zSz0PBpboHYVc+6TFxW9jwSePQC+cvG3F6UDCdHZyIApdVfXQ5N4khcX4as2lIa7jHpmgzR1ydf3aecowBpQ9314cyCfI28CS2tQFABFK5+cLclDFENkP7g7aUSL92tlLHYAu3XOv252jmQBAoGAfQ6zrxsGzywnsZKyAkcILMpFAWvOW4VGqlLsBCS0JFmnBqPbKPQFgYeJhpPTUo6VRfjRIuePSMfSizh+N/1k6TbYyhw8zmTjGTHRjzMtuDNwcaWWhJs1Ct7VCpaaZfMMIZ017/3VMsalDsJxCfg2aNtUViZPB/SunS8MR4WSBm0CgYBZH/0sawRNkgEW47+0uWigCR9sOzH5utuYXHqWNVRBvVc/TT+LozA4fBp8rDW6g5q44CeCTC4m4fsSxxcZ7DJwUWEE59vd13MnZlv6ErIyzSuBef6saPo3kJSOUInV+q+2gEtxEbGZ2hwV+pGPSIr8hfLnFIWbiH9aYylbw9hlog==";

    /** 验签用的 RSA 公钥（X.509，Base64），与上面的私钥是一对。 */
    private static final String PUBLIC_KEY_BASE64 =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3oMWG4P2Fe+mK/n0rempYmhVAubIDTvTDGsT8VriH779Ar8gg84TF3M3OtfhJucFC8Ws678HSEkw5s+xzBS6TnpoeKthdiC/hWzZUrFuV8YEsscmR5PARi4/RN6s0t4vn5wX/VIYwGPwOFx8gvQ5H+SAcPfIamMdydzVlQ8V5jmWNFG6o3jshBPbva+6XXnMT7C00rGh8rqIpjbCL06c36G4GXGlN6V3yEpjno89FwyCToqOTvmjGyBs+kiME2uB6jZdQixO5jERYnG5EbPpkoOdMliRxO/qj3kGwEFn4tkHorEt2Aa+lA/sSiTqKZemBSTCHqbu8iZG5JPiVF0H/QIDAQAB";

    private static final PrivateKey private_key = loadPrivateKey(PRIVATE_KEY_BASE64);
    private static final PublicKey public_key = loadPublicKey(PUBLIC_KEY_BASE64);

    /** token 有效期，30 分钟。 */
    private static final Duration TTL = Duration.ofMinutes(30);

    private JwtUtil() {
    }

    public static String generate_token(Map<String, ?> claims) {
        Instant now = Instant.now();
        JwtBuilder builder = Jwts.builder();
        for (Map.Entry<String, ?> entry : claims.entrySet()) {
            builder.claim(entry.getKey(), entry.getValue());
        }
        return builder
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TTL)))
                .signWith(private_key, Jwts.SIG.RS256)
                .compact();
    }

    public static Claims parse_token(String token) {
        return Jwts.parser()
                .verifyWith(public_key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 需要换一对新密钥时调用它生成，再把结果写到上面两个常量里。
     */
    public static KeyPair generate_keypair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static PrivateKey loadPrivateKey(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new IllegalStateException("加载 JWT 私钥失败", e);
        }
    }

    private static PublicKey loadPublicKey(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new IllegalStateException("加载 JWT 公钥失败", e);
        }
    }
}
