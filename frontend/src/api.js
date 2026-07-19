const API_BASE = 'https://smart-stadium-api-l7ut.onrender.com';

function getAuthHeaders() {
  const token = localStorage.getItem('accessToken');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

export async function requestOtp(email) {
  const res = await fetch(`${API_BASE}/auth/otp/request`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Request failed' }));
    throw err;
  }
  return true;
}

export async function verifyOtp(email, otp) {
  const res = await fetch(`${API_BASE}/auth/otp/verify`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ email, otp }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Verification failed' }));
    throw err;
  }
  return res.json();
}

export function googleLogin() {
  window.location.href = `${API_BASE}/oauth2/authorization/google`;
}

// === New API endpoints ===

export async function sendChatMessage(message) {
  const res = await fetch(`${API_BASE}/api/ai/chat`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ message }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: 'Chat failed' }));
    throw err;
  }
  return res.json();
}

export async function getChatHistory() {
  const res = await fetch(`${API_BASE}/api/ai/history`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) return [];
  return res.json();
}

export async function getMatches() {
  const res = await fetch(`${API_BASE}/api/matches`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) return [];
  return res.json();
}

export async function getStadiumMap() {
  const res = await fetch(`${API_BASE}/api/stadium/map`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) return { zones: [], amenities: [] };
  return res.json();
}

export async function getCrowdData() {
  const res = await fetch(`${API_BASE}/api/stadium/crowd`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) return [];
  return res.json();
}

export async function getUserProfile() {
  const res = await fetch(`${API_BASE}/api/user/profile`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) return null;
  return res.json();
}
