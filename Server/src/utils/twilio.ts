import https from "https";
import { Config } from "../types/index.js";

const TWILIO_ACCOUNT_SID = process.env.TWILIO_ACCOUNT_SID;
const TWILIO_AUTH_TOKEN = process.env.TWILIO_AUTH_TOKEN;
const TWILIO_ASSET_SERVICE_SID = process.env.TWILIO_ASSET_SERVICE_SID;

const CONFIG_ASSET_PATH = "/config.json";

/**
 * Make authenticated request to Twilio API
 */
const twilioRequest = (
  method: string,
  path: string,
  data?: unknown
): Promise<unknown> => {
  return new Promise((resolve, reject) => {
    const auth = Buffer.from(
      `${TWILIO_ACCOUNT_SID}:${TWILIO_AUTH_TOKEN}`
    ).toString("base64");

    const options: https.RequestOptions = {
      hostname: "api.twilio.com",
      path,
      method,
      headers: {
        Authorization: `Basic ${auth}`,
        "Content-Type": "application/json",
      },
    };

    const req = https.request(options, (res) => {
      let body = "";
      res.on("data", (chunk) => (body += chunk));
      res.on("end", () => {
        try {
          resolve(JSON.parse(body));
        } catch {
          resolve(body);
        }
      });
    });

    req.on("error", reject);

    if (data) {
      req.write(JSON.stringify(data));
    }

    req.end();
  });
};

/**
 * Get configuration from Twilio Assets
 */
export const getConfig = async (): Promise<Config> => {
  try {
    const configData = await twilioRequest(
      "GET",
      `/2010-04-01/Accounts/${TWILIO_ACCOUNT_SID}/Assets/${TWILIO_ASSET_SERVICE_SID}${CONFIG_ASSET_PATH}.json`
    ) as Config | null;

    return configData || getDefaultConfig();
  } catch (error) {
    console.error("Error fetching config:", error);
    return getDefaultConfig();
  }
};

/**
 * Update configuration in Twilio Assets
 */
export const updateConfig = async (config: Config): Promise<void> => {
  await twilioRequest(
    "POST",
    `/2010-04-01/Accounts/${TWILIO_ACCOUNT_SID}/Assets/${TWILIO_ASSET_SERVICE_SID}${CONFIG_ASSET_PATH}.json`,
    config
  );
};

/**
 * Get default configuration
 */
const getDefaultConfig = (): Config => {
  return {
    whitelist: [],
    schedule: {
      enabled: true,
      timezone: "America/New_York",
      allowedTimes: [
        {
          dayOfWeek: [1, 2, 3, 4, 5], // Monday-Friday
          startTime: "09:00",
          endTime: "17:00",
        },
      ],
    },
  };
};
