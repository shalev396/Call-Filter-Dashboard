# Call Filter

Smart call filtering for Twilio numbers using whitelist + schedule rules.

## Stack

- **Frontend**: Next.js + shadcn/ui + Redux
- **Backend**: AWS Lambda + Cognito + API Gateway
- **Telephony**: Twilio Functions + Studio Flows

## Quick Start

**Backend**:

```bash
cd Server && npm install && npm run deploy
# Copy Cognito IDs to .env
```

**Frontend**:

```bash
cd Client && npm install && npm run dev
# Deploy: vercel --prod
```

**Twilio**:

```bash
cd Twilio && npm install && twilio serverless:deploy
```

## Features

- Whitelist management (always forward approved numbers)
- Schedule configuration (business hours filtering)
- AWS Cognito authentication (admin-only, no signup)
- Real-time config sync to Twilio Assets

## Documentation

- **API**: `Docs/swagger.yaml` - OpenAPI specification
- **Client**: `Docs/CLIENT.md` - Frontend setup
- **Server**: `Docs/SERVER.md` - Backend setup
- **Twilio**: `Docs/TWILIO.md` - Functions & flows
- **CI/CD**: `Docs/CICD.md` - GitHub Actions deployment
