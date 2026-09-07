import { client } from './client';

export const riskApi = {
  all: () => client.get('/risk/alerts').then((r) => r.data),
  forClient: (clientId) => client.get(`/risk/alerts/${clientId}`).then((r) => r.data),
};
