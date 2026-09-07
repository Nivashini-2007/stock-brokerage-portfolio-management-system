import { client } from './client';

export const ledgerApi = {
  history: (clientId) => client.get(`/ledger/${clientId}`).then((r) => r.data),
  transfer: (payload) => client.post('/ledger/transfer', payload).then((r) => r.data),
};
