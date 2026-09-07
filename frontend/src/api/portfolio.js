import { client } from './client';

export const portfolioApi = {
  holdings: (clientId) => client.get(`/portfolio/${clientId}`).then((r) => r.data),
  all: () => client.get('/portfolio/all').then((r) => r.data),
  performance: (clientId) => client.get(`/portfolio/performance/${clientId}`).then((r) => r.data),
  taxReport: (clientId, year) =>
    client.get(`/portfolio/tax/${clientId}/year/${year}`).then((r) => r.data),
};
