import { apiClient } from './client';
import { Rule, PageResponse } from '@/types/api';

export const rulesService = {
  getAll: async (params?: {
    page?: number;
    size?: number;
    enabled?: boolean;
    ruleType?: string;
  }): Promise<PageResponse<Rule>> => {
    const response = await apiClient.get<PageResponse<Rule>>('/rules', { params });
    return response.data;
  },

  getById: async (id: number): Promise<Rule> => {
    const response = await apiClient.get<Rule>(`/rules/${id}`);
    return response.data;
  },

  create: async (rule: Partial<Rule>): Promise<Rule> => {
    const response = await apiClient.post<Rule>('/rules', rule);
    return response.data;
  },

  update: async (id: number, rule: Partial<Rule>): Promise<Rule> => {
    const response = await apiClient.put<Rule>(`/rules/${id}`, rule);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/rules/${id}`);
  },

  enable: async (id: number): Promise<Rule> => {
    const response = await apiClient.patch<Rule>(`/rules/${id}/enable`);
    return response.data;
  },

  disable: async (id: number): Promise<Rule> => {
    const response = await apiClient.patch<Rule>(`/rules/${id}/disable`);
    return response.data;
  },
};
