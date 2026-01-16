const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:3001';

export async function fetchDashboardData() {
  try {
    const [roomsResponse, eventsResponse, ledgerResponse] = await Promise.all([
      fetch(`${API_BASE_URL}/debug/rooms`).catch(() => null),
      fetch(`${API_BASE_URL}/debug/events`).catch(() => null),
      fetch(`${API_BASE_URL}/debug/ledger`).catch(() => null),
    ]);

    const rooms = roomsResponse ? await roomsResponse.json() : [];
    const events = eventsResponse
      ? await eventsResponse.json()
      : { rawEvents: [], normalizedEvents: [] };
    const ledger = ledgerResponse ? await ledgerResponse.json() : [];

    return {
      rooms: rooms || [],
      events,
      ledger,
    };
  } catch (error) {
    console.warn('API 不可用:', error);
    return {
      rooms: [],
      events: { rawEvents: [], normalizedEvents: [] },
      ledger: [],
    };
  }
}

export async function fetchRooms() {
  try {
    const response = await fetch(`${API_BASE_URL}/debug/rooms`);
    return response.json();
  } catch (error) {
    console.error('Failed to fetch rooms:', error);
    return [];
  }
}

export async function fetchEvents(roomId?: string) {
  try {
    const url = roomId
      ? `${API_BASE_URL}/debug/events?roomId=${roomId}`
      : `${API_BASE_URL}/debug/events`;
    const response = await fetch(url);
    return response.json();
  } catch (error) {
    console.error('Failed to fetch events:', error);
    return { rawEvents: [], normalizedEvents: [] };
  }
}

export async function fetchLedger(creatorId?: string) {
  try {
    const url = creatorId
      ? `${API_BASE_URL}/debug/ledger?creatorId=${creatorId}`
      : `${API_BASE_URL}/debug/ledger`;
    const response = await fetch(url);
    return response.json();
  } catch (error) {
    console.error('Failed to fetch ledger:', error);
    return [];
  }
}
