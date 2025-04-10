package FeedStudy.StudyFeed.global.jwt;

import FeedStudy.StudyFeed.global.exception.ErrorCode;
import FeedStudy.StudyFeed.global.exception.exceptiontype.KeyException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String PRIVATE_KEY_PATH = "src/main/resources/private.key";
    private static final String PUBLIC_KEY_PATH = "src/main/resources/public.key";

    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 3600000;
    private static final long REFRESH_TOKEN_VALIDITY_SECONDS = 3600000 * 24 * 7;


    private PrivateKey privateKey;
    private PublicKey publicKey;

    public JwtUtil() {

        try {
            generateKeysIfAbsent();
            privateKey = getPrivateKey();
            publicKey = getPublicKey();
        } catch (Exception e) {
            throw new KeyException(ErrorCode.KEY_LOAD_ERROR);
        }
    }


    private void generateKeysIfAbsent() throws NoSuchAlgorithmException, IOException {
        Path privateKeyPath = Paths.get(PRIVATE_KEY_PATH);
        Path publicKeyPath = Paths.get(PUBLIC_KEY_PATH);

        if(!Files.exists(privateKeyPath) || !Files.exists(publicKeyPath)) {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            Files.write(privateKeyPath, keyPair.getPrivate().getEncoded());
            Files.write(publicKeyPath, keyPair.getPublic().getEncoded());
        }
    }

    private PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Path path = Paths.get(PUBLIC_KEY_PATH);
        if(!Files.exists(path)) {
            throw new KeyException(ErrorCode.KEY_NOT_EXIST);
        }

        byte[] bytes = Files.readAllBytes(path);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePublic(spec);
    }


    private PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Path path = Paths.get(PRIVATE_KEY_PATH);
        if(!Files.exists(path)) {
            throw new KeyException(ErrorCode.KEY_NOT_EXIST);
        }
        byte[] bytes = Files.readAllBytes(path);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        return rsa.generatePrivate(spec);
    }

    private String createJwt(String subject, String role, long expirationTime) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String createAccessToken(String email, String role) {
        return createJwt(email, role, ACCESS_TOKEN_VALIDITY_SECONDS);
    }

    public String createRefreshToken(String email, String role) {
        return createJwt(email, role, REFRESH_TOKEN_VALIDITY_SECONDS);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getTokenExpiration(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.getTime() - System.currentTimeMillis(); // 남은 유효시간 (ms)
        } catch (Exception e) {
            return -1; // 유효하지 않은 경우 -1 반환
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}
