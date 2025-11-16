declare global {
  namespace NodeJS {
    interface ProcessEnv {
      // AWS
      AWS_REGION: string;

      // Cognito
      COGNITO_USER_POOL_ID: string;
      COGNITO_CLIENT_ID: string;

      // Twilio
      TWILIO_ACCOUNT_SID: string;
      TWILIO_AUTH_TOKEN: string;
      TWILIO_ASSET_SERVICE_SID: string;

      // CORS
      CORS_ORIGIN: string;

      // Environment
      IS_LOCAL: "true" | "false";
    }
  }
}

export {};
