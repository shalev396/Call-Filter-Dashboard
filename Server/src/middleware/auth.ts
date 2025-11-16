import { APIGatewayProxyEventV2 } from "aws-lambda";

export interface AuthUser {
  sub: string;
  email: string | undefined;
  username: string | undefined;
}

/**
 * Middleware to extract authenticated user from JWT
 *
 * PRODUCTION (IS_LOCAL=false):
 * - API Gateway validates JWT and adds claims to event.requestContext.authorizer.jwt
 * - Middleware extracts user from validated claims
 *
 * LOCAL (IS_LOCAL=true):
 * - API Gateway validation bypassed (noAuth: true)
 * - Middleware parses JWT from Authorization header (NO VALIDATION)
 * - Extracts sub, email, username from token payload
 */
export const extractAuthUser = (
  event: APIGatewayProxyEventV2
): AuthUser | null => {
  const isLocal = process.env.IS_LOCAL === "true";

  if (isLocal) {
    // Local dev - parse JWT from Authorization header (no validation!)
    const authHeader =
      event.headers?.["authorization"] || event.headers?.["Authorization"];

    if (!authHeader) {
      console.log("[Auth Middleware] Local mode - No Authorization header");
      return null;
    }

    // Extract token from "Bearer <token>"
    const token = authHeader.replace(/^Bearer\s+/i, "");

    try {
      // Parse JWT payload (base64 decode middle part)
      const parts = token.split(".");
      if (parts.length !== 3) {
        console.error("[Auth Middleware] Invalid JWT format");
        return null;
      }

      const payloadPart = parts[1];
      if (!payloadPart) {
        console.error("[Auth Middleware] Missing JWT payload");
        return null;
      }

      const payload = JSON.parse(
        Buffer.from(payloadPart, "base64").toString("utf-8")
      );

      const user: AuthUser = {
        sub: payload.sub || "unknown",
        email: payload.email,
        username: payload["cognito:username"],
      };

      console.log("[Auth Middleware] Local mode - Extracted user from JWT:", {
        sub: user.sub,
        email: user.email,
      });

      return user;
    } catch (error) {
      console.error("[Auth Middleware] Failed to parse JWT:", error);
      return null;
    }
  }

  // Production mode - extract from API Gateway authorizer context
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const authorizer = (event.requestContext as any)?.["authorizer"];

  if (!authorizer?.["jwt"]) {
    console.error("[Auth Middleware] No JWT authorizer context found");
    return null;
  }

  const claims = authorizer["jwt"]["claims"];

  if (!claims || !claims["sub"]) {
    console.error("[Auth Middleware] No sub claim found in JWT");
    return null;
  }

  const user: AuthUser = {
    sub: claims["sub"] as string,
    email: claims["email"] as string | undefined,
    username: claims["cognito:username"] as string | undefined,
  };

  console.log("[Auth Middleware] Production mode - Authenticated user:", {
    sub: user.sub,
    email: user.email,
  });

  return user;
};

/**
 * Helper to require authentication
 * Throws error if user not found
 */
export const requireAuth = (event: APIGatewayProxyEventV2): AuthUser => {
  const user = extractAuthUser(event);

  if (!user) {
    throw new Error("Unauthorized: No valid user found");
  }

  return user;
};
