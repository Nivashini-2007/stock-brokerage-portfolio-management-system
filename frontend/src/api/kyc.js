import { client } from './client';

export const kycApi = {
  submit: (payload) => client.post('/kyc/submit', payload).then((r) => r.data),
  get: (clientId) => client.get(`/kyc/${clientId}`).then((r) => r.data),
  review: (clientId, payload) => client.post(`/kyc/${clientId}/review`, payload).then((r) => r.data),
};
