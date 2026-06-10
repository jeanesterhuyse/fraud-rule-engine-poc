import { apiClient } from './client';
import { DashboardSummary } from '@/types/api';

export const dashboardService = {
  getSummary: async (): Promise<DashboardSummary> => {
    const response = await apiClient.get<DashboardSummary>('/dashboard/summary');
    return response.data;
  },

  getTopTriggeredRules: async (limit = 10, days = 7) => {
    const response = await apiClient.get('/dashboard/top-triggered-rules', {
      params: { limit, days },
    });
    return response.data;
  },

  getRuleTypeDistribution: async () => {
    const response = await apiClient.get('/dashboard/rule-type-distribution');
    return response.data;
  },
};
