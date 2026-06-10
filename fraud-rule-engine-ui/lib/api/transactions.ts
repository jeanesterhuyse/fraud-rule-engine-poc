import { apiClient } from './client';
import { TriggeredTransaction, PageResponse } from '@/types/api';

export const transactionsService = {
  getAll: async (params?: {
    page?: number;
    size?: number;
    ruleType?: string;
    customerId?: string;
  }): Promise<PageResponse<TriggeredTransaction>> => {
    const response = await apiClient.get<PageResponse<TriggeredTransaction>>(
      '/transactions/all',
      { params }
    );
    return response.data;
  },

  getById: async (id: number): Promise<TriggeredTransaction> => {
    const response = await apiClient.get<TriggeredTransaction>(`/transactions/${id}`);
    return response.data;
  },

  getRecent: async (limit = 50): Promise<TriggeredTransaction[]> => {
    const response = await apiClient.get<TriggeredTransaction[]>(
      '/transactions/recent',
      { params: { limit } }
    );
    return response.data;
  },
};
