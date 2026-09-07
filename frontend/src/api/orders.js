import { client } from './client';

export const ordersApi = {
  place: (payload) => client.post('/orders', payload).then((r) => r.data),
  getById: (id) => client.get(`/orders/${id}`).then((r) => r.data),
  byClient: (clientId) => client.get(`/orders/client/${clientId}`).then((r) => r.data),
  book: () => client.get('/orders/book').then((r) => r.data),
  cancel: (id) => client.delete(`/orders/${id}`).then((r) => r.data),
};
