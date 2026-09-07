import { client } from './client';

export const notificationsApi = {
  forClient: (clientId) => client.get(`/notifications/${clientId}`).then((r) => r.data),
};
