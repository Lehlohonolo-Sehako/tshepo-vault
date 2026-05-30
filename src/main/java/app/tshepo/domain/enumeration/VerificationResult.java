package app.tshepo.domain.enumeration;

/**
 * The VerificationResult enumeration.
 */
public enum VerificationResult {
    VALID,
    INVALID_SIGNATURE,
    EXPIRED,
    REVOKED,
    MALFORMED,
    UNTRUSTED_ISSUER,
}
