import { client } from './client';

export const researchApi = {
  published: () => client.get('/research').then((r) => r.data),
  mine: () => client.get('/research/mine').then((r) => r.data),
  pending: () => client.get('/research/pending').then((r) => r.data),
  create: (payload) => client.post('/research', payload).then((r) => r.data),
  approve: (id, remarks) =>
    client.post(`/research/${id}/approve`, remarks ? { remarks } : undefined).then((r) => r.data),
  reject: (id, remarks) =>
    client.post(`/research/${id}/reject`, remarks ? { remarks } : undefined).then((r) => r.data),
};
