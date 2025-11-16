import {
  CognitoIdentityProviderClient,
  AdminInitiateAuthCommand,
  InitiateAuthCommand,
  ForgotPasswordCommand,
  ConfirmForgotPasswordCommand,
  AdminGetUserCommand,
} from '@aws-sdk/client-cognito-identity-provider';

const client = new CognitoIdentityProviderClient({
  region: process.env.AWS_REGION,
});

const USER_POOL_ID = process.env.COGNITO_USER_POOL_ID;
const CLIENT_ID = process.env.COGNITO_CLIENT_ID;

export const authenticateUser = async (email: string, password: string) => {
  const command = new AdminInitiateAuthCommand({
    UserPoolId: USER_POOL_ID,
    ClientId: CLIENT_ID,
    AuthFlow: 'ADMIN_NO_SRP_AUTH',
    AuthParameters: {
      USERNAME: email,
      PASSWORD: password,
    },
  });

  const response = await client.send(command);
  return response.AuthenticationResult;
};

export const refreshUserToken = async (refreshToken: string) => {
  const command = new InitiateAuthCommand({
    ClientId: CLIENT_ID,
    AuthFlow: 'REFRESH_TOKEN_AUTH',
    AuthParameters: {
      REFRESH_TOKEN: refreshToken,
    },
  });

  const response = await client.send(command);
  return response.AuthenticationResult;
};

export const initiatePasswordReset = async (email: string) => {
  const command = new ForgotPasswordCommand({
    ClientId: CLIENT_ID,
    Username: email,
  });

  await client.send(command);
};

export const confirmPasswordReset = async (
  email: string,
  code: string,
  newPassword: string
) => {
  const command = new ConfirmForgotPasswordCommand({
    ClientId: CLIENT_ID,
    Username: email,
    ConfirmationCode: code,
    Password: newPassword,
  });

  await client.send(command);
};

export const getUserInfo = async (email: string) => {
  const command = new AdminGetUserCommand({
    UserPoolId: USER_POOL_ID,
    Username: email,
  });

  const response = await client.send(command);
  return response;
};

