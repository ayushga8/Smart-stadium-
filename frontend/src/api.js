export const API_BASE = 'https://smart-stadium-api-l7ut.onrender.com';

export function getAuthHeaders() {
  const token = localStorage.getItem('accessToken');
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

export function googleLogin() {
  window.location.href = `${API_BASE}/oauth2/authorization/google`;
}

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
