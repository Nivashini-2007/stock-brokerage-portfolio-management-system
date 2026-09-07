import { client } from './client';

export const KNOWN_SYMBOLS = ['RELIANCE', 'TCS', 'INFY', 'HDFCBANK', 'SBIN'];

export const marketApi = {
  quote: (symbol) => client.get(`/market/quote/${symbol}`).then((r) => r.data),
  chart: (symbol) => client.get(`/market/chart/${symbol}`).then((r) => r.data),
  createOrUpdateStock: (payload) => client.post('/market/stocks', payload).then((r) => r.data),
  setCircuitHalt: (symbol, halted) =>
    client
      .patch(`/market/quote/${symbol}/circuit-halt`, null, { params: { halted } })
      .then((r) => r.data),
};
