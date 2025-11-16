import { APIGatewayProxyEventV2, APIGatewayProxyResultV2 } from 'aws-lambda';
import type { APIResponse } from '../types/index.js';

export const createResponse = <T = unknown>(
  statusCode: number,
  body: APIResponse<T>
): APIGatewayProxyResultV2 => {
  return {
    statusCode,
    headers: {
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': process.env.CORS_ORIGIN,
      'Access-Control-Allow-Credentials': 'true',
    },
    body: JSON.stringify(body),
  };
};

export const createSuccessResponse = <T = unknown>(
  data: T,
  message?: string
): APIGatewayProxyResultV2 => {
  const response: APIResponse<T> = {
    success: true,
    data,
  };
  if (message) {
    response.message = message;
  }
  return createResponse(200, response);
};

export const createErrorResponse = (
  statusCode: number,
  error: string
): APIGatewayProxyResultV2 => {
  return createResponse(statusCode, {
    success: false,
    error,
  });
};

export const parseBody = <T = unknown>(event: APIGatewayProxyEventV2): T | null => {
  try {
    return event.body ? JSON.parse(event.body) : null;
  } catch {
    return null;
  }
};

