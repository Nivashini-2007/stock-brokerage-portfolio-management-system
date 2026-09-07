import { client } from './client';

export const marginApi = {
  get: (clientId) => client.get(`/margin/${clientId}`).then((r) => r.data),
};
