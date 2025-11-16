/**
 * Call Filter Function - Determines if incoming call should be forwarded
 * 
 * This function integrates with your Call Filter Dashboard backend API
 * to fetch the whitelist and schedule configuration dynamically.
 * 
 * @param {object} context - Twilio context (environment variables)
 * @param {object} event - Incoming call event data
 * @param {function} callback - Callback to return response to Twilio Studio
 */

// ===== Israel DST Helpers (from your original code) =====
function lastSunday(year, month) {
  const d = new Date(Date.UTC(year, month + 1, 0, 12));
  const day = d.getUTCDay();
  d.setUTCDate(d.getUTCDate() - day);
  return d.getUTCDate();
}

function fridayBeforeLastSundayOfMarch(year) {
  const lastSun = lastSunday(year, 2);
  const sunday = new Date(Date.UTC(year, 2, lastSun, 12));
  const friday = new Date(sunday.getTime() - 2 * 24 * 3600 * 1000);
  return friday.getUTCDate();
}

function israelDstBoundariesUtc(year) {
  const startDay = fridayBeforeLastSundayOfMarch(year);
  const endDay = lastSunday(year, 9);
  const startUtc = new Date(Date.UTC(year, 2, startDay, 0, 0, 0));
  const endUtc = new Date(Date.UTC(year, 9, endDay, 23, 0, 0) - 24 * 3600 * 1000);
  return { startUtc, endUtc };
}

function isIsraelDST(dateUtc) {
  const { startUtc, endUtc } = israelDstBoundariesUtc(dateUtc.getUTCFullYear());
  return dateUtc >= startUtc && dateUtc < endUtc;
}

function israelUtcOffsetHours(dateUtc) {
  return isIsraelDST(dateUtc) ? 3 : 2;
}

function israelLocalHourToUtcHour(localHour, dateUtc) {
  const offset = israelUtcOffsetHours(dateUtc);
  let utcHour = (localHour - offset) % 24;
  if (utcHour < 0) utcHour += 24;
  return utcHour;
}

function isUtcHourInWindow(currentUtcHour, fromUtcHour, toUtcHour) {
  if (fromUtcHour === toUtcHour) return true;
  if (fromUtcHour < toUtcHour)
    return currentUtcHour >= fromUtcHour && currentUtcHour < toUtcHour;
  return currentUtcHour >= fromUtcHour || currentUtcHour < toUtcHour;
}

/**
 * NEW: Fetch configuration from your backend API
 * This replaces hardcoded whitelist/schedule with dynamic config
 */
async function fetchConfig(context) {
  const axios = require('axios');
  const API_URL = process.env.API_URL || context.API_URL;
  const ACCESS_TOKEN = context.ACCESS_TOKEN;

  if (!API_URL) {
    console.error('API_URL not configured in Twilio environment');
    return null;
  }

  if (!ACCESS_TOKEN) {
    console.error('ACCESS_TOKEN not configured in Twilio environment');
    return null;
  }

  try {
    const response = await axios.get(`${API_URL}/api/config`, {
      headers: {
        Authorization: `Bearer ${ACCESS_TOKEN}`,
      },
    });

    if (response.data && response.data.success) {
      return response.data.data;
    }

    console.error('Failed to fetch config:', response.data);
    return null;
  } catch (error) {
    console.error('Error fetching config from API:', error.message);
    return null;
  }
}

/**
 * NEW: Check if call is within schedule windows
 * Supports multiple time windows per day (your requested format)
 */
function isWithinSchedule(schedule, nowUtc) {
  if (!schedule || !schedule.enabled) {
    return false;
  }

  const currentUtcHour = nowUtc.getUTCHours();
  const currentDay = nowUtc.getUTCDay(); // 0-6 (Sunday-Saturday)

  // Check each allowed time window
  for (const window of schedule.allowedTimes) {
    // Check if current day is in this window's allowed days
    if (!window.dayOfWeek.includes(currentDay)) {
      continue;
    }

    // Parse window times (HH:MM format to UTC hours)
    const [startHour, startMin] = window.startTime.split(':').map(Number);
    const [endHour, endMin] = window.endTime.split(':').map(Number);

    // Convert to UTC using Israel timezone offset
    const fromUtcHour = israelLocalHourToUtcHour(startHour, nowUtc);
    const toUtcHour = israelLocalHourToUtcHour(endHour, nowUtc);

    if (isUtcHourInWindow(currentUtcHour, fromUtcHour, toUtcHour)) {
      return true;
    }
  }

  return false;
}

// ===== Main Handler =====
exports.handler = async function (context, event, callback) {
  const nowUtc = new Date();
  const caller = event.From || event.from || '';

  console.log(`[isForward] Call from: ${caller} at ${nowUtc.toISOString()}`);

  try {
    if (!caller) {
      throw new Error('Missing caller number');
    }

    // Fetch configuration from backend API
    const config = await fetchConfig(context);

    if (!config) {
      console.error('[isForward] Failed to fetch config - DENYING call by default');
      const response = new Twilio.Response();
      response.appendHeader('Content-Type', 'application/json');
      response.setStatusCode(200);
      response.setBody({
        allow: false,
        reason: 'config_error',
        message: 'Could not load configuration',
      });
      return callback(null, response);
    }

    console.log(`[isForward] Config loaded: ${config.whitelist.length} whitelisted numbers`);

    // Check whitelist first - always allow
    const isWhitelisted = config.whitelist.includes(caller);
    if (isWhitelisted) {
      console.log('[isForward] Caller is whitelisted - ALLOWING');
      const response = new Twilio.Response();
      response.appendHeader('Content-Type', 'application/json');
      response.setStatusCode(200);
      response.setBody({
        allow: true,
        reason: 'whitelist',
        caller,
        nowUtc: nowUtc.toISOString(),
      });
      return callback(null, response);
    }

    // Check schedule
    const withinHours = isWithinSchedule(config.schedule, nowUtc);

    if (withinHours) {
      console.log('[isForward] Within allowed schedule - ALLOWING');
    } else {
      console.log('[isForward] Outside allowed schedule - DENYING');
    }

    const response = new Twilio.Response();
    response.appendHeader('Content-Type', 'application/json');
    response.setStatusCode(200);
    response.setBody({
      allow: withinHours,
      reason: withinHours ? 'within_hours' : 'outside_hours',
      caller,
      nowUtc: nowUtc.toISOString(),
      offset: israelUtcOffsetHours(nowUtc),
    });

    return callback(null, response);
  } catch (err) {
    console.error('[isForward] Error:', err);
    const response = new Twilio.Response();
    response.appendHeader('Content-Type', 'application/json');
    response.setStatusCode(200);
    response.setBody({
      allow: false,
      reason: 'error',
      message: err.message || 'Unhandled error',
    });
    return callback(null, response);
  }
};

